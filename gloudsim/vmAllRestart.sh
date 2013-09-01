#!/bin/bash
configFile=$1
ourFormula=$2
echo "restart-test.sh:configFile=$configFile ourFormula=$ourFormula"
./vmAllStop.sh
./vmAllStart.sh $configFile $ourFormula
