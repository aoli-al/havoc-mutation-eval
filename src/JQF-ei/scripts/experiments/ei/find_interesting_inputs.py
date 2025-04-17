import os
from typing import Optional
from configs import *
from visualize import *
import re

def build_corpus_map(path: str) -> Dict[str, str]:
    pattern = re.compile(r"\[\d+\] Saved.*corpus/(id_\d+) src:(\d+),.*")
    mapping = {}
    with open(os.path.join(path, "fuzz.log")) as f:
        for line in f:
            result = pattern.match(line)
            if result:
                mapping[result.group(1)] = "id_" + result.group(2)
    return mapping

def process(base_dir: str):
    DATASET = ['rhino']
    for dataset in DATASET:
        only_algo_cov_data = set([item[11:] for item in process_cov_data(os.path.join(base_dir, "processed", f"{dataset}-only-zest-fast-cov-all.txt"))])
        for i in range(10):
            expreiment_folder = os.path.join(
                base_dir, f"{dataset}-zest-fast-results-{i}")
            corpus_coverage_folder = os.path.join(expreiment_folder, "corpus_coverage")
            if not os.path.exists(corpus_coverage_folder):
                continue

            mapping = build_corpus_map(expreiment_folder)
            for item in sorted(os.listdir(corpus_coverage_folder)):
                index = item.split(".")[0]
                if index not in mapping:
                    continue

                cov_data = process_cov_data(os.path.join(corpus_coverage_folder, item))
                parent_cov_data = process_cov_data(os.path.join(corpus_coverage_folder, mapping[index] + ".txt"))
                intersection = cov_data.intersection(only_algo_cov_data) - parent_cov_data
                if len(intersection) > 10:
                    print("=======================")
                    print(corpus_coverage_folder)
                    print("Index: ", index)
                    print("Parent: ", mapping[index])
                    print(len(intersection))
                    print("".join(intersection))
                    print("=======================")


if __name__ == "__main__":
    process(sys.argv[1])

