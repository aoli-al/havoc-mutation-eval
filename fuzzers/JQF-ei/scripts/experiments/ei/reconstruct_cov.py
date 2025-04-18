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
    print(args)
    if isinstance(args, str):
        subprocess.check_call(args, cwd=EXAMPLES_DIR, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, shell=True)
    else:
        subprocess.check_call(args, cwd=EXAMPLES_DIR, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def run(path: str, task: str):
    cpu = 1 if task == "perf" else 10
    with Pool(cpu) as pool:
        pool.map(call, generate_tasks(path, task))


def generate_tasks(base_path: str, mode: str):
    for dataset in DATASET:
        for algorithm in ALGORITHM:
            for idx in range(0, 10):
                path = os.path.join(base_path, f"{dataset}-{algorithm}-results-{idx}")
                if not os.path.exists(path):
                    break
                corpus_dir = os.path.join(path, "corpus")
                if mode == "perf":
                    if os.path.exists(os.path.join(path, "results.csv")):
                        continue
                    yield ["mvn", "jqf:repro", "-Dengine=repro",
                            f"-Dclass={DATASET_TEST_CLASS_MAPPING[dataset]}",
                            "-Dmethod=testWithGenerator", f"-Dinput={corpus_dir}",
                            f"-DtraceDir={path}", "-DuseFastNonCollidingCoverageInstrumentation=true"]
                else:
                    output_dir = os.path.join(path, "corpus_coverage")
                    if not os.path.exists(output_dir):
                        os.mkdir(output_dir)
                    for file_name in sorted(os.listdir(corpus_dir)):
                        input_path = os.path.realpath(os.path.join(corpus_dir, file_name))
                        output_path = os.path.realpath(os.path.join(output_dir, file_name + '.txt'))
                        #  if os.path.exists(output_path):
                            #  continue
                        yield f"JVM_OPTS=\"-Djqf.repro.logUniqueBranches=true -Xmx16g\" ../bin/jqf-repro -i " + \
                        f"-c $({EXAMPLES_DIR}/../scripts/examples_classpath.sh) {DATASET_TEST_CLASS_MAPPING[dataset]} " + \
                        f"testWithGenerator {input_path} 2>/dev/null | grep \"^# Cov\" | sort | uniq > {output_path}"


if __name__ == "__main__":
    run(sys.argv[1], sys.argv[2])
