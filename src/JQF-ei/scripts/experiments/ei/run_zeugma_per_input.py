from doctest import Example
import subprocess
from configs import *
import sys
import os
from pathlib import Path
from typing import List
from multiprocessing import Pool
import shutil

def call(args: List[str]):
    print(args)
    subprocess.check_call(args, cwd="/usr0/home/aoli/tmp/zeugma-main", stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, shell=True)

def run(path: str, task: str):
    cpu = 1 if task == "perf" else 20
    with Pool(cpu) as pool:
        pool.map(call, generate_tasks(path, task))


def generate_tasks(base_path: str, mode: str):
    for dataset in DATASET:
        for algorithm in ALGORITHM:
            for generator in GENERATOR:
                for idx in range(20):
                    path = os.path.join(base_path, f"{dataset}-{algorithm}-{generator}-results-{idx}")
                    if not os.path.exists(path):
                        continue

                    campaign_dir = os.path.join(path, "campaign")
                    corpus_dir = os.path.join(path, "corpus")
                    failure_dir = os.path.join(path, "failures")
                    if os.path.exists(campaign_dir):
                        shutil.rmtree(campaign_dir)
                        print(campaign_dir)
                    os.makedirs(campaign_dir, exist_ok=True)
                    shutil.copytree(corpus_dir, os.path.join(campaign_dir, "corpus"))
                    shutil.copytree(failure_dir, os.path.join(campaign_dir, "failures"))

                    yield f"mvn -pl :zeugma-evaluation-tools meringue:analyze -Pclosure,zest -Dmeringue.outputDirectory={path} "\
                        "-Dmeringue.duration=P0DT3H0M"

if __name__ == "__main__":
    run(sys.argv[1], sys.argv[2])
