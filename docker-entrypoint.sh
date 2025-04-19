#!/bin/bash

# Activate Python virtual environment
source .venv/bin/activate

# Default command: show help information
if [ "$#" -eq 0 ]; then
    echo "Zeugma Fuzzing Docker Container"
    echo "================================"
    echo ""
    echo "Usage:"
    echo "  docker run zeugma [COMMAND] [ARGS...]"
    echo ""
    echo "Commands:"
    echo "  run              Run all fuzzing evaluations"
    echo "  single           Run a single fuzzing campaign"
    echo "  extract          Extract coverage data"
    echo "  extract-mutation Extract mutation distance data"
    echo "  bash             Start a bash shell"
    echo ""
    echo "Examples:"
    echo "  docker run zeugma run --time 60 --cpus 2 --rep 3"
    echo "  docker run zeugma single FUZZER TARGET OUTPUT_DIR DURATION"
    echo "  docker run zeugma extract INPUT_DIR OUTPUT_DIR"
    echo "  docker run zeugma extract-mutation INPUT_DIR OUTPUT_DIR"
    echo "  docker run -it zeugma bash"
    exit 0
fi

case "$1" in
    run)
        shift
        cd fuzzers
        python3 run.py "$@"
        ;;
    single)
        shift
        if [ "$#" -ne 4 ]; then
            echo "Usage: single FUZZER TARGET OUTPUT_DIR DURATION"
            exit 1
        fi
        cd fuzzers
        mvn -pl :zeugma-evaluation-tools meringue:fuzz meringue:analyze \
            -P$1,$2 \
            -Dmeringue.outputDirectory=$3 \
            -Dmeringue.duration=$4
        ;;
    extract)
        shift
        if [ "$#" -ne 2 ]; then
            echo "Usage: extract INPUT_DIR OUTPUT_DIR"
            exit 1
        fi
        python3 ./scripts/extract.py "$1" "$2"
        ;;
    extract-mutation)
        shift
        if [ "$#" -ne 2 ]; then
            echo "Usage: extract-mutation INPUT_DIR OUTPUT_DIR"
            exit 1
        fi
        python3 ./scripts/extract_mutation_data.py "$1" "$2"
        ;;
    bash)
        exec bash
        ;;
    *)
        echo "Unknown command: $1"
        exit 1
        ;;
esac
