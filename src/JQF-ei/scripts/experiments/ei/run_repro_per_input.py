from doctest import Example
import subprocess
from configs import *
import sys
import os
from pathlib import Path
from typing import List
from multiprocessing import Pool


EXAMPLES_DIR = os.path.join(Path(__file__).resolve().parent, "../../../examples")

def call(args: List[str]):
    for command in args:
        print(command)
        subprocess.check_call(command[0], cwd=command[1], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, shell=True)

def run(path: str, task: str):
    cpu = 1 if task == "perf" else 20
    with Pool(cpu) as pool:
        pool.map(call, generate_tasks(path, task))


def generate_tasks(base_path: str, mode: str):
    for dataset in DATASET:
        for algorithm in ALGORITHM:
            for generator in GENERATOR:
                for idx in range(20):
                    if "mix" in algorithm:
                        path = os.path.join(base_path, f"{dataset}-{algorithm}-{generator}-results-{idx}-tmp")
                        print(path)
                        if not os.path.exists(path):
                            continue
                        corpus_dir = os.path.join(path, "corpus")
                        yield f"JVM_OPTS=\"-Djqf.repro.logUniqueBranches=true -Djqf.repro.traceDir={path}\" " + \
                                f"{EXAMPLES_DIR}/../bin/jqf-repro -i -c $({EXAMPLES_DIR}/../scripts/experiments/../../scripts/examples_classpath.sh) " + \
                                f"{DATASET_TEST_CLASS_MAPPING[dataset]} {generator} " + \
                                f"{corpus_dir} 2> /dev/null | grep \"^# Cov\" | sort | uniq > {path}/cov-all.log"
                    path = os.path.join(base_path, f"{dataset}-{algorithm}-{generator}-results-{idx}")
                    if not os.path.exists(path):
                        continue
                    commands = []
                    if "zeugma" in path:
                        commands.append(
                            (f"mvn -pl :zeugma-evaluation-tools meringue:analyze -P{dataset},{algorithm} -Dmeringue.outputDirectory={path} -Dmeringue.duration=P2DT0H0M", "/usr0/home/aoli/repos/zeugma")
                        )
                        corpus_dir = os.path.join(path, "campaign", "gen")
                        cov_generator = "testWithInputStream"
                    elif "bedivfuzz" in path:
                        commands.append(
                            (f"mvn -pl :zeugma-evaluation-tools meringue:analyze -P{dataset},{algorithm} -Dmeringue.outputDirectory={path} -Dmeringue.duration=P2DT0H0M", "/usr0/home/aoli/repos/zeugma")
                        )
                        corpus_dir = os.path.join(path, "gen")
                        cov_generator = "testWithInputStream"
                    else:
                        corpus_dir = os.path.join(path, "corpus")
                        cov_generator = generator
                    commands.append(
                        (f"JVM_OPTS=\"-Djqf.repro.logUniqueBranches=true -Djqf.repro.traceDir={path}\" " + \
                            f"{EXAMPLES_DIR}/../bin/jqf-repro -i -c $({EXAMPLES_DIR}/../scripts/experiments/../../scripts/examples_classpath.sh) " + \
                            f"{DATASET_TEST_CLASS_MAPPING[dataset]} {cov_generator} " + \
                            f"{corpus_dir} 2> /dev/null | grep -a \"^# Cov\" | sort | uniq > {path}/cov-all.log", EXAMPLES_DIR
                         )
                    )
                    yield commands
                    #  yield "-Djqf.repro.logUniqueBranches=true"
                    #  yield ["mvn", "jqf:repro", "-Dengine=repro",
                            #  f"-Dclass={DATASET_TEST_CLASS_MAPPING[dataset]}",
                            #  "-Dmethod=testWithGenerator", f"-Dinput={corpus_dir}",
                            #  f"-DtraceDir={path}", f"-DlogCoverage={corpus_dir}/../tmp.log"]


if __name__ == "__main__":
    run(sys.argv[1], sys.argv[2])
