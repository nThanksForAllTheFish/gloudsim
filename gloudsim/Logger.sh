#!/bin/bash
outputFile=$1
#echo restart vm8
#ssh se017e "xm destroy vm8;xm create /cloudNFS/centos-img/centos-vm8.cfg"
#echo wait 20 seconds
#sleep 20;
echo run Logger
ssh vm8 "cd /cloudNFS/CheckpointSim;source /etc/profile;java -Xmx900m -cp lib/cpsim.jar fr.imag.mescal.optft.sim.log.Logger jobRealLength.log /localfs/contextNFS /cloudNFS/CheckpointSim/cpState $outputFile"
