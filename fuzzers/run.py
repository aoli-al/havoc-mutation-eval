from multiprocessing import Pool
import subprocess
import argparse
import shutil
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

CONFIGURATIONS = [
    "ant",
    "closure",
    "maven",
    "rhino",
    "chocopy",
    "gson",
    "jackson"
]
ALGOS = [
    "zeugma-none",
    "ei",
    "zest",
    "zeugma-linked",
    "bedivfuzz-structure",
    "random",
    "zest-mini",
]

def call(command: list[str]):
    subprocess.call(command, cwd=os.path.join(BASE_DIR, "zeugma"))



def get_commands(time: int, log_mutation: bool, repetitions: int, base_dir: str):
    log_mutation_profile = ",log-mutation" if log_mutation else ""
    for config in CONFIGURATIONS:
        for algo in ALGOS:
            for index in range(repetitions):
                output_dir = f"{base_dir}/{config}-{algo}-results-{index}"
                command = f"mvn -pl :zeugma-evaluation-tools meringue:fuzz meringue:analyze -P{config},{algo}{log_mutation_profile} -Dmeringue.outputDirectory={output_dir} -Dmeringue.duration=P0DT0H{time}M"
                yield command.split(" ")


def main():
    parser = argparse.ArgumentParser(description="Run fuzzers.")
    parser.add_argument("--time", type=int,
                        help="Running time in minutes", default=1)
    parser.add_argument(
        "--cpus", type=int, help="Number of instances running in parallel", default=1)
    parser.add_argument(
        "--rep", type=int, help="Number of repetitions", default=1)
    parser.add_argument("--log-mutation", type=bool, default=False,
                        help="Log mutation distance of each technique")
    args = parser.parse_args()

    base_dir = f"{BASE_DIR}/../data/raw/fresh-baked"
    if os.path.exists(base_dir):
        shutil.rmtree(base_dir)
    os.makedirs(base_dir, exist_ok=True)
    with Pool(args.cpus) as p:
        p.map(call, get_commands(
            args.time, args.log_mutation, args.rep, base_dir))


if __name__ == "__main__":
    main()
