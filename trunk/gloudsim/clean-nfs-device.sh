#!/bin/bash
for ((i = 0; i < 16;))
do
	echo "rm -rf /localfs/contextNFS/$i/*"
	rm -rf /localfs/contextNFS/$i/*
	i=$[$i+1]
done
