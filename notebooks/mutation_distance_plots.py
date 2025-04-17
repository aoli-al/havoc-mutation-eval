import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
import tqdm

from matplotlib.ticker import ScalarFormatter


def plot_mut_distance_scatter(df, benchmark_name, labels=False):
    """
    Create scatter plots of mutation distances arranged in 2 rows with 3 plots per row.
    Includes a y=x reference line instead of a regression line.

    Parameters:
    df (pandas.DataFrame): DataFrame containing mutation data with columns:
        - mutation_bytes
        - mutation_string
        - algorithm
        - benchmark_name
    benchmark_name (str): Name of the benchmark to plot

    Returns:
    matplotlib.figure.Figure: The generated plot
    """
    # Filter data for the specified benchmark
    plot_data = df[df['benchmark_name'] == benchmark_name].copy()
    plt.style.use("default")

    # Get unique algorithms
    algorithms = plot_data['algorithm'].unique()
    num_algorithms = len(algorithms)

    # Define a color palette
    colors = ['#4878CF', '#EE854A', '#D65F5F', '#59A14F', '#B279A2', '#BAB0AC']

    # Calculate rows and columns for the subplots
    rows = 2
    cols = 3

    # Create subplots with 2 rows and 3 columns
    fig, axes = plt.subplots(rows, cols, figsize=(18, 10), sharex=True, sharey=True)

    fig.suptitle(f'Mutation Distance of each Technique ({benchmark_name})', fontsize=24, y=0.98)

    # Flatten the axes array for easier iteration
    axes = axes.flatten()

    # Plot each algorithm separately with a different color
    for i, (algo, color) in enumerate(zip(algorithms, colors)):
        if i < len(axes):  # Ensure we don't go beyond available axes
            ax = axes[i]
            algo_data = plot_data[plot_data['algorithm'] == algo]
            
            # Plain scatter plot instead of regplot
            ax.scatter(
                algo_data['mutation_bytes'],
                algo_data['mutation_string'],
                s=1, 
                alpha=0.5, 
                color=color
            )
            
            # Add y=x line (slope=1, intercept=0)
            x_vals = np.array([0, 1])  # Create x values for the line
            ax.plot(x_vals, x_vals, color=color, linestyle='--', linewidth=1.5)
            
            ax.set_title(f'{algo}', fontsize=20)
            ax.set_xlabel('Mutation Distance (bytes)', fontsize=18)
            ax.set_ylabel('Mutation Distance (string)', fontsize=18)
            ax.xaxis.set_tick_params(labelsize=14)
            ax.yaxis.set_tick_params(labelsize=14)
            ax.set_ylim(0, 1)
            ax.set_xlim(0, 1)
            
            # Add special marker and vertical line for the second subplot
            if labels:
                if i == 1:  # Second subplot (index 1)
                    # Add a highlighted point in the middle area
                    highlight_x, highlight_y = 0.3, 0.7  # Coordinates for the highlighted point
                    ax.scatter(highlight_x, highlight_y, s=50, color=color, edgecolor='black', zorder=10)
                    
                    # Add vertical line down to the y=x reference line
                    intersection_x = highlight_x  # approximate intersection with y=x line
                    ax.plot([highlight_x, highlight_x], [highlight_y, intersection_x], 
                           color='black', linewidth=1.5, zorder=9)
                    
                    # Add delta symbol as label
                    ax.text(highlight_x + 0.02, (highlight_y + intersection_x) / 2, 
                           r'$\Delta$', fontsize=18, ha='left', va='center')
                
                if i == 2:  # Third subplot (index 2)
                    # Add arrow pointing to top left region, starting from outside the plot
                    ax.annotate('Havoc Effect', 
                               xy=(0.2, 0.8),  # Arrow points here (target)
                               xytext=(1.2, 0.9),  # Text position outside the plot
                               fontsize=16,
                               arrowprops=dict(
                                   facecolor='black',
                                   shrink=0.05,
                                   width=1.5,
                                   headwidth=8,
                                   connectionstyle="arc3,rad=-0.3"  # Curved arrow
                               ))

    # Hide any unused subplots
    for j in range(i+1, len(axes)):
        axes[j].set_visible(False)

    # Adjust layout
    plt.tight_layout()
    return fig

def plot_mutation_dist_heatmap(df, havoc_only=False, figsize=(12, 8), cmap='OrRd', 
                              save_path=None, dpi=300):
    """
    Create a heatmap of summed mutation_distance_diff grouped by algorithm and benchmark_name.
    
    Parameters:
    -----------
    df : pandas.DataFrame
        DataFrame containing the mutation data with columns:
        'algorithm', 'benchmark_name', 'mutation_distance_diff', 'mutation_string', 'mutation_bytes'
    havoc_only : bool, default=False
        If True, only include rows where mutation_distance_string > mutation_distance_bytes
    figsize : tuple, default=(12, 8)
        Figure size as (width, height) in inches
    cmap : str, default='OrRd'
        Colormap for the heatmap
    save_path : str, default=None
        If provided, save the figure to this path
    dpi : int, default=300
        DPI for saved figure
        
    Returns:
    --------
    matplotlib.figure.Figure
        The figure object containing the heatmap
    """
    # Create a copy to avoid modifying the original dataframe
    data = df.copy()
    
    # Filter for havoc_only if requested
    if havoc_only:
        data = data[data['mutation_string'] > data['mutation_bytes']]
    
    # Step 1: Group by algorithm and benchmark_name, then sum the mutation_distance_diff
    heatmap_data = data.groupby(['algorithm', 'benchmark_name'])['mutation_distance_diff'].mean().reset_index()
    
    # Step 2: Pivot the data for the heatmap
    pivot_data = heatmap_data.pivot(index='algorithm', columns='benchmark_name', values='mutation_distance_diff')
    
    # Create a custom annotation array to display "N/A" for NaN values and format numbers with one decimal
    annot_array = pivot_data.copy()
    
    # Function to format each cell: keep "N/A" for NaNs and format numbers with one decimal place
    def format_cell(val):
        if pd.isna(val):
            return "N/A"
        else:
            return f"{val:.1f}"  # Format with one decimal place
            
    # Apply formatting function to each cell
    annot_array = annot_array.applymap(format_cell)
    
    # Order the algorithms as specified
    algorithm_order = ['Random', 'Zest-Mini', 'Zest', 'EI', 'BeDivFuzz', 'Zeugma'][::-1]
    
    # Filter algorithm_order to include only algorithms present in the data
    available_algorithms = [algo for algo in algorithm_order if algo in pivot_data.index]   
    pivot_data = pivot_data.reindex(available_algorithms)

    # Step 3: Create the heatmap
    plt.figure(figsize=figsize)
    
    # Mask for NaN values - we'll use this to show "N/A" text
    mask = pivot_data.isna()
    
    # Create the heatmap with seaborn
    title_suffix = " (Positive only)" if havoc_only else ""
    ax = sns.heatmap(pivot_data, 
                    annot=True,          # Show values in cells
                    fmt='.1f',           # Format with 1 decimal place
                    cmap=cmap,           # Color map
                    linewidths=.5,       # Add lines between cells
                    annot_kws={"size": 20},  # Increase font size of cell values
                    cbar_kws={'label': 'Average of Mutation Distance Difference'},
                    mask=None)           # Don't mask any cells
    
    # Now add "N/A" text to the NaN cells manually
    for i, idx in enumerate(pivot_data.index):
        for j, col in enumerate(pivot_data.columns):
            if mask.loc[idx, col]:
                # Add "N/A" text to this cell
                ax.text(j + 0.5, i + 0.5, "N/A", 
                       ha="center", va="center",
                       fontsize=16)  # Match the size with annot_kws
    
    # Improve the plot appearance
    ax.set_title('Normalized Mutation Distance Differences ' + r'($\Delta$) ' + f'{title_suffix}', fontsize=26)
    ax.grid(False)
    
    # The correct axis labels - algorithms are on Y-axis, benchmarks are on X-axis
    ax.set_ylabel('Technique', fontsize=24, labelpad=20)  # Y-axis is for algorithms
    ax.set_xlabel('Benchmark', fontsize=24)  # X-axis is for benchmarks
    
    ax.xaxis.set_tick_params(labelsize=14)
    ax.yaxis.set_tick_params(labelsize=14)
    
    # Rotate x-axis labels if they overlap
    plt.xticks(rotation=45, ha='right')
    
    # Add some padding to avoid cutting off labels
    plt.tight_layout()
    
    # Save the figure if requested
    if save_path:
        plt.savefig(save_path, dpi=dpi, bbox_inches='tight')
    
    # Return the figure object
    fig = plt.gcf()
    return fig


def create_zero_mutation_plot(df):
    # Set the style to match the example
    sns.set_style("whitegrid")

    # Filter to include only rows where parent_result == 'SUCCESS'
    filtered_df = df[df['parent_result'] == 'SUCCESS']

    # Calculate the percentage of zero mutations for each benchmark and algorithm
    zero_rates = []
    for benchmark in filtered_df['benchmark_name'].unique():
        benchmark_df = filtered_df[filtered_df['benchmark_name'] == benchmark]

        for algorithm in benchmark_df['algorithm'].unique():
            algo_df = benchmark_df[benchmark_df['algorithm'] == algorithm]

            # Calculate zero mutation rate
            total_rows = len(algo_df)
            zero_rows = len(algo_df[algo_df['mutation_string'] == 0])

            if total_rows > 0:
                zero_rate = zero_rows / total_rows * 100
            else:
                zero_rate = 0

            zero_rates.append({
                'benchmark_name': benchmark,
                'algorithm': algorithm,
                'zero_rate': zero_rate,
                'total_rows': total_rows,
                'zero_rows': zero_rows
            })

    # Convert to DataFrame
    zero_df = pd.DataFrame(zero_rates)

    # Create the plot
    fig, ax = plt.subplots(figsize=(14, 6))

    # Get unique benchmarks and use the specified algorithm order
    benchmarks = zero_df['benchmark_name'].unique()
    algorithms = ["Random", "Zest-Mini", "Zest", "EI", "BeDivFuzz", "Zeugma"]

    # Set up the positions for grouped bars
    x = np.arange(len(benchmarks))
    width = 0.14  # Width of each bar

    # Define hatching patterns for bars (similar to the example)
    hatches = ['', '///', '\\\\\\', '|||', 'xxx', '...']

    # Define color palette (adjust to match example more closely)
    colors = ['#4878CF', '#EE854A', '#D65F5F', '#59A14F', '#B279A2', '#BAB0AC']

    # Plot each algorithm group
    for i, algorithm in enumerate(algorithms):
        if algorithm in zero_df['algorithm'].values:
            mask = zero_df['algorithm'] == algorithm
            values = [zero_df[mask & (zero_df['benchmark_name'] == b)]['zero_rate'].values[0]
                     if any(mask & (zero_df['benchmark_name'] == b)) else 0
                     for b in benchmarks]

            bars = ax.bar(x + (i - 2.5) * width, values, width,
                   label=algorithm, color=colors[i])

            # Add hatching to bars
            for bar, hatch in zip(bars, [hatches[i]] * len(bars)):
                bar.set_hatch(hatch)

            # Add data points on top of bars
            for j, val in enumerate(values):
                if val > 0:  # Only add points for non-zero values
                    ax.plot(x[j] + (i - 2.5) * width, val, 'o', color='black', markersize=3)

    # Customize the plot
    ax.set_xticks(x)
    ax.set_xticklabels(benchmarks)
    ax.set_xlabel('Benchmark', fontsize=24)
    ax.set_ylabel('Zero Mutation Rate (%)', fontsize=24)
    ax.set_title('Zero String Mutations for each Technique', fontsize=28)
    ax.xaxis.set_tick_params(labelsize=18)
    ax.yaxis.set_tick_params(labelsize=18)

    # Add benchmark counts like in the example (ChocoPy (4856), etc.)
    benchmark_counts = {}
    for benchmark in benchmarks:
        count = len(filtered_df[filtered_df['benchmark_name'] == benchmark])
        benchmark_counts[benchmark] = count

    # labels_with_counts = [f"{b}\n({benchmark_counts[b]})" for b in benchmarks]
    # ax.set_xticklabels(labels_with_counts)

    # Set y-axis limits
    ax.set_ylim(0, 80)  # Set to 105 to leave room for data points at the top

    # Add a legend
    ax.legend(loc='upper center',
              ncol=len(algorithms), frameon=True, fontsize=18)

    plt.tight_layout()
    return plt, zero_df


def create_saved_all_ratio_plot(df, output_filename=None):
    """
    Generate a bar plot comparing the ratio of Saved/All mutation distances
    for each technique and benchmark.
    
    Parameters:
    df (pandas.DataFrame): DataFrame containing mutation data with 'saved' column
    output_filename (str, optional): If provided, save the plot to this filename
    
    Returns:
    matplotlib.pyplot: The plot object
    pandas.DataFrame: The calculated ratios data
    """
    # Set the style to match the example
    sns.set_style("whitegrid")
    
    # Create a copy of the dataframe to avoid modifying the original
    plot_df = df.copy()
    
    # Multiply mutation_string values by 100 to convert to percentages
    plot_df['mutation_string'] = plot_df['mutation_string'] * 100
    
    # List of algorithms and benchmarks
    algorithms = ['Random', 'Zest-Mini', 'Zest', 'EI', 'BeDivFuzz', 'Zeugma']
    benchmarks = sorted(plot_df['benchmark_name'].unique())
    
    # Create a list to store the ratio data
    ratio_data = []
    
    # Calculate ratio values for each combination
    for benchmark in benchmarks:
        for algorithm in algorithms:
            # Filter for current algorithm and benchmark
            algo_bench_data = plot_df[(plot_df['algorithm'] == algorithm) & 
                                     (plot_df['benchmark_name'] == benchmark)]
            
            if algo_bench_data.empty:
                continue
                
            # Calculate median for all data
            median_all = algo_bench_data['mutation_string'].median()
            
            # Get saved data (where saved column is True)
            saved_data = algo_bench_data[algo_bench_data['saved'] == True]
            median_saved = saved_data['mutation_string'].median() if not saved_data.empty else np.nan
            
            # Calculate ratio (avoid division by zero)
            if median_all > 0 and not np.isnan(median_saved):
                ratio = median_saved / median_all
            else:
                ratio = np.nan
            
            # Store the results
            ratio_data.append({
                'benchmark_name': benchmark,
                'algorithm': algorithm,
                'median_all': median_all,
                'median_saved': median_saved,
                'ratio': ratio
            })
    
    # Convert to DataFrame
    ratio_df = pd.DataFrame(ratio_data)
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(14, 6))
    
    # Get unique benchmarks
    x = np.arange(len(benchmarks))
    width = 0.14  # Width of each bar
    
    # Define hatching patterns for bars
    hatches = ['', '///', '\\\\\\', '|||', 'xxx', '...']
    
    # Define color palette (matching the example)
    colors = ['#4878CF', '#EE854A', '#D65F5F', '#59A14F', '#B279A2', '#BAB0AC']
    
    # Plot each algorithm group
    for i, algorithm in enumerate(algorithms):
        if algorithm in ratio_df['algorithm'].values:
            mask = ratio_df['algorithm'] == algorithm
            values = [ratio_df[mask & (ratio_df['benchmark_name'] == b)]['ratio'].values[0]
                     if any(mask & (ratio_df['benchmark_name'] == b)) else np.nan
                     for b in benchmarks]
            
            # Replace NaN with 0 for plotting
            plot_values = [0 if np.isnan(v) else v for v in values]
            
            bars = ax.bar(x + (i - 2.5) * width, plot_values, width,
                   label=algorithm, color=colors[i])
            
            # Add hatching to bars
            for bar, hatch in zip(bars, [hatches[i]] * len(bars)):
                bar.set_hatch(hatch)
            
            # Add data points on top of bars for non-zero and non-NaN values
            for j, (val, plot_val) in enumerate(zip(values, plot_values)):
                if plot_val > 0 and not np.isnan(val):
                    ax.plot(x[j] + (i - 2.5) * width, plot_val, 'o', color='black', markersize=3)
    
    # Customize the plot
    ax.set_xticks(x)
    ax.set_xticklabels(benchmarks)
    ax.set_xlabel('Benchmark', fontsize=24)
    ax.set_ylabel('Ratio (log scale)', fontsize=24)
    ax.set_title('Saved/All Mutation Distance Ratio', fontsize=28)
    ax.xaxis.set_tick_params(labelsize=18)
    ax.yaxis.set_tick_params(labelsize=18)
    
    # Add a horizontal line at y=1 to indicate where saved = all
    ax.axhline(y=1, color='black', linestyle='--', alpha=0.5)
    
    # Use log scale for y-axis
    ax.set_yscale('log')
    
    # Set y-axis limits for the log scale
    min_ratio = ratio_df['ratio'].min()
    max_ratio = ratio_df['ratio'].max()
    
    if not np.isnan(min_ratio) and not np.isnan(max_ratio):
        # Set bottom limit to 0.1 or 10% below minimum, whichever is lower
        bottom_limit = min(0.1, min_ratio * 0.9) if min_ratio > 0 else 0.1
        # Set top limit to 10 or 10% above maximum, whichever is higher
        top_limit = max(10, max_ratio * 1.1)
        ax.set_ylim(bottom_limit, top_limit)
    else:
        ax.set_ylim(0.1, 10)  # Default if no valid ratios
        
    # Format y-axis tick labels as numbers instead of scientific notation
    formatter = ScalarFormatter()
    formatter.set_scientific(False)
    ax.yaxis.set_major_formatter(formatter)
    
    # Add a legend
    ax.legend(loc='upper center', ncol=len(algorithms), frameon=True, fontsize=18)
    
    plt.tight_layout()
    
    # Save the figure if output_filename is provided
    if output_filename:
        plt.savefig(output_filename, bbox_inches='tight')
    
def create_success_rate_chart(df, filter_zero=True):
    # Set the style to match the example
    sns.set_style("whitegrid")

    # Filter to include only rows where parent_result == 'SUCCESS'
    filtered_df = df[df['parent_result'] == 'SUCCESS']

    if filter_zero:
        filtered_df = filtered_df[filtered_df["mutation_string"] != 0]

    # Calculate the success rate for each benchmark and algorithm
    success_rates = []
    for benchmark in filtered_df['benchmark_name'].unique():
        benchmark_df = filtered_df[filtered_df['benchmark_name'] == benchmark]

        for algorithm in benchmark_df['algorithm'].unique():
            algo_df = benchmark_df[benchmark_df['algorithm'] == algorithm]

            # Calculate success rate
            total_rows = len(algo_df)
            success_rows = len(algo_df[algo_df['result'] == 'SUCCESS'])

            if total_rows > 0:
                success_rate = success_rows / total_rows * 100
            else:
                success_rate = 0

            success_rates.append({
                'benchmark_name': benchmark,
                'algorithm': algorithm,
                'success_rate': success_rate,
                'total_rows': total_rows,
                'success_rows': success_rows
            })

    # Convert to DataFrame
    success_df = pd.DataFrame(success_rates)

    # Create the plot
    fig, ax = plt.subplots(figsize=(14, 6))

    # Get unique benchmarks and algorithms
    # benchmarks = ['ant', 'maven', 'rhino', 'closure']
    benchmarks = df['benchmark_name'].unique()
    algorithms = ["Random", "Zest-Mini", "Zest", "EI", "BeDivFuzz", "Zeugma"]

    # Set up the positions for grouped bars
    x = np.arange(len(benchmarks))
    width = 0.14  # Width of each bar

    # Define hatching patterns for bars (similar to the example)
    hatches = ['', '///', '\\\\\\', '|||', 'xxx', '...']

    # Define color palette (adjust to match example more closely)
    colors = ['#4878CF', '#EE854A', '#D65F5F', '#59A14F', '#B279A2', '#BAB0AC']

    # Plot each algorithm group
    for i, algorithm in enumerate(algorithms):
        if algorithm in success_df['algorithm'].values:
            mask = success_df['algorithm'] == algorithm
            values = [success_df[mask & (success_df['benchmark_name'] == b)]['success_rate'].values[0]
                     if any(mask & (success_df['benchmark_name'] == b)) else 0
                     for b in benchmarks]

            bars = ax.bar(x + (i - 2.5) * width, values, width,
                   label=algorithm, color=colors[i])

            # Add hatching to bars
            for bar, hatch in zip(bars, [hatches[i]] * len(bars)):
                bar.set_hatch(hatch)

            # Add data points on top of bars
            for j, val in enumerate(values):
                if val > 0:  # Only add points for non-zero values
                    ax.plot(x[j] + (i - 2.5) * width, val, 'o', color='black', markersize=3)

    # Customize the plot
    ax.set_xticks(x)
    ax.set_xticklabels(benchmarks)
    ax.set_xlabel('Benchmark', fontsize=24)
    ax.set_ylabel('Percent of Validity \n Preserving Mutations (%)', fontsize=24)
    ax.set_title('Validity Preserving Mutations of each Technique', fontsize=28)

    # Add benchmark counts like in the example (ChocoPy (4856), etc.)
    benchmark_counts = {}
    for benchmark in benchmarks:
        count = len(filtered_df[filtered_df['benchmark_name'] == benchmark])
        benchmark_counts[benchmark] = count

    labels_with_counts = [f"{b}\n({benchmark_counts[b]})" for b in benchmarks]
    ax.xaxis.set_tick_params(labelsize=18)
    ax.yaxis.set_tick_params(labelsize=18)

    # Set y-axis limits
    ax.set_ylim(0, 105)  # Set to 105 to leave room for data points at the top

    # Add a legend
    ax.legend(loc='upper center',
              ncol=len(algorithms), frameon=True, fontsize=18)

    plt.tight_layout()
    return plt, success_df
