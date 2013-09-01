#!/bin/bash

paralleldegree=5
limitLength=1000
testMode=static
taskMode=single
dynamicSolu=false
#true or false
ourFormula=$1
echo limitLength=$limitLength --- taskMode=$taskMode --- ourformula=$ourFormula formula

./clean-all-device.sh
./vmAllRestart.sh prop.config $ourFormula
mark=$ourFormula-$testMode-$taskMode-$dynamicSolu-$limitLength-$paralleldegree
./JobEmulator.sh $limitLength prop.config output-$mark.txt observer-$mark.txt $ourFormula migstat-$mark.txt
./Logger.sh result-$mark.txt
backup=bk-$mark-tr
if [ ! -d $backup ]
then
	mkdir $backup
fi
mv output-$mark.txt* $backup
mv result-$mark.txt* $backup
mv jobRealLength.log* $backup
mv observer-$mark.txt* $backup
mv migstat-$mark.txt* $backup
mv modifyPar.log $backup
echo =============== done =================

