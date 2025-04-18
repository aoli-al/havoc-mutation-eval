#!/bin/bash

set -e

if [ $# -lt 3 ]; then
  echo "Usage: $0 <NAME> <TEST_CLASS> <RUNS>"
  exit 1
fi

pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

JQF_DIR="$SCRIPT_DIR/../../.."
JQF_REPRO="$JQF_DIR/bin/jqf-repro -i"
NAME=$1
TEST_CLASS="edu.berkeley.cs.jqf.examples.$2"
RUNS="$3"
ALGO=$4
METHOD=$6


export JVM_OPTS="$JVM_OPTS -Djqf.repro.logUniqueBranches=true -Xmx20g"

for e in $(seq 0 $RUNS); do
  ZEST_FAST_OUT_DIR="$NAME-zest-testWithGenerator-results-$e"
  EI_OUT_DIR="$NAME-ei-testWithGenerator-results-$e"
  $JQF_REPRO -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator   $ZEST_FAST_OUT_DIR/corpus/* 2>$ZEST_FAST_OUT_DIR/cov_error.log | grep "^# Cov" | sort | uniq > $ZEST_FAST_OUT_DIR/cov-all.log &
  $JQF_REPRO -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator   $EI_OUT_DIR/corpus/* 2>$EI_OUT_DIR/cov_error.log | grep "^# Cov" | sort | uniq > $EI_OUT_DIR/cov-all.log &
  ZEST_FAST_OUT_DIR="$NAME-zest-testWithReversedGenerator-results-$e"
  EI_OUT_DIR="$NAME-ei-testWithReversedGenerator-results-$e"
  $JQF_REPRO -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithReversedGenerator   $ZEST_FAST_OUT_DIR/corpus/* 2>$ZEST_FAST_OUT_DIR/cov_error.log | grep "^# Cov" | sort | uniq > $ZEST_FAST_OUT_DIR/cov-all.log &
  $JQF_REPRO -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithReversedGenerator $EI_OUT_DIR/corpus/* 2>$EI_OUT_DIR/cov_error.log | grep "^# Cov" | sort | uniq > $EI_OUT_DIR/cov-all.log 
done

for job in `jobs -p`
do
  echo $job
  wait $job || let "FAIL+=1"
done

