#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
from configs import ALGORITHM


def get_last_line_of_file(file_path):
    with open(file_path, 'rb') as f:
        f.seek(-2, os.SEEK_END)
        while f.read(1) != b'\n':
            f.seek(-2, os.SEEK_CUR)
        last_line = f.readline().decode()
    return last_line

def get_last_lines_from_statistics(folder_path, algo):
    last_lines = {}
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if "zeugma" in algo:
                file_name = "statistics.csv"
            else:
                file_name = "plot_data"
            if file == file_name:
                file_path = os.path.join(root, file)
                if algo not in file_path:
                    continue
                last_line = get_last_line_of_file(file_path)
                last_lines[file_path] = last_line
    return last_lines

# Specify the folder path
#  folder_path = '/data/aoli/results-JQF/closure-24h'

for algo in ALGORITHM:
    print(algo)
    last_lines = get_last_lines_from_statistics(sys.argv[1], algo)
    if not last_lines:
        continue
    total_execution = 0
    count = 0
    for file_path, last_line in last_lines.items():
        count += 1
        if "zeugma" in algo:
            total_execution += int(last_line.split(",")[3])
        elif "bedivfuzz" in algo:
            valid_input = int(last_line.split(", ")[4])
            total_execution += valid_input
        else:
            valid_input = int(last_line.split(", ")[11])
            invalid_input = int(last_line.split(", ")[12])
            total_execution += invalid_input + valid_input
    print(algo, total_execution / count)
