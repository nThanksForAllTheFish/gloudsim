#!/bin/bash

memIndex=$1
times=5
port=12345

for(( i = 0 ; i<=10;))
do
	tick=`echo "print 0.005*2**$i" | python`
	echo "processing $i: tick=$tick"
	java -Xmx1000m -cp lib/cpsim.jar fr.imag.mescal.optft.prepare.EstCheckpointCost $memIndex $memIndex /cloudNFS/cpContext cpcost-nfs-$i.sam 100 $tick $times $port
	i=$[$i+1]
done
