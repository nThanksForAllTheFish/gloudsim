#!/bin/bash

for ((i = 1; i <= 56;))
do
        echo "clean /ramfs of  vm$i"
        ssh vm$i "rm -rf /framfs/*"
        i=$[$i+1]
done

