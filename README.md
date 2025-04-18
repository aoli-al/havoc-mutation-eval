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

The results will be saved in the `data/raw/fresh-baked` folder.