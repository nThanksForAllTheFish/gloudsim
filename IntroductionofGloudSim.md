# Introduction #

Google released a large-scale real production trace (http://code.google.com/p/googleclusterdata/) in Nov. 2011. Since then, many researchers have studied and characterized the trace carefully. Many characterization papers can be found on Internet. However, it's still uneasy to make use of the Google trace for simulation, because the Google trace involves too much information (hundreds of G bytes in disk space size) and a lot of useful information is actually hidden for confidentiality. The hidden information includes the real memory usage, the real CPU rate consumed per task, etc. Instead, through the trace, we can know their "relative" values (a.k.a., scaled or normalized values) in that each observed value is got by dividing the true value by the maximum value appearing in the system. More details can be found in Google trace guide
(https://docs.google.com/file/d/0B5g07T_gRDg9NjZnSjZTZzRfbmM/edit).

Based on the Google trace, we designed and implemented a simulation system to reproduce the Google jobs, tasks, events, and resource utilization. Our objective is to support and ease the further research on cloud computing based on the Google trace. This simulation system is called Gloudsim (Google trace based Cloud Simulation System). The whole system is coded in pure Java for high portability. We tried best to make the whole simulation as close as possible to the real situation. For example, we designed a method by executing a Java program with loaded file size to simulate the “REAL” Google task execution with real memory size. We use BLCR to checkpoint and restart tasks at run time. XEN serves as the hypervisor to manage the VM instances, which run millions of Google tasks.

**HINT**: Using `GloudSim`, you don't need to download and study Google's original trace any more (unless you want to do more than what `GloudSim` can help you). You can find the sample job object files in the simFailureTrace directory.

_What can `GloudSim` do for you?_

On a **REAL** cluster or a data center (with multiple VM instances you already deployed), `GloudSim` can help:

  * simulating Jobs, Tasks, Task Failure Events, Task memory utilization
  * queuing tasks when necessary
  * calling BLCR to perform the real checkpoint/restart behaviors,
  * optimizing checkpoint interval (with implemented Young's formula)
  * generating a set of log files in course of the simulation, including the true workload to process, the wall-clock time of each task execution, job execution time, queuing length, number of jobs finished over time, etc.
  * analyzing the log files by math tools like computing distribution

There are a few steps to simulate a Google job in the system.

  * First, we summarize and construct Google sample jobs based on Google traces. Each sample job is a real job appearing in Google trace, and it contains one or more batchtasks. Different batchtasks in one job run in parallel (i.e., Bag-of-Tasks mode). In one batchtask, there is one chain of tasks connected in series, and each task means a uninterrupted execution duration. For example, suppose a job has one batchtask, and this batchtask has 5 tasks, then, there are 4 failure events during the job execution.
  * Then, one or more sample jobs will be selected from among all sample jobs. The selected sample jobs will serve as the "REAL" jobs in simulation. Sample job's properties and behaviors (e.g., failure events) are stored in a .obj file, which can be retrieved easily (see prepare.TestLoadJobTrace.java).
  * Finally, based on simTraceFailure/jobArrivalTrace.txt, the simulated "real" jobs will be submitted to the system over time. Our simulation system will schedule them one by one and run them on VM instances. During their execution, the failure events will be simulated based on trace by using BLCR toolkit.

For details, you are highly recommended to read the paper published in SC'13 (Optimization of Cloud Task Processing with Checkpoint\_restart Mechanism). You can find many details about how to optimize checkpoint intervals there. The SC'13 paper is the first successful case of using `GloudSim`.