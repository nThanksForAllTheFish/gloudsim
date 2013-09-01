#!/bin/bash

retestFile=retest2.sh
#only valid when useJobArrivalTrace=false
paralleldegree=$1
simJobNum=$2
#
numOfPhyHosts=16
completeAllWorkload=true
loadRatio=1
useJobArrivalTrace=false
#
limitLength=$3
jobTraceFileName=jobTrace-SC-$3-dec.obj
# testMode="static" or "dynamic"
testMode=static
# taskMode=single or  batch
taskMode=$4
dynamicSolu=false
#only valid when useJobArrivalTrace=true
simLength=5000

java -cp lib/cpsim.jar fr.imag.mescal.optft.util.BatchModifyParameters $retestFile $paralleldegree $simJobNum $numOfPhyHosts $completeAllWorkload $loadRatio $useJobArrivalTrace $limitLength $jobTraceFileName $testMode $taskMode $dynamicSolu $simLength
echo "new parameters are set (done)."
