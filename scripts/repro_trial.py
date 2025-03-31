from multiprocessing import Pool
import os
import shutil

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
    "ei",
    "zest",
    "zeugma-linked",
    "zest-mini",
    "random",
    "bedivfuzz-structure",
]

def run_command_with_file_ops(command_info):
    config, algo, iter_num = command_info
    output_dir = f"/data/aoli/havoc_eval/cov-test-count-based-2/{config}-{algo}-results-{iter_num}"

    # Define paths
    corpus_path = os.path.join(output_dir, "campaign/corpus")
    corpus_trial_controlled_path = os.path.join(output_dir, "campaign/corpus_trial_controlled")
    corpus_full_path = os.path.join(output_dir, "campaign/corpus_full")

    # Step 1: Move corpus to corpus_full (if exists)
    if os.path.exists(corpus_path):
        print(f"Moving {corpus_path} to {corpus_full_path}")
        shutil.move(corpus_path, corpus_full_path)

    # Step 2: Move corpus_trial_controlled to corpus (if exists)
    if os.path.exists(corpus_trial_controlled_path):
        print(f"Moving {corpus_trial_controlled_path} to {corpus_path}")
        shutil.move(corpus_trial_controlled_path, corpus_path)

    # Step 3: Run the Maven command
    command = f"mvn -pl :zeugma-evaluation-tools meringue:analyze -P{config},{algo} -Dmeringue.outputDirectory={output_dir}"
    print(f"Running: {command}")
    os.system(command)

    # Step 4: Restore original structure
    if os.path.exists(corpus_path):
        # Move current corpus back to corpus_trial_controlled
        shutil.move(corpus_path, corpus_trial_controlled_path)
        print(f"Restored {corpus_path} to {corpus_trial_controlled_path}")

    if os.path.exists(corpus_full_path):
        # Move corpus_full back to corpus
        shutil.move(corpus_full_path, corpus_path)
        print(f"Restored {corpus_full_path} to {corpus_path}")

def get_command_info():
    command_infos = []
    for config in CONFIGURATIONS:
        for algo in ALGOS:
            for iter_num in range(0, 20):
                command_infos.append((config, algo, iter_num))
    return command_infos

if __name__ == "__main__":
    # For testing, run just one command with a specific configuration
    # test_config = "rhino"  # Choose one configuration for testing
    # test_algo = "zest"  # Choose one algorithm for testing
    # test_iter = 1  # Choose one iteration for testing

    # # Run a single test command
    # run_command_with_file_ops((test_config, test_algo, test_iter))

    # # Comment out the Pool for now - uncomment for full run
    with Pool(1) as p:
        p.map(run_command_with_file_ops, get_command_info())
