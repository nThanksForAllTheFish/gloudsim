# Quick Start #

## 1. Download the package using svn. ##
## 2. Prerequisites ##
  * You need to install BLCR (Berkey Lab Checkpoint/Restart Toolkit). BLCR is used to perform checkpoint and restart for any running/failed program. If you encounter any problems, you can post the questions in BLCR discussion group, and the developers will reply you very soon with very great patience!
  * Install and set NFS (Network File System), e.g., called /cloudNFS
  * Install XEN3 or XEN4 on each physical machine, and make sure the XEN commands like “xm create xxx.cfg” are normal.
  * On each physical machine, you should bootup the VM instances before hand. In my testbed, for example, there are 8 physical machines, on each deploying 7 VM instances. Each physical machine has 8 cores, so it is necessary to reserve one core for XEN, which means there are only 7 cores to use if you hope to let each VM instance correspond to one core. At the end of this step, vm1,vm2,….,vm56 will be started on the 8 machines.
  * The VM version I used in my testbed is centos 5.3. You should configure the network (i.e., /etc/sysconfig/network-scripts/[NIC-card-config-file]) among the VM instances such that: (1) they can communicate each other; (2) they can communicate with physical machines (because physical machines are NFS servers). On VM instances, mount necessary directories, e.g., the VM image directory, the shared-disk (NFS) for storing checkpoint files, the local directory for storing checkpoint files.

**HINT**: You need to install BLCR on VM images. In fact, for simplicity, you could use NFS devise to install BLCR, and mount it onto any VM instance, then, every VM instance has BLCR installed.
  * You need to create a directory called heapMemFiles in the download package. It is used to simulate memory sizes based on trace and its content is shown below:
```
[root@vm1 heapMemFiles]# pwd
/cloudNFS/CheckpointSim/heapMemFiles
[root@vm1 heapMemFiles]# ls
0.heap    13.heap  18.heap  22.heap  27.heap  31.heap  36.heap  40.heap  45.heap  4.heap   54.heap  59.heap  63.heap  68.heap  72.heap  77.heap  81.heap  86.heap  90.heap  95.heap  9.heap
100.heap  14.heap  19.heap  23.heap  28.heap  32.heap  37.heap  41.heap  46.heap  50.heap  55.heap  5.heap   64.heap  69.heap  73.heap  78.heap  82.heap  87.heap  91.heap  96.heap
10.heap   15.heap  1.heap   24.heap  29.heap  33.heap  38.heap  42.heap  47.heap  51.heap  56.heap  60.heap  65.heap  6.heap   74.heap  79.heap  83.heap  88.heap  92.heap  97.heap
11.heap   16.heap  20.heap  25.heap  2.heap   34.heap  39.heap  43.heap  48.heap  52.heap  57.heap  61.heap  66.heap  70.heap  75.heap  7.heap   84.heap  89.heap  93.heap  98.heap
12.heap   17.heap  21.heap  26.heap  30.heap  35.heap  3.heap   44.heap  49.heap  53.heap  58.heap  62.heap  67.heap  71.heap  76.heap  80.heap  85.heap  8.heap   94.heap  99.heap
[root@vm1 heapMemFiles]# du -sh 0.heap
0       0.heap
[root@vm1 heapMemFiles]# du -sh 1.heap
2.0M    1.heap
[root@vm1 heapMemFiles]# du -sh 2.heap
4.0M    2.heap
[root@vm1 heapMemFiles]# du -sh 20.heap
40M     20.heap
```

## 3. How to generate sample jobs or other necessary files ##
**Hint** You can use the java programs under fr.imag.mescal.gloudsim.prepare to prepare the necessary files used for simulation.
  * You need sample job files: For simplicity, I already generated the files for you. You could find them in the simFailureTrace directory.  In this directory, jobArrivalTrace.txt records the submission dates of jobs in the Google trace. There are three further sub-directories, single, batch, mix. They just contain corresponding types of jobs. For example, single means in the job, there is only one batchtask. In batch directory, each job has multiple parallel batchtasks (like mapreduce). "mix" means the mixture of the two types of jobs.
(BatchTask actually refers to one TASK mentioned in the paper. A batchtask contains a chain of subtasks, each of which refers to an uninterrupted execution duration.)
  * You need to modify prop.config file for your environment. For example, how many physical machines to use.
## 4. Start the simulation using JobEmulator.java class, which is the entry point. ##
**Hint** Some useful scripts can be found in the package.==