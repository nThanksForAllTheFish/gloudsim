#!/bin/bash
cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 1000 single 7550 jobTrace-SC-1000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh true

cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 1000 single 7550 jobTrace-SC-1000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh false

cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 2000 single 7550 jobTrace-SC-2000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh true

cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 2000 single 7550 jobTrace-SC-2000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh false

cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 4000 single 7550 jobTrace-SC-4000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh true

cd /cloudNFS/ParallelColt/
./restartAll.sh
cd /cloudNFS/CheckpointSim
./modifyPar.sh 4000 single 7550 jobTrace-SC-4000-dec.obj | tee modifyPar.log
echo "run retest.sh"
./retest.sh false
