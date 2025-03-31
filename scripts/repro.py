from multiprocessing import Pool
import sys
import os

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
    #  "ei",
    #  "zest",
    #  "zeugma-linked",
    #  "bedivfuzz-simple",
    #  "bedivfuzz-structure",
    "random",
    "zest-mini",
]


def get_commands():
    path = sys.argv[1]

    for config in CONFIGURATIONS:
        for algo in ALGOS:
            for iter in range(0, 20):
                output_dir = f"{path}/{config}-{algo}-results-{iter}"
                if os.path.exists(output_dir):
                    command = f"mvn -pl :zeugma-evaluation-tools meringue:analyze -P{config},{algo} -Dmeringue.outputDirectory={output_dir}"
                    yield command

with Pool(1) as p:
    p.map(os.system, get_commands())
