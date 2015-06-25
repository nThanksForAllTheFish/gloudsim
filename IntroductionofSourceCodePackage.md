# Introduction of Source Code #

  * bin: binary directory
  * cost: checkpoint cost characterized (in seconds):
```
line_number memory_size checkpoint_cost
```

**Hint**: nfs=network file system ; ramfs: ram file system (local file system) on vm ;
  * jobEventDir: the dir containing job class information (filename: jobSchedClassMap.txt)
```
jobID:class_id
```

**Hint**: see Google-cluster-trace-guide to know the meaning of class\_id
  * lengthstat: statistics (such as CDF) of job length
  * memstat: statistics of memory usage of task
  * simFailureTrace: the dir containing all ready-to-use sample jobs
**Hint**: These sample jobs are all the jobs that have at least one failure event per job. Moreover, the number of tasks having failure events is supposed to be greater than half of total number of tasks in the job. You can also generate other types of sample jobs/tasks based on Google trace.

**Hint**: _single_ dir means the job containing sequential-connected tasks (probably single job); _batch_ dir means the job's tasks are connected in parallel ; _mix_ means the mixture of the both.
  * src: the source code:
```
fr.imag.mescal.gloudsim.comm: communication
fr.imag.mescal.gloudsim.elem: all elements used to simulate objects like job, task,e tc.
fr.imag.mescal.gloudsim.gnuplot: to generate gnuplot files for plotting figures
fr.imag.mescal.gloudsim.prepare: to generate necessary files before official simulation, or analyze/characterize trace. For example, EstCheckpointCost.java is used to estimate checkpoint cost.
fr.imag.mescal.gloudsim.sim: the key package, which is used to perform the simulation on server end. The entry point is fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator.java 
fr.imag.mescal.gloudsim.numeric: to simulate task checkpoint/fail/restart numerically, which can be executed on a normal desktop computer.
fr.imag.mescal.gloudsim.numeric: the key package, which is used to perform the simulation on execution end (i.e., slave node).  
fr.imag.mescal.gloudsim.util: the utility package
```

**Hint**: You can make use of the ready-to-use sample job files to perform numerical simulation for simplicity. See fr.imag.mescal.gloudsim.numeric.

**Hint**: You can get the contents of sample jobs using fr.imag.mescal.gloudsim.test.ReadJobTrace.java

  * lib: the library package, which is the archive of the GloudSim.
  * prop.config: the configuration file