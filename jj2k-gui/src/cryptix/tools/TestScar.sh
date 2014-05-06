#!/bin/sh

#
#  TestScar.sh - run a quick test to see if scar is all there
#  and basically working.
#
#  Dependencies: CLASSPATH must be se to find all classes needed.
#

set -x
varg="v"

# done in place, and locally for now as scar does not handle absolute
# pathnames well, and cd'ing might change the nature of the CLASSPATH
# cd /tmp
tmp=.scar$$
trap "rm -rf ${tmp}in ${tmp}out ${tmp}archive" 0

#
#   Make a toy directory.
#
mkdir ${tmp}in ${tmp}out
echo 1111111111111 > ${tmp}in/one
echo 2222222222222 > ${tmp}in/two
echo 3333333333333 > ${tmp}in/three

#
#   Archive it, extract it, and compare the extraction  with the original.
#
java cryptix.tools.Scar -e${varg}r -p "test"  ${tmp}in  ${tmp}archive
[ $? -ne 0 ] && echo Scar encrypt failed $? && exit 1

java cryptix.tools.Scar -d${varg} -p "test"  ${tmp}archive ${tmp}out
[ $? -ne 0 ] && echo Scar decrypt failed $? && exit 1

for file in ${tmp}in/*
do
    cmp ${file} ${tmp}out/${file##*/} || exit 1
done

exit 0
