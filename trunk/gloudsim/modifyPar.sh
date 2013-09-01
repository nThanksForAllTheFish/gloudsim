#!/bin/bash

retestFile=retest.sh
#only valid when useJobArrivalTrace=false
paralleldegree=100
simJobNum=800
#
numOfPhyHosts=8
completeAllWorkload=true
loadRatio=1
useJobArrivalTrace=true
#
limitLength=$1
jobTraceFileName=$4
# testMode="static" or "dynamic"
testMode=static
# taskMode=single or  batch
taskMode=$2
dynamicSolu=false
#only valid when useJobArrivalTrace=true
simLength=$3

java -cp lib/cpsim.jar fr.imag.mescal.optft.util.BatchModifyParameters $retestFile $paralleldegree $simJobNum $numOfPhyHosts $completeAllWorkload $loadRatio $useJobArrivalTrace $limitLength $jobTraceFileName $testMode $taskMode $dynamicSolu $simLength
echo "new parameters are set (done)."
