#!/bin/bash
#example(cpurate=40%): ./EstCPProgram.sh 30 161000000 505 0.002
memIndex=$1
length=$2
sleepTimes=$3
tick=$4

cr_run java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.optft.prepare.EstCPProgram $memIndex /cloudNFS/CheckpointSim/heapMemFiles/$memIndex.heap $length $tick 5555 $sleepTimes
