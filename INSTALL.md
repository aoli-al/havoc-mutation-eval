To verify that you have succesfully compiled all fuzzers, you may run a small compaign for each fuzzer. 

- For example, to run a small campaign for the `ei` fuzzer with the `closure` target, you can run the following command:

Docker:

```bash
docker run -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval single ei closure /havoc-mutation-eval/data/raw/ei-closure-single-run PT5M
```

Maven:

```bash 
cd fuzzers
mvn -pl :zeugma-evaluation-tools meringue:fuzz meringue:analyze \
  -Pei,closure,log-mutation \
  -Dmeringue.outputDirectory=../data/raw/ei-closure-single-run \
  -Dmeringue.duration=PT5M
```

Then you can check the output in `data/raw/ei-closure-single-run` directory:

```tree
.
├── campaign
│   ├── corpus              ---> Contains raw byte stream of each saved input.
│   ├── coverage_hash       
│   ├── failures
│   ├── fuzz.log            ---> Contains logs of the fuzzing process.
│   ├── mutation.log        ---> Contains logs of the mutation process (mutation distance).
│   └── plot_data
├── coverage.csv            ---> Raw coverage data.
├── failures.json
├── jacoco
│   └── jacoco.csv
└── summary.json            ---> Contains a summary of the fuzzing results.
```