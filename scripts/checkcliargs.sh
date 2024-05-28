#!/bin/bash

main="org.pitest.mutationtest.commandline.MutationCoverageReport"
new=/tmp/checkcliargs-new
old=/tmp/checkcliargs-old

java -cp "lib/test/*" $main --help >$new
java -cp "examples/lib/test/*" $main --help >$old

diff $old $new

rm -rf $new $old
