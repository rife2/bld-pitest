#!/bin/bash

MAIN="org.pitest.mutationtest.commandline.MutationCoverageReport"
TMPNEW=/tmp/checkcliargs-new
TMPOLD=/tmp/checkcliargs-old

java -cp "lib/test/*" $MAIN --help >$TMPNEW
java -cp "examples/lib/test/*" $MAIN --help >$TMPOLD

diff $TMPOLD $TMPNEW

rm -rf $TMPNEW $TMPOLD
