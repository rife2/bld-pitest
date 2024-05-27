#!/bin/bash

java -cp "lib/test/*" org.pitest.mutationtest.commandline.MutationCoverageReport --help |\
grep "^.*--.*" |\
sed -e "s/\* --/--/" -e "s/ .*//" -e "s/^--/\"--/" -e "s/$/\",/" |\
sort |\
sed -e '/testPlugin/d' -e '/--help/d' -e '/---/d' -e '$s/,//'