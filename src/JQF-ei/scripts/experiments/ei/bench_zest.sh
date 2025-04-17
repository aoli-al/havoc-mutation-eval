#!/bin/bash

set -e

if [ $# -lt 6 ]; then
  echo "Usage: $0 <NAME> <TEST_CLASS> <IDX> <TIME> <DICT> <SEEDS>"
  exit 1
fi

pushd `dirname $0` > /dev/null
SCRIPT_DIR=$( dirname "$0" )
popd > /dev/null

JQF_DIR="$SCRIPT_DIR/../../../"
JQF_EI="$JQF_DIR/bin/jqf-ei"
JQF_ZEST="$JQF_DIR/bin/jqf-zest"
JQF_MIX="$JQF_DIR/bin/jqf-mix"
NAME=$1
TEST_CLASS="edu.berkeley.cs.jqf.examples.$2"
IDX=$3
TIME=$4
DICT="$JQF_DIR/examples/target/test-classes/dictionaries/$5"
SEEDS="$JQF_DIR/examples/target/seeds/$6"
SEEDS_DIR=$(dirname "$SEEDS")
METHOD=$7

e=$IDX

EI_NO_HAVOC_OUT_DIR="$NAME-ei-no-havoc-results-$e"
EI_OUT_DIR="$NAME-ei-fast-results-$e"
ZEST_FAST_OUT_DIR="$NAME-zest-$METHOD-results-$e"

if [ -d "$JQF_OUT_DIR" ]; then
  echo "Error! There is already a directory by the name of $JQF_OUT_DIR"
  exit 3
fi

# Do not let GC mess with fuzzing
export JVM_OPTS="$JVM_OPTS -XX:-UseGCOverheadLimit -Xmx20g"


SNAME="$NAME-$e"



FAST_ENV="\"$JVM_OPTS -DuseFastNonCollidingCoverageInstrumentation=true\""
screen -S "$SNAME" -dm -t zest_fast_$e
# screen -S "$SNAME" -X screen -t mix_$e
# screen -S "$SNAME" -X screen -t mix_no_havoc_$e
screen -S "$SNAME" -p zest_fast_$e -X stuff "JVM_OPTS=$FAST_ENV timeout $TIME $JQF_ZEST -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS $METHOD $ZEST_FAST_OUT_DIR^M"
# screen -S "$SNAME" -p mix_$e -X stuff "JVM_OPTS=$FAST_ENV timeout $TIME $JQF_MIX $TIME -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator $MIX_OUT_DIR^M"

# NO_HAVOC_ENV="\"$JVM_OPTS -DuseFastNonCollidingCoverageInstrumentation=true -Djqf.ei.HAVOC_PROBABILITY=0.0\""
# screen -S "$SNAME" -p mix_no_havoc_$e -X stuff "JVM_OPTS=$NO_HAVOC_ENV timeout $TIME $JQF_MIX $TIME -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator $MIX_NO_HAVOC_OUT_DIR^M"
# screen -S "$SNAME" -p ei_no_havoc_$e -X stuff "JVM_OPTS=$NO_HAVOC_ENV timeout $TIME $JQF_EI -c \$($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator $EI_NO_HAVOC_OUT_DIR^M"

