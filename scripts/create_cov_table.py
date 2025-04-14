#!/usr/bin/env python3
import pandas as pd
import argparse

def load_and_process_data(trial_file, time_file):
    """Load and process the two input CSV files."""
    # Load the CSV files
    trial_df = pd.read_csv(trial_file)
    time_df = pd.read_csv(time_file)
    
    # Add a column to denote the type
    trial_df['bound_type'] = 'Trial'
    time_df['bound_type'] = 'Time'
    
    # Combine the dataframes
    combined_df = pd.concat([trial_df, time_df])
    
    # Filter out rows where time is not 1 day
    filtered_df = combined_df[combined_df['time'] == '1 days 00:00:00']
    
    # Filter out rows with BeDiv-Simple
    filtered_df = filtered_df[filtered_df['fuzzer'] != 'BeDiv-Simple']
    
    # Rename BeDiv-Struct to BeDivFuzz and Zeugma-Link to Zeugma for the table
    filtered_df['fuzzer'] = filtered_df['fuzzer'].replace({
        'BeDiv-Struct': 'BeDivFuzz',
        'Zeugma-Link': 'Zeugma'
    })
    
    return filtered_df

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
            zest_trial = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject) & (data['bound_type'] == 'Trial')]['stat'].values
            zest_time = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject) & (data['bound_type'] == 'Time')]['stat'].values
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
                row_data = data[(data['fuzzer'] == fuzzer) & (data['subject'] == subject) & (data['bound_type'] == bound_type)]
                
                if len(row_data) == 0:
                    row_parts.append("N/A")
                    continue
                
                value = row_data['stat'].values[0]
                significance = row_data['sig'].values[0]
                
                # Determine if this value is the highest for this subject and bound_type
                all_values = data[(data['subject'] == subject) & (data['bound_type'] == bound_type)]['stat'].values
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
        # Get the data for Zeugma for Trial bound_type
        zeugma_trial = data[(data['fuzzer'] == 'Zeugma') & (data['subject'] == subject) & (data['bound_type'] == 'Trial')]
        zest_trial = data[(data['fuzzer'] == 'Zest') & (data['subject'] == subject) & (data['bound_type'] == 'Trial')]
        
        if len(zeugma_trial) == 0 or len(zest_trial) == 0:
            zeugma_n_row_parts.extend(["N/A", "N/A"])
            continue
            
        # For Fixed, always N/A
        zeugma_n_row_parts.append("N/A")
        
        # For Trial, use the Zeugma value
        value = zeugma_trial['stat'].values[0]
        significance = zeugma_trial['sig'].values[0]
        zest_val = zest_trial['stat'].values[0]
        
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
    parser.add_argument('trial_bounded', help='Path to the trial-bounded coverage CSV file')
    parser.add_argument('time_bounded', help='Path to the time-bounded coverage CSV file')
    parser.add_argument('--output', '-o', default='output_table.tex', help='Output file path')
    
    args = parser.parse_args()
    
    # Load and process the data
    data = load_and_process_data(args.trial_bounded, args.time_bounded)
    
    # Generate the LaTeX table
    latex_table = generate_latex_table(data)
    
    # Write the table to the output file
    with open(args.output, 'w') as f:
        f.write(latex_table)
    
    print(f"LaTeX table written to {args.output}")

if __name__ == "__main__":
    main()