#!/bin/bash

echo "cleaning /cloudNFS/CheckpointSim/cpState"
rm -rf /cloudNFS/CheckpointSim/cpState/*
echo "calling /cloudNFS/CheckpointSim/clean-nfs-device.sh"
ssh vm32 "/cloudNFS/CheckpointSim/clean-nfs-device.sh"
echo "clean ramfs"
/cloudNFS/CheckpointSim/clean-ramfs-device.sh
