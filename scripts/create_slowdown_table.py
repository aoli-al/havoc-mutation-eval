from scipy.stats import gstd
import pandas as pd
import numpy as np
import math
import re

def extract_repetition(campaign_id):
    """Extract the repetition number from campaign_id."""
    return int(campaign_id.split('-')[-1])

def geometric_mean(values):
    """Calculate the geometric mean of a list of values."""
    if not values:
        return 0
    return math.exp(sum(math.log(x) for x in values) / len(values))

def analyze_fuzzer_slowdown(csv_file):
    """
    Analyze fuzzer runtime slowdown from campaign trials data.

    Args:
        csv_file: Path to the CSV file with campaign trials data

    Returns:
        DataFrame with geometric mean slowdowns and standard deviations
    """
    # Read the CSV file
    df = pd.read_csv(csv_file)

    # Extract repetition number from campaign_id
    df['repetition'] = df['campaign_id'].apply(extract_repetition)

    # Create a pivot table with benchmark, repetition, and technique
    pivot_df = df.pivot_table(
        index=['benchmark', 'repetition'],
        columns='technique',
        values='executions',
        aggfunc='first'
    )

    # Calculate slowdowns for each benchmark and repetition
    results = []

    for benchmark, group in pivot_df.groupby(level=0):
        # For each technique, calculate the slowdown against its baseline

        # EI vs Zest
        ei_slowdowns = []
        for rep, row in group.iterrows():
            if pd.notna(row.get('ei')) and pd.notna(row.get('zest')):
                slowdown = row['ei'] / row['zest']
                ei_slowdowns.append(slowdown)

        # BeDivFuzz vs Zest
        bedivfuzz_slowdowns = []
        for rep, row in group.iterrows():
            if pd.notna(row.get('bedivfuzz')) and pd.notna(row.get('zest')):
                slowdown = row['bedivfuzz'] / row['zest']
                bedivfuzz_slowdowns.append(slowdown)

        # Zeugma vs zeugma-none
        zeugma_slowdowns = []
        for rep, row in group.iterrows():
            if pd.notna(row.get('zeugma')) and pd.notna(row.get('zeugma-none')):
                slowdown = row['zeugma'] / row['zeugma-none']
                zeugma_slowdowns.append(slowdown)

        # Calculate geometric means and standard deviations
        if ei_slowdowns:
            ei_geo_mean = geometric_mean(ei_slowdowns)
            ei_std_dev = gstd(ei_slowdowns)
            results.append({
                'benchmark': benchmark,
                'technique': 'EI',
                'geo_mean': ei_geo_mean,
                'std_dev': ei_std_dev,
                'count': len(ei_slowdowns)
            })

        if bedivfuzz_slowdowns:
            bedivfuzz_geo_mean = geometric_mean(bedivfuzz_slowdowns)
            bedivfuzz_std_dev = gstd(bedivfuzz_slowdowns)
            results.append({
                'benchmark': benchmark,
                'technique': 'BeDivFuzz',
                'geo_mean': bedivfuzz_geo_mean,
                'std_dev': bedivfuzz_std_dev,
                'count': len(bedivfuzz_slowdowns)
            })

        if zeugma_slowdowns:
            zeugma_geo_mean = geometric_mean(zeugma_slowdowns)
            zeugma_std_dev = gstd(zeugma_slowdowns)
            results.append({
                'benchmark': benchmark,
                'technique': 'Zeugma',
                'geo_mean': zeugma_geo_mean,
                'std_dev': zeugma_std_dev,
                'count': len(zeugma_slowdowns)
            })

    # Convert to DataFrame
    result_df = pd.DataFrame(results)

    result_df.to_csv("slowdown.csv")

    return result_df

def generate_latex_table(result_df):
    """
    Generate a LaTeX table from the analysis results with color coding.

    Args:
        result_df: DataFrame with analysis results

    Returns:
        String containing the LaTeX table with color-coded cells
    """
    # Pivot the results for easier table generation
    table_df = result_df.pivot(
        index='benchmark',
        columns='technique',
        values=['geo_mean', 'std_dev']
    )

    # Sort benchmarks alphabetically
    table_df = table_df.sort_index()

    # Add LaTeX package requirements in a comment
    latex_requirements = "% Requires \\usepackage{xcolor} in the preamble"

    # Start building the LaTeX table
    latex_table = [
        latex_requirements,
        "\\begin{table}[t]",
        "\\centering",
        "\\scriptsize",
        "\\caption{Runtime Slowdown by Benchmark and Technique (compared to baseline)}",
        "\\begin{tabular}{l|ccc}",
        "\\toprule",
        "\\textbf{Benchmark} & \\textbf{EI} & \\textbf{BeDivFuzz} & \\textbf{Zeugma} \\\\",
        "\\midrule"
    ]

    # Function to generate color based on value
    def get_color_cmd(value):
        if value < 1:
            # Red for values below 1 (faster than baseline)
            # Intensity increases as value approaches 0
            intensity = max(0, min(100, int(100 * (1 - value))))
            return f"\\cellcolor{{red!{intensity}!white}}"
        else:
            # Green for values above 1 (slower than baseline)
            # Intensity increases as value gets larger, capped at 100%
            intensity = max(0, min(100, int(50 * (value - 1))))
            return f"\\cellcolor{{green!{intensity}!white}}"

    # Add a row for each benchmark
    for benchmark in table_df.index:
        row = f"{benchmark}"

        for technique in ['EI', 'BeDivFuzz', 'Zeugma']:
            if (technique in table_df['geo_mean'].columns and
                pd.notna(table_df['geo_mean'][technique][benchmark])):

                mean = table_df['geo_mean'][technique][benchmark]
                std = table_df['std_dev'][technique][benchmark]
                color_cmd = get_color_cmd(mean)
                row += f" & $\\footnotesize{color_cmd}{mean:.2f}\\times {std:.2f}$" + r"$^{\pm 1}$"
            else:
                row += " & -"

        row += " \\\\"
        latex_table.append(row)

    # Finish the table
    latex_table.extend([
        "\\bottomrule",
        "\\end{tabular}",
        "\\label{tab:runtime_slowdown}",
        "\\end{table}"
    ])
    return "\n".join(latex_table)


def main():
    # File path
    csv_file = "tmp/out_trials/campaign_trials_detail.csv"

    # Analyze data
    print("Analyzing fuzzer runtime slowdown...")
    result_df = analyze_fuzzer_slowdown(csv_file)

    # Print summary of results
    print("\nSummary of Results (Geometric Mean Slowdowns):")
    for benchmark, group in result_df.groupby('benchmark'):
        print(f"\nBenchmark: {benchmark}")
        for _, row in group.iterrows():
            print(f"  {row['technique']}: {row['geo_mean']:.2f} ± {row['std_dev']:.2f} (from {row['count']} repetitions)")

    # Generate and print LaTeX table
    print("\nLaTeX Table:")
    latex_table = generate_latex_table(result_df)
    print(latex_table)

if __name__ == "__main__":
    main()
