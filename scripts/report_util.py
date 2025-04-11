import base64
from io import BytesIO
import os

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import scipy
from matplotlib.backends.backend_pgf import PdfPages

A12_BOUNDS = [0.56, 0.64, 0.71]
ODDS_RATIO_BOUNDS = [1.25, 1.5, 2.0]


def a12(values1, values2):
    """
    Returns Vargha-Delaney A12 statistic.
    Vargha, A., and Delaney, H. D. (2000).
    A Critique and Improvement of the "CL" Common Language Effect Size Statistics of McGraw and Wong.
    Journal of Educational and Behavioral Statistics, 25(2), 101–132.
    https://doi.org/10.2307/1165329
    """
    a = scipy.stats.mannwhitneyu(values1, values2)[0] / (len(values1) * len(values2))
    return 1 - a if a < 0.5 else a


def mann_whitney(values1, values2):
    return scipy.stats.mannwhitneyu(values1, values2, alternative='two-sided', use_continuity=True)[1]


def fisher_exact(truth_values1, truth_values2):
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    return scipy.stats.fisher_exact(table, alternative='two-sided')[1]


def odds_ratio(truth_values1, truth_values2):
    """
    Computes the odd ratio for two boolean lists of samples.
    Applies a modified Haldane-Anscombe correction as described by:
    Weber, F., Knapp, G., Ickstadt, K., Kundt, G., & Glass, Ä. (2020).
    Zero-cell corrections in random-effects meta-analyses.
    Research synthesis methods, 11(6), 913–919.
    https://doi.org/10.1002/jrsm.1460
    """
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    if 0 in table[0] or 0 in table[1]:
        table = [[x + 0.5 for x in row] for row in table]
    odds0 = table[0][0] / table[0][1]
    odds1 = table[1][0] / table[1][1]
    return odds0 / odds1 if odds0 > odds1 else odds1 / odds0


def compute_bucket(value, bounds):
    for i, bound in enumerate(bounds):
        if value < bound:
            return i
    return len(bounds)


def to_props(text_colors, background_colors):
    props = []
    for t_row, b_row in zip(text_colors, background_colors):
        props.append(f'color: {t}; background-color: {b};' for t, b in zip(t_row, b_row))
    return pd.DataFrame(props)


def compute_pairwise(data, x, y):
    f1, f2, bounds2, _ = get_stat_functions(data, y)
    unique_x = sorted(data[x].unique())
    sig_level = compute_sig_level(unique_x)
    text = [[x2 for x2 in unique_x] for _ in unique_x]
    text_colors = [['black' for _ in unique_x] for _ in unique_x]
    background_colors = [['white' for _ in unique_x] for _ in unique_x]
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            values1 = data[data[x] == x1][y]
            values2 = data[data[x] == x2][y]
            if r > c:
                p = f1(values1, values2)
                text[r][c] = f'{p:.2E}'
                text_colors[r][c] = 'black'
                background_colors[r][c] = '#8080ff' if p < sig_level else '#e6e6f0'
            elif r < c:
                e = f2(values1, values2)
                text[r][c] = f'{e:.3f}'
                bucket = compute_bucket(e, bounds2)
                text_colors[r][c] = ['black', 'black', 'black', 'white'][bucket]
                background_colors[r][c] = ['#f0e6e6', '#ff8080', '#ff0000', '#800000'][bucket]
    return text, text_colors, background_colors


def pairwise_heatmap(data, x, y, caption):
    text, text_colors, background_colors = compute_pairwise(data, x, y)
    return pd.DataFrame(text).style \
        .set_caption(caption) \
        .apply(lambda _: to_props(text_colors, background_colors), axis=None) \
        .hide(axis="index") \
        .hide(axis="columns")


def format_time_delta(time_delta):
    for unit in ['d', 'h', 'm', 's']:
        value = time_delta / pd.to_timedelta(1, unit)
        if value.is_integer():
            return f'{int(value)}{unit.upper()}'
    value = time_delta / pd.to_timedelta(1, 'm')
    return f'{round(value, 3)}M'


def fig_to_html(bbox_inches='tight'):
    buffer = BytesIO()
    plt.savefig(buffer, dpi=600, bbox_inches=bbox_inches, format='png')
    plt.close()
    return f'<img src="data:image/png;base64,{base64.b64encode(buffer.getvalue()).decode("utf-8")}">'


def fig_to_pdf(path, fig):
    plt.rcParams.update({
        "pgf.texsystem": "pdflatex",
        "font.family": "serif",
        "text.usetex": True,
        "pgf.rcfonts": False,
        "pgf.preamble": "\n".join([
            r"\usepackage[utf8x]{inputenc}",
            r"\usepackage[T1]{fontenc}",
            r"\usepackage{tikz}",
            r"\usepackage{pgfplots}",
            r"\usepackage{pgf}",
            r"\usepackage{libertine}",
            r"\usepackage[libertine]{newtxmath}",
        ])
    })
    with PdfPages(path) as pdf:
        pdf.savefig(fig)
    mpl.rcParams.update(mpl.rcParamsDefault)


def select(data, **kwargs):
    for k, v in kwargs.items():
        data = data[data[k] == v]
    return data


def get_stat_functions(data, y):
    if data[y].dtypes == bool:
        return fisher_exact, odds_ratio, ODDS_RATIO_BOUNDS, np.mean
    elif pd.api.types.is_numeric_dtype(data[y]):
        return mann_whitney, a12, A12_BOUNDS, np.median
    else:
        raise ValueError


def compute_sig_level(treatments, alpha=0.05):
    n = len(treatments)
    number_of_comparisons = 1 if n < 2 else n * (n - 1) / 2
    return alpha / number_of_comparisons


def read_timedelta_csv(file, column='time'):
    df = pd.read_csv(file)
    if column in df.columns:
        df[column] = pd.to_timedelta(df[column])
    return df

def plot_legend(cmap, columns, cov_output_dir):
    del cmap['Simple']
    # Create handles for each legend item
    handles = [plt.plot([], [], marker="s", markersize=15, color=v, ls="none", label=k)[0] for k, v in cmap.items()]
    
    # Create figure with appropriate size
    # Using number of items instead of columns for the height, since each item will be on its own line
    num_items = len(cmap)
    fig, ax = plt.subplots(figsize=(columns, num_items * 0.5))
    ax.axis('off')
    
    # Create legend with one item per row (ncol=1)
    # Increase font size with prop parameter
    legend = ax.legend(
        handles=handles, 
        frameon=False, 
        loc='center left', 
        ncol=1,  # One item per column (vertical list)
        prop={'size': 14},  # Increase font size
        labelspacing=1.2  # Add more space between legend items
    )
    
    # # Increase marker size in the legend
    # for handle in legend.legend_handles:
    #     handle._legmarker.set_markersize(15)
    
    # Save the figure
    fig = legend.figure
    fig.canvas.draw()
    bbox = legend.get_window_extent().transformed(fig.dpi_scale_trans.inverted())
    plt.savefig(os.path.join(cov_output_dir, "legend.pdf"), bbox_inches=bbox, dpi=600, transparent=True)