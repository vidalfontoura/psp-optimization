#!/bin/bash

FILES=*

for f in $FILES
do
        echo "Processing $f dir.."
        result=$(cut -c 1-3 $f/FUN.txt | grep 39)
        echo $result

done

