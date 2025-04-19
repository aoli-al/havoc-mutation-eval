import os
import pathlib
import sys

import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import pandas as pd

from tabulate import tabulate
from tqdm import tqdm
import pandas as pd
import argparse
import scipy

fuzzer_map = {
    'BeDiv-Struct': 'BeDivFuzz',
    'Zeugma-Link': 'Zeugma',
    'Zeugma': 'Zeugma',
    'EI': 'EI',
    'Zest-Mini': 'Zest-Mini',
    'Zest': 'Zest',
    'Random': 'Random',
    'BeDiv-Simple': 'Simple',
    'BeDivFuzz-Simple': 'Simple',
    'BeDivFuzz': 'BeDivFuzz'
}

def select(data, **kwargs):
    for k, v in kwargs.items():
        data = data[data[k] == v]
    return data

def compute_sig_level(treatments, alpha=0.05):
    n = len(treatments)
    number_of_comparisons = 1 if n < 2 else n * (n - 1) / 2
    return alpha / number_of_comparisons

def mann_whitney(values1, values2):
    return scipy.stats.mannwhitneyu(values1, values2, alternative='two-sided', use_continuity=True)[1]

def plot_coverage(data, subject, cmap=None):
    # Define the specific order and colors
    fuzzers = sorted(data['fuzzer'].unique())
    plot_fuzzers = [f for f in fuzzers if 'Simple' not in f]
    plot_fuzzers = [fuzzer_map[f] for f in plot_fuzzers]
    legend_order = ["Random", "Zest-Mini", "Zest", "EI", "BeDivFuzz", "Zeugma"]
    colors = ['#4878CF', '#EE854A', '#D65F5F', '#59A14F', '#B279A2', '#BAB0AC']

    # Define different line styles for grayscale distinction
    line_styles = ['-.', (0, (3, 1, 1, 1)), ":", '--', (0, (5, 1)), '-']

    # Create custom color map
    custom_cmap = {k: v for k, v in zip(legend_order, colors)}

    cmap = custom_cmap
    lmap = {k: v for k, v in zip(legend_order, line_styles)}


    data = select(data, subject=subject)
    plt.rcParams["font.family"] = 'sans-serif'
    fig, ax = plt.subplots(figsize=(8, 4))

    stats = data.groupby(by=['time', 'fuzzer'])['covered_branches'] \
        .agg([min, max, 'median']) \
        .reset_index() \
        .sort_values('time')

    # Only include fuzzers that are in our legend order

    # Plot each fuzzer with its specific color and line style
    for i, fuzzer in enumerate(plot_fuzzers):
        color = cmap[fuzzer]
        linestyle = lmap[fuzzer]

        selected = stats[stats['fuzzer'] == fuzzer]
        times = (selected['time'] / pd.to_timedelta(1, 'm')).tolist()

        ax.plot(times, selected['median'], color=color, linestyle=linestyle,
                label=fuzzer, linewidth=2)
        ax.fill_between(times, selected['min'], selected['max'], color=color, alpha=0.2)

    # Add legend with custom order
    handles, labels = ax.get_legend_handles_labels()
    by_label = dict(zip(labels, handles))
    ordered_labels = [label for label in legend_order if label in labels]
    # Save just the legend as a separate file
    # Create a new figure for the legend
    # figlegend = plt.figure(figsize=(6, 6))

    # Create a list of artists and labels for the legend
    legend_handles = []
    for i, label in enumerate(legend_order):
        if label in plot_fuzzers:
            color = cmap[label]
            linestyle = lmap[label]
            line = plt.Line2D([0], [0], color=color, linestyle=linestyle, linewidth=2)
            legend_handles.append((line, label))

    # Create the standalone legend
    fig.legend([h for h, l in legend_handles],
                        [l for h, l in legend_handles],
                        loc='upper center', ncol=3, fontsize=14, bbox_to_anchor=(0.5, 1.2))

    # Remove the frame
    fig.set_frameon(False)
    # Save the legend

    ax.set_xlabel('Time (Minutes)', fontsize=18)
    ax.set_ylabel('Covered Branches', fontsize=18)
    ax.xaxis.get_major_locator().set_params(integer=True)
    ax.yaxis.get_major_locator().set_params(integer=True)
    ax.xaxis.set_tick_params(labelsize=14)
    ax.yaxis.set_tick_params(labelsize=14)

    ax.set_ylim(bottom=0)
    ax.set_xlim(left=0)
    ax.set_title(subject.title(), fontsize=20, fontweight='bold')
    fig.show()


def create_coverage_over_time_plots(data, output_dir):
    subjects = sorted(data['subject'].unique())
    fuzzers = sorted(data['fuzzer'].unique())
    fuzzers = [fuzzer_map[f] for f in fuzzers]
    cmap = {k[0]: k[1] for k in zip(fuzzers, [k for k in mcolors.TABLEAU_COLORS])}
    content = ''
    for subject in subjects:
        plot_coverage(data, subject, cmap)


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



def process_cov_data(corpus_size_df, time_df):
    """Load and process the two input CSV files."""
    print("Processing coverage data...")
    time_df['time'] = pd.to_timedelta(time_df['time'])


    corpus_size_df['time_to_execution_limit'] = pd.to_timedelta(corpus_size_df['time_to_execution_limit'], unit='s')
    corpus_size_df['normalized_execution_time'] = pd.to_timedelta(corpus_size_df['normalized_execution_time'], unit='s')
    rows = []

    for _, row in corpus_size_df.iterrows():
        campaign_id = row['campaign_id']

        rows.append({
            'subject': row['subject'],
            'fuzzer': convert_id_to_fuzzer(campaign_id),
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
    print("Aggregating time/trial-bounded coverage data...")
    aggregated = []
    for fuzzer in tqdm(df['fuzzer'].unique()):
        for subject in tqdm(df['subject'].unique()):
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
                sig_level = compute_sig_level(df['fuzzer'].unique())
                if (mann_whitney(baseline, values) < sig_level):
                    new_data[coverage + "_sig"] = 'color: red;'
                else:
                    new_data[coverage + "_sig"] = ''
            aggregated.append(new_data)
    return pd.DataFrame(aggregated)

def convert_id_to_fuzzer(campaign_id):
    campaign_id = campaign_id.lower()
    if "zeugma" in campaign_id and "link" in campaign_id:
        return "Zeugma"
    elif "zeugma" in campaign_id and "none" in campaign_id:
        return "Zeugma-None"
    elif "structure" in campaign_id:
        return "BeDivFuzz"
    elif "simple" in campaign_id:
        return "BeDivFuzz-Simple"
    elif "zest-mini" in campaign_id:
        return "Zest-Mini"
    elif "random" in campaign_id:
        return "Random"
    elif "ei" in campaign_id:
        return "EI"
    else:
        return "Zest"

def generate_cov_latex_table(data):
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