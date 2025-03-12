import json
import os
import pathlib
import sys
from dataclasses import dataclass
from collections import defaultdict

import numpy as np
import pandas as pd

FAILURES_FILE_NAME = 'failures.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'
PLOT_DATA_FILE = 'campaign/plot_data'
ZEUGMA_PLOT_DATA_FILE = 'campaign/statistics.csv'


@dataclass(order=True, frozen=True)
class StackTraceElement:
    """Represents an element in a Java stack trace."""
    declaringClass: str
    fileName: str = None
    methodName: str = None
    lineNumber: int = -1

    def __repr__(self):
        if self.lineNumber == -2:
            x = 'Native Method'
        elif self.fileName is None:
            x = "Unknown Source"
        elif self.lineNumber >= 0:
            x = f"{self.fileName}:{self.lineNumber}"
        else:
            x = self.fileName
        return f"{self.declaringClass}.{self.methodName}({x})"


class Campaign:
    """Represents the results of a fuzzing campaign."""

    def __init__(self, campaign_dir):
        self.id = os.path.basename(campaign_dir)
        self.coverage_file = os.path.join(campaign_dir, COVERAGE_FILE_NAME)
        self.summary_file = os.path.join(campaign_dir, SUMMARY_FILE_NAME)
        self.failures_file = os.path.join(campaign_dir, FAILURES_FILE_NAME)
        self.plot_data_file = os.path.join(campaign_dir, PLOT_DATA_FILE)
        
        if not os.path.exists(self.plot_data_file):
            self.plot_data_file = os.path.join(campaign_dir, ZEUGMA_PLOT_DATA_FILE)
        
        self.valid = all(os.path.exists(f) for f in [self.plot_data_file]) and \
                    all(os.path.isfile(f) for f in [self.coverage_file, self.summary_file, self.failures_file])
        
        if self.valid:
            with open(self.summary_file, 'r') as f:
                summary = json.load(f)
                self.subject = summary['configuration']['testClassName'].split('.')[-1].replace('Fuzz', '')
                self.fuzzer = Campaign.get_fuzzer(summary)
                self.duration = summary['configuration']['duration']
            
            # Initialize executions and corpus_size attributes
            self.executions = 0
            self.corpus_size = 0

    @staticmethod
    def get_fuzzer(summary):
        fuzzer = summary['frameworkClassName'].split('.')[-1].replace('Framework', '')
        if fuzzer == 'BeDivFuzz':
            fuzzer = 'BeDiv'
            if '-Djqf.div.SAVE_ONLY_NEW_STRUCTURES=true' in summary['configuration']['javaOptions']:
                fuzzer += '-Struct'
            else:
                fuzzer += '-Simple'
        elif fuzzer == 'Zeugma':
            crossover_type = 'X'
            for opt in summary['configuration']['javaOptions']:
                if opt.startswith('-Dzeugma.crossover='):
                    crossover_type = opt[len('-Dzeugma.crossover='):].title()
            fuzzer += "-" + crossover_type.replace('Linked', 'Link') \
                .replace('One_Point', '1PT') \
                .replace('Two_Point', '2PT') \
                .replace('None', 'X')
        return fuzzer

    def add_trial_info(self, df):
        df['subject'] = self.subject
        df['campaign_id'] = self.id
        df['fuzzer'] = self.fuzzer

    def get_coverage_data(self):
        df = pd.read_csv(self.coverage_file) \
            .rename(columns=lambda x: x.strip())
        
        df['time'] = pd.to_timedelta(df['time'], 'ms')
        self.add_trial_info(df)
        return df

    def get_executions(self):
        """Parse the plot_data file to get the number of executions."""
        if not os.path.exists(self.plot_data_file):
            print(f"Warning: Plot data file does not exist for campaign {self.id}")
            return
        
        try:
            with open(self.plot_data_file, 'r') as f:
                last_line = f.readlines()[-1].strip()
            # print(self.fuzzer)
            # print(last_line)
                
            if self.fuzzer == "Zeugma-Link":
                # For Zeugma-Link, executions is the 4th column
                self.executions = int(last_line.split(",")[3])
            elif self.fuzzer == "BeDiv-Struct" or self.fuzzer == "BeDiv-Simple":
                # For BeDivFuzz-Struct, executions is the 5th column
                self.executions = int(last_line.split(",")[4])
            else:
                # For other fuzzers, executions is the sum of 12th and 13th columns
                split = last_line.split(",")
                self.executions = int(split[11]) + int(split[12])
                
            return self.executions
        except Exception as e:
            print(f"Error parsing executions for campaign {self.id}: {str(e)}")
            return 0

    def get_corpus_size_at_execution_limit(self, execution_limit):
        """
        Parse the plot_data file to get the corpus size at the last row 
        that does not exceed the execution limit.
        """
        if not os.path.exists(self.plot_data_file):
            print(f"Warning: Plot data file does not exist for campaign {self.id}")
            return 0
        
        try:
            lines = []
            with open(self.plot_data_file, 'r') as f:
                lines = f.readlines()
            
            # Skip header if it exists
            start_idx = 1 if len(lines) > 1 and not lines[0][0].isdigit() else 0
            
            last_valid_line = None
            for line in lines[start_idx:]:
                split = line.strip().split(",")
                
                if self.fuzzer == "Zeugma-Link":
                    executions = int(split[3])
                    if executions <= execution_limit:
                        last_valid_line = split
                    else:
                        break
                elif self.fuzzer == "BeDiv-Struct" or self.fuzzer == "BeDiv-Simple":
                    executions = int(split[4])
                    if executions <= execution_limit:
                        last_valid_line = split
                    else:
                        break
                else:
                    executions = int(split[11]) + int(split[12])
                    if executions <= execution_limit:
                        last_valid_line = split
                    else:
                        break
            
            if last_valid_line:
                if self.fuzzer == "Zeugma-Link":
                    # Corpus size is the 5th column for Zeugma-Link
                    self.corpus_size = int(last_valid_line[4])
                elif self.fuzzer == "BeDiv-Struct" or self.fuzzer == "BeDiv-Simple":
                    # For BeDiv-Struct, corpus size is the 6th column
                    self.corpus_size = int(last_valid_line[6])
                else:
                    # Corpus size is the 4th column for other fuzzers
                    self.corpus_size = int(last_valid_line[3])
                    
                return self.corpus_size
            else:
                print(f"Warning: No valid line found within execution limit for campaign {self.id}")
                return 0
                
        except Exception as e:
            print(f"Error parsing corpus size for campaign {self.id}: {str(e)}")
            return 0

    def get_failure_data(self):
        with open(self.failures_file, 'r') as f:
            records = json.load(f)
        if len(records) == 0:
            df = pd.DataFrame([], columns=['type', 'trace', 'detection_time', 'inducing_inputs'])
        else:
            df = pd.DataFrame.from_records(records) \
                .rename(columns=lambda x: x.strip())
            df['type'] = df['failure'].apply(lambda x: x['type'])
            df['trace'] = df['trace'] = df['failure'].apply(
                lambda x: tuple(map(lambda y: StackTraceElement(**y), x['trace'])))
            df['detection_time'] = pd.to_timedelta(df['firstTime'], 'ms')
            df = df.rename(columns={'inducingInputs': 'inducing_inputs'})
            df = df[['type', 'trace', 'detection_time', 'inducing_inputs']]
        self.add_trial_info(df)
        return df


def find_campaigns(input_dir):
    print(f'Searching for campaigns in {input_dir}.')
    files = [os.path.join(input_dir, f) for f in os.listdir(input_dir)]
    campaigns = list(map(Campaign, filter(os.path.isdir, files)))
    print(f"\tFound {len(campaigns)} campaigns.")
    return campaigns


def check_campaigns(campaigns):
    print(f'Checking campaigns.')
    result = []
    for c in campaigns:
        if not c.valid:
            print(f"\tMissing required files for {c.id}.")
        else:
            result.append(c)
    print(f'\t{len(result)} campaigns were valid.')
    return result


def read_campaigns(input_dir):
    return check_campaigns(find_campaigns(input_dir))


def get_executions_for_all_campaigns(campaigns):
    """Parse plot_data files and save executions count for each campaign."""
    print(f'Getting execution counts for all campaigns.')
    for campaign in campaigns:
        campaign.get_executions()
    print(f'\tFinished getting execution counts.')
    return campaigns


def find_min_executions_per_benchmark(campaigns):
    """Find the minimum number of executions per benchmark."""
    print(f'Finding minimum executions per benchmark.')
    min_executions = defaultdict(lambda: float('inf'))
    
    for campaign in campaigns:
        if campaign.executions > 0:  # Only consider campaigns with valid execution counts
            min_executions[campaign.subject] = min(min_executions[campaign.subject], campaign.executions)
    
    # Convert from defaultdict to regular dict and handle the case where no valid executions were found
    result = {}
    for benchmark, min_exec in min_executions.items():
        if min_exec != float('inf'):
            result[benchmark] = min_exec
        else:
            print(f"\tWarning: No valid execution counts found for benchmark {benchmark}")
            result[benchmark] = 0
    
    print(f'\tMinimum executions per benchmark: {result}')
    return result


def get_corpus_sizes_at_execution_limits(campaigns, min_executions_per_benchmark):
    """
    Parse plot_data files again to get corpus sizes at the minimum execution count for each benchmark.
    """
    print(f'Getting corpus sizes at execution limits.')
    for campaign in campaigns:
        benchmark = campaign.subject
        execution_limit = min_executions_per_benchmark.get(benchmark, 0)
        
        if execution_limit > 0:
            campaign.get_corpus_size_at_execution_limit(execution_limit)
        else:
            print(f"\tSkipping corpus size calculation for campaign {campaign.id} due to zero execution limit")
    
    print(f'\tFinished getting corpus sizes.')
    return campaigns


def create_corpus_size_csv(campaigns, output_dir):
    """Create a CSV file with corpus size information."""
    file = os.path.join(output_dir, 'corpus_sizes.csv')
    print('Creating corpus size CSV.')
    
    data = []
    for c in campaigns:
        data.append({
            'campaign_id': c.id,
            'fuzzer': c.fuzzer,
            'subject': c.subject,
            'executions': c.executions,
            'corpus_size': c.corpus_size
        })
    
    df = pd.DataFrame(data)
    df.to_csv(file, index=False)
    print(f'\tWrote corpus size CSV to {file}.')
    return df


def copy_controlled_corpus_files(input_dir, output_dir):
    """
    Copy a controlled set of corpus files based on the corpus_size in the CSV.
    For each campaign, copies the first corpus_size files to a new directory.
    """
    corpus_sizes_file = os.path.join(output_dir, 'corpus_sizes.csv')
    print(f'Copying controlled corpus files based on {corpus_sizes_file}')
    
    if not os.path.exists(corpus_sizes_file):
        print(f"\tError: Corpus sizes file does not exist: {corpus_sizes_file}")
        return
    
    try:
        # Read the corpus sizes CSV
        corpus_df = pd.read_csv(corpus_sizes_file)
        
        for _, row in corpus_df.iterrows():
            campaign_id = row['campaign_id']
            corpus_size = int(row['corpus_size'])
            
            # Define source and destination directories
            source_dir = os.path.join(input_dir, campaign_id, 'campaign', 'corpus')
            dest_dir = os.path.join(input_dir, campaign_id, 'campaign', 'corpus_trial_controlled')
            
            if not os.path.isdir(source_dir):
                print(f"\tWarning: Source corpus directory does not exist: {source_dir}")
                continue
            
            # Create destination directory if it doesn't exist
            os.makedirs(dest_dir, exist_ok=True)
            
            # Get all files in the source directory and sort by filename
            try:
                corpus_files = sorted([f for f in os.listdir(source_dir) if os.path.isfile(os.path.join(source_dir, f))])
                
                # Take only the first corpus_size files
                files_to_copy = corpus_files[:corpus_size]
                
                print(f"\tCopying {len(files_to_copy)} files (out of {len(corpus_files)}) for campaign {campaign_id}")
                
                # Copy the files
                import shutil
                for file in files_to_copy:
                    source_file = os.path.join(source_dir, file)
                    dest_file = os.path.join(dest_dir, file)
                    shutil.copy2(source_file, dest_file)
                
                print(f"\tSuccessfully copied files to {dest_dir}")
                
            except Exception as e:
                print(f"\tError copying files for campaign {campaign_id}: {str(e)}")
        
        print(f'\tFinished copying controlled corpus files.')
        
    except Exception as e:
        print(f"\tError processing corpus sizes CSV: {str(e)}")


def resample(data, time_index):
    data = data.copy() \
        .set_index('time') \
        .sort_index()
    # Create a placeholder data frame indexed at the sample times filled with NaNs
    placeholder = pd.DataFrame(np.nan, index=time_index, columns=data.columns)
    # Combine the placeholder data frame with the original
    # Replace the NaN's in placeholder with the last value at or before the sample time in the original
    data = data.combine_first(placeholder).ffill().fillna(0)
    # Drop times not in the resample index
    return data.loc[data.index.isin(time_index)] \
        .reset_index() \
        .rename(columns={'index': 'time'}) \
        .drop_duplicates(subset=['time'])


def create_coverage_csv(campaigns, times):
    duration = max(times)
    # Create an index from 0 to duration (inclusive) with 1000 sample times
    index = pd.timedelta_range(start=pd.Timedelta(0, 'ms'), end=duration, closed=None, periods=1000)
    # Ensure that the specified times are included in the index
    index = index.union(pd.TimedeltaIndex(sorted(times)))
    # Resample the data for each campaign at the index times
    return pd.concat([resample(c.get_coverage_data(), index) for c in campaigns]) \
        .reset_index()


def create_failures_table(campaigns):
    failures = pd.concat([t.get_failure_data() for t in campaigns]) \
        .reset_index(drop=True) \
        .sort_values(['subject', 'type', 'trace'])
    # Read known failures
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'data', 'failures.json'), 'r') as f:
        known_failures = json.load(f)
    for f in known_failures:
        f['trace'] = tuple(map(lambda y: StackTraceElement(**y), f['trace']))
    # Match failures against known failures which have been manually mapped to defects
    return pd.merge(failures, pd.DataFrame.from_records(known_failures), on=["subject", "type", "trace"], how="left")


def create_detections_table(campaigns):
    # 1. Read detected failure
    # 2. Remove failures not manually mapped to defects
    # 3. Transform each associated defect into a row
    # 4. Simplify column names.
    # 5. Remove rows for failures associated with no defects
    # 6. Find the first detection of each defect for each campaign
    # 7. Flatten the table and select desired columns
    return create_failures_table(campaigns) \
        .dropna(subset=['associatedDefects']) \
        .explode('associatedDefects') \
        .rename(columns={'associatedDefects': 'defect', 'detection_time': 'time'}) \
        .dropna(subset=['defect']) \
        .groupby(['campaign_id', 'fuzzer', 'defect', 'subject']) \
        .min() \
        .reset_index()[['campaign_id', 'fuzzer', 'subject', 'defect', 'time']]


def create_defects_csv(campaigns):
    # Find the first detection of each defect for each campaign
    detections = create_detections_table(campaigns)
    # If a campaign never detected a particular defect, fill in NaT
    defect_pairs = detections[['defect', 'subject']].drop_duplicates().itertuples(index=False, name=None)
    rows = []
    for defect, subject in defect_pairs:
        rows.extend([c.id, c.fuzzer, c.subject, defect, pd.NaT] for c in campaigns if c.subject == subject)
    return pd.DataFrame(rows, columns=['campaign_id', 'fuzzer', 'subject', 'defect', 'time']) \
        .set_index(['campaign_id', 'fuzzer', 'subject', 'defect']) \
        .combine_first(detections.set_index(['campaign_id', 'fuzzer', 'subject', 'defect'])) \
        .reset_index() \
        .sort_values(by=['campaign_id', 'defect'])


def extract_coverage_data(campaigns, times, output_dir):
    file = os.path.join(output_dir, 'coverage.csv')
    print('Creating coverage CSV.')
    coverage = create_coverage_csv(campaigns, times)
    coverage.to_csv(file, index=False)
    print(f'\tWrote coverage CSV to {file}.')
    return coverage


def extract_detections_data(campaigns, output_dir):
    file = os.path.join(output_dir, 'detections.csv')
    print('Creating defect detections CSV.')
    defects = create_defects_csv(campaigns)
    defects.to_csv(file, index=False)
    print(f'\tWrote defects detections CSV to {file}.')
    return defects


def extract_corpus_size_data(campaigns, output_dir):
    # First, get execution counts for all campaigns
    campaigns = get_executions_for_all_campaigns(campaigns)
    
    # Find minimum executions per benchmark
    min_executions = find_min_executions_per_benchmark(campaigns)
    
    # Get corpus sizes at the execution limits
    campaigns = get_corpus_sizes_at_execution_limits(campaigns, min_executions)
    
    # Create and save corpus size CSV
    return create_corpus_size_csv(campaigns, output_dir)


def extract_data(input_dir, output_dir):
    print("Extracting more data")
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(24, 'h')]
    campaigns = read_campaigns(input_dir)
    os.makedirs(output_dir, exist_ok=True)
    
    # Extract corpus size data (new functionality)
    print("Extracting corpus data")
    corpus_data = extract_corpus_size_data(campaigns, output_dir)
    print("Extracted corpus data")
    
    # Copy controlled corpus files based on corpus sizes
    copy_controlled_corpus_files(input_dir, output_dir)
    
    # Extract existing data
    coverage_data = extract_coverage_data(campaigns, times, output_dir)
    detections_data = extract_detections_data(campaigns, output_dir)
    
    return coverage_data, detections_data, corpus_data


def main():
    if len(sys.argv) < 3:
        print("Usage: python script.py <input_dir> <output_dir>")
        sys.exit(1)
    
    extract_data(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()