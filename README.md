# The Havoc Paradox in Generator-Based Fuzzing

This repository contains the code and data for the paper "The Havoc Paradox in Generator-Based Fuzzing".

## Requirements

- **Python**: >= 3.10
- **java**: == 11
- **Maven**: >= 3.8.6

**Minimum Hardware Requirements:**

- **CPU**: >= 1 cores
- **Memory**: >= 16 G
- **Disk**: >= 50 G

## Docker Setup

We provide a Docker image that includes all the required dependencies and automatically builds the fuzzers. To use it:

1. Build the Docker image:

   ```bash
   docker build -t havoc-mutation-eval .
   ```

   Or if you want to use the pre-built image, you can pull it from Docker Hub:

   ```bash
   docker pull leeleo3x/havoc-mutation-eval
   docker tag leeleo3x/havoc-mutation-eval havoc-mutation-eval
   ```

2. Run the Docker container:

   ```bash
   # Run all evaluations
   docker run -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval run --time 5 --cpus 1 --rep 1 --log-mutation true

   # Run a single campaign
   docker run -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval single FUZZER TARGET OUTPUT_DIR DURATION

   # Extract coverage data
   docker run -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval extract /havoc-mutation-eval/data/raw/fresh-baked /havoc-mutation-eval/data/aggregated

   # Extract mutation distance data
   docker run -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval extract-mutation /havoc-mutation-eval/data/raw/fresh-baked /havoc-mutation-eval/data/aggregated

   # Start an interactive shell
   docker run -it -v $(pwd)/data:/havoc-mutation-eval/data havoc-mutation-eval bash
   ```

> [!NOTE]
> Duration is defined the ISO-8601 duration format, e.g., `PT1H` for 1 hour, `PT5M` for 5 minutes, etc.

## Build the Fuzzers

- To build the fuzzers, run the following command:

```bash
cd fuzzers
./setup.sh
```

## Run one Evaluation

To run a single campaign, you may use the `mvn` command directly:

```bash
cd fuzzers
mvn -pl :zeugma-evaluation-tools meringue:fuzz meringue:analyze \
 -P{FUZZER},{TARGET}{,log-mutation} \
 -Dmeringue.outputDirectory={OUTPUT_DIR} \
 -Dmeringue.duration={DURATION}
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

> [!NOTE]
> Each instance takes ~4-6 GBs so you may need to adjust the number of instances running in parallel according to your machine's memory. The default value is 1.

## Post-process the Results

Once you have finished all campaigns, you can run the following command to post-process the results:

First, you need to create a virtual environment and install the required packages. You can do this by running the following command:

```bash
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r requirements.txt
```

Then, you can run the following command to extract the results from the raw data:

### Pre-baked

To analyze pre-baked result, you first need to download data from [FigShare](https://figshare.com/s/789b43d5b7845655a36d) and unzip the data in the `data/raw` folder.

```
cd ./data/raw
wget ...
unzip pre-baked.zip
cd ../..
```

- To extract the coverage data

```bash
python3 ./scripts/extract.py ./data/raw/pre-baked/24h-no-mutation-distance ./data/aggregated
```

- To extract the mutation distance data

```bash
python3 ./scripts/extract_mutation_data.py ./data/raw/pre-baked/1h-with-mutation-distance ./data/aggregated
```

### Fresh-baked

- First you may extract the coverage data.

```bash
python3 ./scripts/extract.py ./data/raw/fresh-baked ./data/aggregated/fresh-baked
```

- If you have run the campaigns with the `--log-mutation` argument set to `true`, you can also run the following command to post-process the mutation distance logs:

```bash
python3 ./scripts/extract_mutation_data.py ./data/raw/fresh-baked ./data/aggregated/fresh-baked
```

Next, you may check the aggregated result in the `data/aggregated/fresh-baked` folder.

## Visualized the Results

You may open `notebooks/Final Results.ipynb` to visualize the results. Remember to change `DATA_DIR` to `../data/aggregated/fresh-baked` in the notebook if you want to analyze fresh-baked data.

> [!NOTE]
> If you run the campaign too short, you may not get enough data to visualize. You can run the campaign longer, e.g., 5 minutes, and then run the post-process script again.
