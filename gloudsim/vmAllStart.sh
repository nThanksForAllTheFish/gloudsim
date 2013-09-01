rootDir=/cloudNFS/CheckpointSim
configFile=$1
ourFormula=$2
echo "configFile=$configFile"
cd $rootDir
if [ ! -d $rootDir/debug ]
then
	mkdir $rootDir/debug
fi
rm -rf $rootDir/debug/*
i="1"
while [ $i -lt 57 ]
do
        echo "start vm$i's VMServer.sh: $configFile"
        ssh vm$i "cd $rootDir;source /etc/profile;$rootDir/VMServer.sh $configFile $ourFormula &"
        i=$[$i+1]
done
#/cloudNFS/scripts/batch-clean-ramfs.sh
