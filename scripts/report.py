import os
import pathlib
import sys

import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import pandas as pd

import extract
import report_util
import tables

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

TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        * {
            font-family: Open Sans, sans-serif;
            color: black;
        }

        h2 {
            font-size: 20px;
            font-weight: 550;
            display: block;
        }

        h3 {
            font-size: 12px;
            display: block;
        }

        img {
            max-width: 100%;
            max-height: calc((100vh - 100px) * 1 / 2);
            width: auto;
            height: auto;
            object-fit: contain;
        }

        .wrapper {
            display: flex;
            overflow-x: scroll;
            gap: 20px;
        }

        table * {
            font-size: 10px;
            font-weight: normal;
            text-align: right;
            padding: 5px;
        }

        table {
            border-bottom: black 1px solid;
            border-top: black 1px solid;
            border-collapse: collapse;
        }
    </style>
    <title>Fuzzing Report</title>
</head>
<body>
<div>
    $content
</div>
</body>
</html>
"""


def find_dataset(input_dir, name):
    file = os.path.join(input_dir, f'{name}.csv')
    print(f'Checking for {name} data: {file}.')
    if os.path.isfile(file):
        print(f'\t{name.title()} data found.')
        return report_util.read_timedelta_csv(file)
    else:
        print(f'\t{name.title()} data not found.')
        return None


def create_pairwise_subsection(frames, name=''):
    content = ''.join(t.to_html() for t in frames)
    return f'<div><h3>{name}Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def plot_coverage(data, subject, cmap=None, output_dir=None):
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


    data = report_util.select(data, subject=subject)
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
    figlegend = plt.figure(figsize=(6, 6))

    # Create a list of artists and labels for the legend
    legend_handles = []
    for i, label in enumerate(legend_order):
        if label in plot_fuzzers:
            color = cmap[label]
            linestyle = lmap[label]
            line = plt.Line2D([0], [0], color=color, linestyle=linestyle, linewidth=2)
            legend_handles.append((line, label))

    # Create the standalone legend
    figlegend.legend([h for h, l in legend_handles],
                        [l for h, l in legend_handles],
                        loc='center', ncol=3, fontsize=14)

    # Remove the frame
    figlegend.set_frameon(False)

    # Save the legend
    figlegend.savefig(os.path.join(output_dir, 'legend.pdf'), bbox_inches='tight')
    plt.close(figlegend)

    ax.set_xlabel('Time (Minutes)', fontsize=18)
    ax.set_ylabel('Covered Branches', fontsize=18)
    ax.xaxis.get_major_locator().set_params(integer=True)
    ax.yaxis.get_major_locator().set_params(integer=True)
    ax.xaxis.set_tick_params(labelsize=14)
    ax.yaxis.set_tick_params(labelsize=14)

    ax.set_ylim(bottom=0)
    ax.set_xlim(left=0)
    ax.set_title(subject.title(), fontsize=20, fontweight='bold')

    return fig

def create_plots_subsection(data, output_dir):
    subjects = sorted(data['subject'].unique())
    fuzzers = sorted(data['fuzzer'].unique())
    fuzzers = [fuzzer_map[f] for f in fuzzers]
    cmap = {k[0]: k[1] for k in zip(fuzzers, [k for k in mcolors.TABLEAU_COLORS])}
    content = ''
    cov_output_dir = os.path.join(output_dir, 'cov')
    if not os.path.exists(cov_output_dir):
        os.makedirs(cov_output_dir)
    for subject in subjects:
        plot_coverage(data, subject, cmap, cov_output_dir)
        # Save the figure to a file in scripts/figs/cov/
        fig_file = os.path.join(cov_output_dir, f"{subject.title()}.pdf")
        plt.savefig(fig_file, bbox_inches='tight')


def create_coverage_content(data, times, output_dir):
    create_plots_subsection(data, output_dir)
    cov_table = tables.create_coverage_table(data, times, output_dir)

def create_report(input_dir, output_dir):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(24, 'h')]
    coverage = find_dataset(input_dir, 'coverage')
    # detections = None
    assert coverage is not None, 'Coverage data not found.'
    coverage['fuzzer'] = coverage['fuzzer'].replace({
        "BeDiv-Struct": "BeDivFuzz",
        "Zeugma-Link": "Zeugma",
    })
    create_coverage_content(coverage, times, output_dir)

def main():
    create_report(sys.argv[1], sys.argv[2])

if __name__ == "__main__":
    main()
