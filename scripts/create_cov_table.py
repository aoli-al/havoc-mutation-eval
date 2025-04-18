#!/usr/bin/env python3
from tabulate import tabulate
import pandas as pd
import argparse
import report_util
from scripts.extract import Campaign

def get_closest_covered_branches_at(time_df, campaign_id, time_to_execution_limit):
    campaign_time_data = time_df[time_df['campaign_id'] == campaign_id]
    if len(campaign_time_data) == 0:
        return 0
            # Filter to only include times less than or equal to the execution limit
    valid_times = campaign_time_data[campaign_time_data['time'] <= time_to_execution_limit]

    if len(valid_times) == 0:
        # If no valid times found (all times are after the limit)
        return 0

    # Get the maximum time that's still within the limit
    max_valid_time = valid_times['time'].max()

    # Get the row with that time
    row_at_max_valid_time = valid_times[valid_times['time'] == max_valid_time]

    # Return the covered branches at that time
    return row_at_max_valid_time['covered_branches'].iloc[0]



def load_and_process_data(corpus_size, time_file):
    """Load and process the two input CSV files."""
    # Load the CSV files
    corpus_size_df = pd.read_csv(corpus_size)
    time_df = pd.read_csv(time_file)
    time_df['time'] = pd.to_timedelta(time_df['time'])


    corpus_size_df['time_to_execution_limit'] = pd.to_timedelta(corpus_size_df['time_to_execution_limit'], unit='s')
    corpus_size_df['normalized_execution_time'] = pd.to_timedelta(corpus_size_df['normalized_execution_time'], unit='s')
    rows = []

    for _, row in corpus_size_df.iterrows():
        campaign_id = row['campaign_id']

        rows.append({
            'subject': row['subject'],
            'fuzzer': Campaign.convert_id_to_fuzzer(campaign_id),
            'campaign_id': campaign_id,
            'trial_bound_coverage': get_closest_covered_branches_at(time_df, campaign_id, row['time_to_execution_limit']),
            'time_bound_coverage': get_closest_covered_branches_at(time_df, campaign_id, pd.Timedelta('1 days 00:00:00')),
            'normalized_coverage': get_closest_covered_branches_at(time_df, campaign_id, row['normalized_execution_time']),
        })
    df = pd.DataFrame(rows)
    df['fuzzer'] = df['fuzzer'].replace({
        'BeDiv-Struct': 'BeDivFuzz',
        'Zeugma-Link': 'Zeugma'
    })

    return df

def get_aggregated_coverage(df):
    aggregated = []
    for fuzzer in df['fuzzer'].unique():
        for subject in df['subject'].unique():
            new_data = {
                'fuzzer': fuzzer,
                'subject': subject,
                'trial_bound_coverage': df[(df['fuzzer'] == fuzzer) & (df['subject'] == subject)]['trial_bound_coverage'].median(),
                'time_bound_coverage': df[(df['fuzzer'] == fuzzer) & (df['subject'] == subject)]['time_bound_coverage'].median(),
                'normalized_coverage': df[(df['fuzzer'] == fuzzer) & (df['subject'] == subject)]['normalized_coverage'].median(),
            }
            for coverage in ['trial_bound_coverage', 'time_bound_coverage', 'normalized_coverage']:
                baseline = df[(df['fuzzer'] == 'Zest') & (df['subject'] == subject)][coverage].values
                values =df[(df['fuzzer'] == fuzzer) & (df['subject'] == subject)][coverage].values
                sig_level = report_util.compute_sig_level(df['fuzzer'].unique())
                if (report_util.mann_whitney(baseline, values) < sig_level):
                    new_data[coverage + "_sig"] = 'color: red;'
            aggregated.append(new_data)
    return pd.DataFrame(aggregated)




def generate_latex_table(data):
    """Generate the LaTeX table based on the processed data."""
    subjects = ['Ant', 'Chocopy', 'Closure', 'Gson', 'Jackson', 'Maven', 'Rhino']
    fuzzers = ['Random', 'Zest-Mini', 'Zest', 'EI', 'BeDivFuzz', 'Zeugma']
    bound_types = ['Trial', 'Time']
    data['subject'] = data['subject'].apply(lambda x: x.lower())

    # Initialize the table content
    table_content = []

    # Add the top part of the table
    table_top = r"""\begin{table}[t]
    \centering
    \scriptsize
    \setlength{\tabcolsep}{3pt}
    \caption{For each fuzzer, we report the median branch coverage in application classes for each subject across 20 fuzzing campaigns after 24 hours. Values in \textcolor{red}{red} indicate a statistically significant decrease compared to Zest, while \textcolor{\chigher}{olive} values show a significant increase. The highest value for each benchmark is highlighted in blue.}
    \label{tab:cov}
    \begin{tabular}{l|cc|cc|cc|cc|cc|cc|cc}
    \toprule
    \multirow{3}{*}{\textbf{Fuzzer}} & \multicolumn{2}{c|}{\textbf{Ant}} & \multicolumn{2}{c|}{\textbf{Chocopy}} & \multicolumn{2}{c|}{\textbf{Closure}} & \multicolumn{2}{c|}{\textbf{Gson}} & \multicolumn{2}{c|}{\textbf{Jackson}} & \multicolumn{2}{c|}{\textbf{Maven}} & \multicolumn{2}{c}{\textbf{Rhino}}\\
    & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} & Fixed & \multirow{2}{*}{24hr} \\
    & Trial & & Trial & & Trial & & Trial & & Trial & & Trial & & Trial & \\
    \midrule"""

    table_content.append(table_top)

    # For each fuzzer, create a row in the table
    for fuzzer in fuzzers:
        row_parts = [f"    {fuzzer}"]

        for subject in subjects:
            subject = subject.lower()
            # Get reference values for Zest
            zest_trial = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject)]['trial_bound_coverage'].values
            zest_time = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject)]['time_bound_coverage'].values
            print(fuzzer, subject)
            print(zest_trial)
            print(zest_time)

            if len(zest_trial) == 0 or len(zest_time) == 0:
                continue

            zest_trial_val = zest_trial[0]
            zest_time_val = zest_time[0]

            # For each bound type (Fixed and Trial)
            for i, bound_type in enumerate(bound_types):
                # Get the value and significance for this fuzzer/subject/bound_type
                row_data = data[(data['fuzzer'] == fuzzer) & (data['subject'] == subject)]

                if len(row_data) == 0:
                    row_parts.append("N/A")
                    continue

                bound_coverage_key = bound_type.lower() + '_bound_coverage'
                value = row_data[bound_coverage_key].values[0]
                significance = row_data[bound_coverage_key + '_sig'].values[0]

                # Determine if this value is the highest for this subject and bound_type
                all_values = data[(data['subject'] == subject)][bound_coverage_key].values
                is_highest = value == max(all_values)

                # Format the value based on significance and comparison to Zest
                if significance == 'color: red;':
                    reference_val = zest_trial_val if bound_type == 'Trial' else zest_time_val

                    if value < reference_val:
                        # Significantly worse than Zest
                        formatted_value = f"\\textcolor{{red}}{{{value}}}"
                    elif value > reference_val:
                        # Significantly better than Zest
                        formatted_value = f"\\textcolor{{\\chigher}}{{{value}}}"
                    else:
                        # Equal to Zest but marked as significant
                        formatted_value = f"{value}"
                else:
                    # Not significant
                    formatted_value = f"{value}"

                # Add blue highlighting if highest
                if is_highest:
                    formatted_value = f"\\cellcolor{{blue!15}}{formatted_value}"

                row_parts.append(formatted_value)

        # Combine the parts into a row
        row = " & ".join(row_parts) + " \\\\"
        table_content.append(row)

    # Add Zeugma^N row with N/A for Fixed and values for Trial
    zeugma_n_row_parts = [r"    Zeugma$^{N}$"]

    for subject in subjects:
        subject = subject.lower()
        # Get the data for Zeugma for Trial bound_type
        zeugma_trial = data[(data['fuzzer'] == 'Zeugma') & (data['subject'] == subject)]
        zest_trial = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject)]['time_bound_coverage'].values


        if len(zeugma_trial) == 0 or len(zest_trial) == 0:
            zeugma_n_row_parts.extend(["N/A", "N/A"])
            continue

        # For Fixed, always N/A
        zeugma_n_row_parts.append("N/A")

        # For Trial, use the Zeugma value
        value = zeugma_trial['normalized_coverage'].values[0]
        significance = zeugma_trial['normalized_coverage_sig'].values[0]
        zest_val = zest_trial[0]

        # Format based on significance
        if significance == 'color: red;':
            if value < zest_val:
                formatted_value = f"\\textcolor{{red}}{{{value}}}"
            elif value > zest_val:
                formatted_value = f"\\textcolor{{\\chigher}}{{{value}}}"
            else:
                formatted_value = f"{value}"
        else:
            formatted_value = f"{value}"

        zeugma_n_row_parts.append(formatted_value)

    zeugma_n_row = " & ".join(zeugma_n_row_parts) + " \\\\"
    table_content.append(zeugma_n_row)

    # Add the bottom part of the table
    table_bottom = r"""    \bottomrule
    \end{tabular}
\end{table}"""

    table_content.append(table_bottom)

    # Join all parts of the table
    return "\n".join(table_content)

def main():
    """Main function to handle command line arguments and generate the table."""
    parser = argparse.ArgumentParser(description='Generate a LaTeX table from fuzzing coverage data.')
    parser.add_argument('time_bounded', help='Path to the time-bounded coverage CSV file')
    parser.add_argument('corpus_size', help='Path to the corpus size CSV file')
    parser.add_argument('--output', '-o', default='output_table.tex', help='Output file path')

    args = parser.parse_args()

    # Load and process the data
    data = load_and_process_data(args.corpus_size, args.time_bounded)
    # data = pd.read_csv("aggregated_coverage.csv")
    data = data[(data['fuzzer'] != 'BeDivFuzz-Simple') & (data['fuzzer'] != 'Zeugma-None')]
    data = get_aggregated_coverage(data)
    print(tabulate(data, headers='keys', tablefmt='latex', showindex=False))


    # Generate the LaTeX table
    latex_table = generate_latex_table(data)

    # # Write the table to the output file
    with open(args.output, 'w') as f:
        f.write(latex_table)

    print(f"LaTeX table written to {args.output}")

if __name__ == "__main__":
    main()