#!/bin/bash

retestFile=retest3.sh
#only valid when useJobArrivalTrace=false
paralleldegree=100
simJobNum=100
#
numOfPhyHosts=8
completeAllWorkload=true
loadRatio=1
useJobArrivalTrace=true
#
limitLength=$1
jobTraceFileName=jobTrace-$1-dec.obj
# testMode="static" or "dynamic"
testMode=dynamic
# taskMode=single or  batch
taskMode=$2
dynamicSolu=$3
#only valid when useJobArrivalTrace=true
simLength=$4

java -cp lib/cpsim.jar fr.imag.mescal.optft.util.BatchModifyParameters $retestFile $paralleldegree $simJobNum $numOfPhyHosts $completeAllWorkload $loadRatio $useJobArrivalTrace $limitLength $jobTraceFileName $testMode $taskMode $dynamicSolu $simLength
echo "new parameters are set (done)."
