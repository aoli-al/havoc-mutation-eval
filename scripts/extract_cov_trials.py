import json
import os
import pathlib
import sys
import shutil
from dataclasses import dataclass
from collections import defaultdict

import numpy as np
import pandas as pd

FAILURES_FILE_NAME = 'failures.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'
PLOT_DATA_FILE_NAME = 'campaign/plot_data'
ZEUGMA_PLOT_DATA_FILE_NAME = 'campaign/statistics.csv'


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
        self.plot_data_file = os.path.join(campaign_dir, PLOT_DATA_FILE_NAME)
        self.corpus_dir = os.path.join(campaign_dir, "campaign", "corpus")

        if not os.path.exists(self.plot_data_file):
            self.plot_data_file = os.path.join(campaign_dir, ZEUGMA_PLOT_DATA_FILE_NAME)

        self.valid = os.path.exists(self.plot_data_file) and os.path.exists(self.corpus_dir)
                    # all(os.path.isfile(f) for f in [self.coverage_file, self.summary_file, self.failures_file])
        if not self.valid:
            print(f"INVALID: {self.id}")
            print(f"Plot data file: {self.plot_data_file}, {os.path.exists(self.plot_data_file)}")
            print(f"Corpus dir: {self.corpus_dir}, {os.path.exists(self.corpus_dir)}")


        if self.valid:
            # with open(self.summary_file, 'r') as f:
            #     summary = json.load(f)
            self.subject = self.id.split("-")[0].strip()
            # self.fuzzer = Campaign.get_fuzzer(summary)
            # self.duration = summary['configuration']['duration']

            # Initialize executions and corpus_size attributes
            self.executions = 0
            self.corpus_size = 0
            self.execution_time = 0  # Time to reach the execution count (in seconds)
            self.corpus_time_size = 0  # Number of files in corpus within the time limit

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

            if "zeugma" in self.id:
                # For Zeugma-Link, executions is the 4th column
                self.executions = int(last_line.split(",")[3])
            elif "bediv" in self.id:
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

    def get_time_for_executions(self, execution_count):
        """
        Get the time it took to reach a specific execution count.
        For Zeugma, the time is in the first column (milliseconds).
        For other fuzzers, the first column is unix timestamp, so we subtract the first row's timestamp.
        Returns time in seconds.
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

            first_timestamp = None
            last_valid_timestamp = None

            for line in lines[start_idx:]:
                split = line.strip().split(",")

                # Get the timestamp/time from the first column
                timestamp = float(split[0])
                if first_timestamp is None:
                    first_timestamp = timestamp

                # Get execution count based on campaign type
                if "zeugma" in self.id:
                    # Time directly in milliseconds for Zeugma
                    executions = int(split[3])
                    if executions <= execution_count:
                        last_valid_timestamp = timestamp
                    else:
                        break
                elif "bediv" in self.id:
                    executions = int(split[4])
                    if executions <= execution_count:
                        last_valid_timestamp = timestamp
                    else:
                        break
                else:
                    executions = int(split[11]) + int(split[12])
                    if executions <= execution_count:
                        last_valid_timestamp = timestamp
                    else:
                        break

            if last_valid_timestamp is not None:
                if "zeugma" in self.id:
                    # For Zeugma, time is already in milliseconds, convert to seconds
                    time_seconds = last_valid_timestamp / 1000.0
                else:
                    # For other fuzzers, subtract first timestamp (unix time) to get elapsed time in seconds
                    time_seconds = last_valid_timestamp - first_timestamp

                self.execution_time = time_seconds
                return time_seconds
            else:
                print(f"Warning: No valid time found for execution count {execution_count} in campaign {self.id}")
                return 0

        except Exception as e:
            print(f"Error calculating time for campaign {self.id}: {str(e)}")
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

                if "zeugma" in self.id:
                    executions = int(split[3])
                    if executions <= execution_limit:
                        last_valid_line = split
                    else:
                        break
                elif "bediv" in self.id:
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
                if "zeugma" in self.id:
                    # Corpus size is the 5th column for Zeugma-Link
                    self.corpus_size = int(last_valid_line[4])
                elif "bediv" in self.id:
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

    def get_corpus_size_at_time_limit(self, time_limit_seconds):
        """
        Count the number of corpus files that were created within the time limit.

        Args:
            time_limit_seconds: Time limit in seconds from the first file's creation time

        Returns:
            Number of corpus files created within the time limit
        """
        import os
        import time

        if not os.path.exists(self.corpus_dir):
            print(f"Warning: Corpus directory does not exist for campaign {self.id}")
            return 0

        try:
            # Get all files in the corpus directory
            corpus_files = [os.path.join(self.corpus_dir, f) for f in os.listdir(self.corpus_dir)
                           if os.path.isfile(os.path.join(self.corpus_dir, f))]

            if not corpus_files:
                print(f"Warning: No corpus files found for campaign {self.id}")
                return 0

            # Sort files by modification time
            corpus_files.sort(key=lambda f: os.path.getmtime(f))

            # Get the modification time of the first file
            start_time = os.path.getmtime(corpus_files[0])

            # Calculate the end time as start_time + time_limit
            end_time = start_time + time_limit_seconds

            # Count files with modification time <= end_time
            count = sum(1 for f in corpus_files if os.path.getmtime(f) <= end_time)

            self.corpus_time_size = count
            return count

        except Exception as e:
            print(f"Error calculating corpus size at time limit for campaign {self.id}: {str(e)}")
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

    def get_total_runtime(self):
        """
        Calculate the total runtime of the campaign.
        For Zeugma, the time is in the first column (milliseconds).
        For other fuzzers, the first column is unix timestamp, so we subtract first row from last row.
        Returns time in seconds.
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

            if start_idx >= len(lines):
                print(f"Warning: No data rows in plot data file for campaign {self.id}")
                return 0

            # Get first and last timestamp
            first_line = lines[start_idx].strip().split(",")
            last_line = lines[-1].strip().split(",")

            first_timestamp = float(first_line[0])
            last_timestamp = float(last_line[0])

            if "zeugma" in self.id:
                # For Zeugma, time is directly in milliseconds, convert to seconds
                # The last timestamp is the total elapsed time
                runtime_seconds = last_timestamp / 1000.0
            else:
                # For other fuzzers, subtract first timestamp from last (unix timestamps)
                runtime_seconds = last_timestamp - first_timestamp

            return runtime_seconds

        except Exception as e:
            print(f"Error calculating total runtime for campaign {self.id}: {str(e)}")
            return 0


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
    Parse plot_data files again to get corpus sizes and time at the minimum execution count for each benchmark.
    For each campaign, finds the latest row that has executions <= min_executions and extracts both
    the corpus size and the time it took to reach that point.
    """
    print(f'Getting corpus sizes at execution limits.')
    for campaign in campaigns:
        benchmark = campaign.subject
        execution_limit = min_executions_per_benchmark.get(benchmark, 0)

        if execution_limit > 0:
            # Get corpus size at the execution limit
            campaign.get_corpus_size_at_execution_limit(execution_limit)

            # Get the time it took to reach this execution limit
            campaign.get_time_for_executions(execution_limit)
        else:
            print(f"\tSkipping corpus size calculation for campaign {campaign.id} due to zero execution limit")

    print(f'\tFinished getting corpus sizes and times.')
    return campaigns


def get_corpus_sizes_at_time_limits(campaigns, min_executions_per_benchmark):
    """
    For each campaign, find the time it took to reach the benchmark's minimum execution count,
    then count corpus files created within that time period based on file modification times.
    """
    print(f'Getting corpus sizes based on time limits.')
    for campaign in campaigns:
        benchmark = campaign.subject
        execution_limit = min_executions_per_benchmark.get(benchmark, 0)

        if execution_limit > 0:
            # First, get the time it took to reach the execution limit
            time_limit_seconds = campaign.get_time_for_executions(execution_limit)

            if time_limit_seconds > 0:
                # Then, count corpus files within that time limit
                campaign.get_corpus_size_at_time_limit(time_limit_seconds)
            else:
                print(f"\tSkipping corpus size calculation for campaign {campaign.id} due to zero time limit")
        else:
            print(f"\tSkipping corpus size calculation for campaign {campaign.id} due to zero execution limit")

    print(f'\tFinished getting corpus sizes based on time limits.')
    return campaigns


def create_corpus_size_csv(campaigns, output_dir):
    """Create a CSV file with corpus size information for both methods (execution-based and time-based)."""
    file = os.path.join(output_dir, 'corpus_sizes.csv')
    print('Creating corpus size CSV.')

    data = []
    for c in campaigns:
        data.append({
            'campaign_id': c.id,
            'subject': c.subject,
            'executions': c.executions,
            'execution_based_corpus_size': c.corpus_size,
            'time_to_execution_limit': c.execution_time,
            'time_based_corpus_size': c.corpus_time_size
        })

    df = pd.DataFrame(data)
    df.to_csv(file, index=False)
    print(f'\tWrote corpus size CSV to {file}.')
    return df


def copy_controlled_corpus_files(input_dir, output_dir):
    """
    Copy a controlled set of corpus files based on the time_based_corpus_size in the CSV.
    For each campaign, sorts the files by modification time and copies those created
    within the time limit to a new directory.
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
            time_limit = float(row['time_to_execution_limit'])
            time_based_corpus_size = int(row['time_based_corpus_size'])

            # Define source and destination directories
            source_dir = os.path.join(input_dir, campaign_id, 'campaign', 'corpus')
            dest_dir = os.path.join(input_dir, campaign_id, 'campaign', 'corpus_trial_controlled')

            if os.path.exists(dest_dir):
                # Remove existing destination directory
                shutil.rmtree(dest_dir)

            if not os.path.isdir(source_dir):
                print(f"\tWarning: Source corpus directory does not exist: {source_dir}")
                continue

            # Create destination directory if it doesn't exist
            os.makedirs(dest_dir, exist_ok=True)

            # Get all files in the source directory
            try:
                all_files = [os.path.join(source_dir, f) for f in os.listdir(source_dir)
                            if os.path.isfile(os.path.join(source_dir, f))]

                if not all_files:
                    print(f"\tWarning: No corpus files found for campaign {campaign_id}")
                    continue

                # Sort files by modification time
                all_files.sort(key=lambda f: os.path.getmtime(f))

                # Get the modification time of the first file
                start_time = os.path.getmtime(all_files[0])

                # Calculate the end time as start_time + time_limit
                end_time = start_time + time_limit

                # Filter files to those modified before end_time
                files_to_copy = [f for f in all_files if os.path.getmtime(f) <= end_time]

                print(f"\tCopying {len(files_to_copy)} files (out of {len(all_files)}) for campaign {campaign_id}")

                # Copy the files
                for source_file in files_to_copy:
                    file_name = os.path.basename(source_file)
                    dest_file = os.path.join(dest_dir, file_name)
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
    # Filter out campaigns with 20 in the ID
    print("Filtering campaigns...")
    print(len(campaigns))
    campaigns = [c for c in campaigns if '20' not in c.id]
    print(len(campaigns))

    # Find minimum executions per benchmark
    min_executions = find_min_executions_per_benchmark(campaigns)

    # Write campaign trials summary CSV - NEW
    create_campaign_trials_summary(campaigns, output_dir)

    # First approach: Get corpus sizes at execution limits
    campaigns = get_corpus_sizes_at_execution_limits(campaigns, min_executions)

    # Second approach: Get corpus sizes at time limits based on file modification times
    campaigns = get_corpus_sizes_at_time_limits(campaigns, min_executions)

    # Create and save corpus size CSV with both methods
    return create_corpus_size_csv(campaigns, output_dir)


def create_campaign_trials_summary(campaigns, output_dir):
    """Create a CSV with all campaigns and their trials per benchmark, plus summary statistics per technique."""
    print('Creating campaign trials summary CSV.')

    # Calculate total runtime for each campaign
    for campaign in campaigns:
        campaign.total_runtime = campaign.get_total_runtime()

    # Extract technique name from campaign ID
    for campaign in campaigns:
        # Extract fuzzer technique from campaign ID
        id_parts = campaign.id.lower().split('-')
        # Skip the subject and get the technique part
        technique = None

        # Special handling for zest-mini
        if 'zest-mini' in campaign.id.lower():
            technique = 'zest-mini'
        else:
            # For other techniques
            for part in id_parts[1:]:
                if any(tech in part for tech in ['zeugma', 'bediv', 'zest', 'ei', 'random']):
                    technique = part
                    break

        campaign.technique = technique

    # Create a dataframe with detailed campaign information
    data = []
    for c in campaigns:
        # Skip campaigns with bedivfuzz-simple technique
        if c.technique == 'bedivfuzz-simple' or 'bedivfuzz-simple' in c.id.lower():
            continue

        data.append({
            'campaign_id': c.id,
            'benchmark': c.subject,
            'technique': c.technique,
            'executions': c.executions,
            'corpus_size': c.corpus_size,
            'time_to_exec_limit': c.execution_time,
            'time_based_corpus_size': c.corpus_time_size,
            'total_runtime': c.total_runtime  # Add total runtime
        })

    detail_df = pd.DataFrame(data)

    # Identify campaigns with total runtime significantly less than 86400 seconds (24 hours)
    threshold = 86400 * 0.99  # 99% of 24 hours, allowing for some margin
    short_runtime_campaigns = detail_df[detail_df['total_runtime'] < threshold]

    # Write the short runtime campaigns to a file, grouped by benchmark and technique
    short_runtime_file = os.path.join(output_dir, 'short_runtime_campaigns.txt')
    with open(short_runtime_file, 'w') as f:
        # Write header with total count
        total_short_campaigns = len(short_runtime_campaigns)
        f.write(f"Total campaigns with runtime < 99% of 24 hours: {total_short_campaigns}\n\n")

        # Group by benchmark and technique
        grouped = short_runtime_campaigns.groupby(['benchmark', 'technique'])

        for (benchmark, technique), group in grouped:
            f.write(f"Benchmark: {benchmark}, Technique: {technique}\n")
            f.write(f"Count: {len(group)}\n")
            f.write("-" * 80 + "\n")

            # Sort by runtime
            sorted_group = group.sort_values(by='total_runtime')

            for _, row in sorted_group.iterrows():
                f.write(f"  {row['campaign_id']}: {row['total_runtime']:.2f} seconds ({row['total_runtime']/3600:.2f} hours)\n")

            f.write("\n")

    print(f"\tIdentified {total_short_campaigns} campaigns with shorter than expected runtime. Details written to {short_runtime_file}")

    # Write the detailed campaign info to CSV
    detail_file = os.path.join(output_dir, 'campaign_trials_detail.csv')
    detail_df.to_csv(detail_file, index=False)
    print(f'\tWrote detailed campaign trials to {detail_file}.')

    # Calculate summary statistics per technique per benchmark
    summary_data = []
    threshold = 86400 * 0.99  # 99% of 24 hours, allowing for some margin

    for benchmark in detail_df['benchmark'].unique():
        benchmark_df = detail_df[detail_df['benchmark'] == benchmark]

        for technique in benchmark_df['technique'].unique():
            technique_df = benchmark_df[benchmark_df['technique'] == technique]

            # Filter out campaigns with short runtimes
            valid_technique_df = technique_df[technique_df['total_runtime'] >= threshold]

            # Skip if no valid campaigns remain after filtering
            if len(valid_technique_df) == 0:
                print(f"\tWarning: No valid campaigns for {benchmark}/{technique} after filtering short runtimes")
                continue

            # Calculate statistics using only valid campaigns
            summary_data.append({
                'benchmark': benchmark,
                'technique': technique,
                'num_trials': len(valid_technique_df),  # This now reflects only valid trials
                'total_trials': len(technique_df),  # Keep track of total trials before filtering
                'avg_executions': valid_technique_df['executions'].mean(),
                'std_executions': valid_technique_df['executions'].std(),
                'avg_exec_speed': valid_technique_df['executions'].mean() / 86400,  # Average executions per second
                'std_exec_speed': valid_technique_df['executions'].std() / 86400,  # Std dev of executions per second
            })

    # Create summary dataframe and write to CSV
    summary_df = pd.DataFrame(summary_data)
    summary_file = os.path.join(output_dir, 'technique_benchmark_summary.csv')
    summary_df.to_csv(summary_file, index=False)
    print(f'\tWrote technique/benchmark summary to {summary_file}.')

    return detail_df, summary_df


def extract_data(input_dir, output_dir):
    print("Extracting more data")
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(24, 'h')]
    campaigns = read_campaigns(input_dir)
    os.makedirs(output_dir, exist_ok=True)

    # # Extract corpus size data (new functionality)
    print("Extracting corpus data")
    # corpus_data = extract_corpus_size_data(campaigns, output_dir)
    print("Extracted corpus data")

    # Copy controlled corpus files based on time-based corpus sizes
    copy_controlled_corpus_files(input_dir, output_dir)

    # Extract existing data
    # coverage_data = extract_coverage_data(campaigns, times, output_dir)
    # detections_data = extract_detections_data(campaigns, output_dir)

    return corpus_data


def main():
    if len(sys.argv) < 3:
        print("Usage: python script.py <input_dir> <output_dir>")
        sys.exit(1)

    extract_data(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
