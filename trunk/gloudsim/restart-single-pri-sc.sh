#!/bin/bash
cd /cloudNFS/CheckpointSim
./modifyPar.sh 1000 single 7550 jobTrace-SC-1000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh true

