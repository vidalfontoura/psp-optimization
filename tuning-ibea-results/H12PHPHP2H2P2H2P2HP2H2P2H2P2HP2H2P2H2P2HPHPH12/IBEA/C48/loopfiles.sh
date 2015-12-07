#!/bin/bash

FILES=*

for f in $FILES
do
        echo "Processing $f dir.."
        result=$(cut -c 1-3 $f/FUN.txt)
        echo $result

done

