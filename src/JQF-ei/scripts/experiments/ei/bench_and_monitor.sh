#!/bin/bash

set -e

if [ $# -lt 6 ]; then
  echo "Usage: $0 <NAME> <TEST_CLASS> <IDX> <TIME> <DICT> <SEEDS>"
  exit 1
fi

pushd `dirname $0` > /dev/null
SCRIPT_DIR=$( dirname "$0" )
popd > /dev/null

ALGO=$8
EXPERIMENT=$9
ENDPOINT=${10}
ORG_ID=${11}
BUCKET=${12}
TOKEN=${13}
JQF_DIR="$SCRIPT_DIR/../../../"
JQF_BIN="$JQF_DIR/bin/jqf-$ALGO"
NAME=$1
TEST_CLASS="edu.berkeley.cs.jqf.examples.$2"
IDX=$3
TIME=$4
DICT="$JQF_DIR/examples/target/test-classes/dictionaries/$5"
SEEDS="$JQF_DIR/examples/target/seeds/$6"
SEEDS_DIR=$(dirname "$SEEDS")
METHOD=$7
e=$IDX
JQF_REPRO="$JQF_DIR/bin/jqf-reporter -i"

OUT_DIR="$NAME-$ALGO-$METHOD-results-$e"

# Do not let GC mess with fuzzing
export JVM_OPTS="$JVM_OPTS -XX:-UseGCOverheadLimit -Xmx10g -Djqf.ei.TIMEOUT=10000"


SNAME=$ALGO\_$METHOD\_$e



mkdir -p $OUT_DIR/corpus

screen -S "$SNAME" -dm -t fuzzer
screen -S "$SNAME" -X screen -t monitor


FAST_ENV="\"$JVM_OPTS -DuseFastNonCollidingCoverageInstrumentation=true\""


if [ "$ALGO" == "mix" ]; then
  COMMAND="$JQF_BIN $TIME -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS $METHOD $OUT_DIR^M"
else
  COMMAND="timeout $TIME $JQF_BIN -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS $METHOD $OUT_DIR^M"
fi


screen -S "$SNAME" -p fuzzer -X stuff "JVM_OPTS=$FAST_ENV $COMMAND"

