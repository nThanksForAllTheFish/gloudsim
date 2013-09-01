#!/bin/bash

export CLASSPATH=lib/cpsim.jar:$CLASSPATH
priority=-1
#simLength is used for jobArrivalTrace
simLength=7550
limitLength=$1
configFile=$2
outputFile=$3
obFile=$4
ourFormulaMark=$5
migFile=$6
echo priority=$priority limitLength=$limitLength simLength=$simLength
rm -rf cpContext/*
ulimit -n 32768
java -Xmx26000m fr.imag.mescal.optft.sim.mainserver.JobEmulator $priority $limitLength $simLength $configFile $obFile $ourFormulaMark $migFile 2>&1 | tee $outputFile
