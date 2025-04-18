from doctest import Example
import subprocess
from configs import *
import sys
import os
from pathlib import Path
from typing import List
from multiprocessing import Pool
import glob


EXAMPLES_DIR = os.path.join(Path(__file__).resolve().parent, "../../../examples")


def run(base_path: str):
    corpus_dir = os.path.join(base_path, "corpus")
    for f in glob.glob(os.path.join(corpus_dir, "id_*")):
        args = f"JVM_OPTS=\"-Djqf.repro.logUniqueBranches=true -Djqf.repro.traceDir=.\" " + \
                f"{EXAMPLES_DIR}/../bin/jqf-repro -i -c $({EXAMPLES_DIR}/../scripts/experiments/../../scripts/examples_classpath.sh) " + \
                f"{DATASET_TEST_CLASS_MAPPING['rhino']} testWithGenerator " + \
                f"{f}"
        print(args)
        out = subprocess.check_output(args, cwd=EXAMPLES_DIR, shell=True).decode("utf-8")
        if "visitDotQuery" in out:
            print(f)
            break
                    #  yield "-Djqf.repro.logUniqueBranches=true"
                    #  yield ["mvn", "jqf:repro", "-Dengine=repro",
                            #  f"-Dclass={DATASET_TEST_CLASS_MAPPING[dataset]}",
                            #  "-Dmethod=testWithGenerator", f"-Dinput={corpus_dir}",
                            #  f"-DtraceDir={path}", f"-DlogCoverage={corpus_dir}/../tmp.log"]


if __name__ == "__main__":
    run(sys.argv[1])
