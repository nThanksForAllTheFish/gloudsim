#!/bin/bash

cpcostName=80perc
for ((i=1;i<=5;))
do
	echo "processing $i ...... cpcostName=$cpcostName"
	./testCPURateCPCost.sh $cpcostName
	mkdir CPCost-$cpcostName$i
	mv *.sam *.cost CPCost-$cpcostName$i
	i=$[$i+1]
done
