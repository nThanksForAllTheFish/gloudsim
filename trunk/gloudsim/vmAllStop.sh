#!/bin/bash

cd /cloudNFS/ParallelColt
i="1"
while [ $i -lt 57 ]
do
	echo "stop vm$i's guestVMServer"
	ssh vm$i "killall java"
	i=$[$i+1]
done
