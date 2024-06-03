#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import subprocess

base = sys.argv[1]
#  campaigns = {
    #  "zeugma": [242,902,572,407,187,1012,1067,682,517,792,957,462,627,77,297,737,352,132,847,22],
    #  "zest": [183,623,403,898,513,18,568,348,1008,788,238,73,843,458,1063,128,733,678,953,293]
#  }
campaigns = {
    "zeugma-24": ["closure-0", "closure-1", "closure-2", "closure-3", "closure-4"],
    "zeugmax-24": [1,2,3,4,5]
}

results = []
for algo, indices in campaigns.items():
    for  campaign in indices:
        path = os.path.join(base, str(campaign))
        if not os.path.exists(path):
            continue
        subprocess.run([
            "mvn",
            "-pl",
            ":zeugma-evaluation-tools",
            "meringue:analyze",
            "-Pclosure,zeugma-none",
            "-Dmeringue.duration=P1DT0H0M",
            "-Dmeringue.outputDirectory=",
        ])
        command = " ".join([
            f"JVM_OPTS=\"-Djqf.repro.logUniqueBranches=true -Djqf.repro.traceDir={path}\"",
            "./bin/jqf-repro",
            "-i",
            "-c",
            "$(./scripts/examples_classpath.sh)",
            "edu.berkeley.cs.jqf.examples.closure.CompilerTest",
            "testWithInputStream",
            os.path.join(path, "gen")
        ])
        subprocess.run(f"{command} 2> /dev/null | grep \"^# Cov\" | sort | uniq > {path}/cov-all.log",
                       shell=True,
                       cwd="/Users/aoli/repos/JQF-ei")
        with open(os.path.join(path, "cov-all.log")) as f:
            result = set()
            for line in f:
                result.add(line)
        results.append(result)
    if not results:
        continue
    total = sum(map(lambda it: len(it), results))
    print(algo, total / len(results))
    with open(os.path.join(base, f'{algo}-intersection.txt'), "w") as f:
        f.write("".join(sorted(set.intersection(*results))))
    with open(os.path.join(base, f'{algo}-union.txt'), "w") as f:
        f.write("".join(sorted(set.union(*results))))
