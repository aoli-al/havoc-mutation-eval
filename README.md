# The Havoc Paradox in Generator-Based Fuzzing

This repository contains the code and data for the paper "The Havoc Paradox in Generator-Based Fuzzing".

## Requirements


## Build the Fuzzers

- To build the fuzzers, run the following command:

```bash
cd fuzzers
./setup.sh
```

## Run one Evaluation

```
mvn :mer
```

## Run all Evaluations

We also provide a script to run all evaluations. To do so, run the following command:

```python
python3 run.py -h
usage: run.py [-h] [--timeout TIMEOUT] [--cpus CPUS] [--rep REP] [--log-mutation LOG_MUTATION]

Run fuzzers.

options:
  -h, --help            show this help message and exit
  --time Time           Running time in minutes
  --cpus CPUS           Number of instances running in parallel
  --rep REP             Number of repetitions
  --log-mutation LOG_MUTATION
                        Log mutation distance of each technique
```

This script will run all the fuzzers in parallel, using the number of CPUs specified by the `--cpus` argument. The default value is 1. The script will also run the fuzzers for the number of repetitions specified by the `--rep` argument. The default value is 1. The script will enable the mutation distance logging of each technique, if the `--log-mutation` argument is set to `true`. The default value is `false`.

The results will be saved in the `data/raw/fresh-baked` folder. Each campaign will be saved in a separate folder, with name `{target}-{fuzzer}-results-{id}`.


## Post-process the Results

Once you have finished all campaigns, you can run the following command to post-process the results:

```python
python3 ./scripts/extract.py ./data/raw/fresh-baked ./data/aggregated/fresh-baked
```

If you have run the campaigns with the `--log-mutation` argument set to `true`, you can also run the following command to post-process the mutation distance logs:

```python
python3 ./scripts/extract_mutation_data.py ./data/raw/fresh-baked ./data/aggregated/fresh-baked
```

Next, you may check the aggregated result in the `data/aggregated/fresh-baked` folder.

## Visualized the Results