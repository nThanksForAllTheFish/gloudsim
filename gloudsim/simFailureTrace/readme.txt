jobArrivalTrace.txt contains the job arrival timestamps recorded in the Google trace. 
In the three directories (batch, single, mix): 
jobTrace-xxx-dec.obj contains the sample jobs of Google trace. 
Across different jobtrace.obj files, they have the same sample jobs, e.g., the length, the mem size, the failure intervals, etc.
The main differences are below:
jobTrace-[number]-dec.obj  means the jobs'MTBF and MNOF are only computed for the jobs whose lengths are less than [number] seconds. 
For example, jobTrace-2000-dec.obj, if a job in this file is shorter than 2000 seconds, then its MTBF and its MNOF will be non-zero and compuated as the mean value of all the jobs whose lengths are shorter than 2000 seconds. 

jobTrace-[number]-[number]-dec.obj is different from jobTrace-0-[number]-[number]-dec.obj
The former means pseudo-stair-style computation of MTBF and MNOF
The latter means a stair-style computation of MTBF and MNOF.
In general, the latter is recommeded. 
[0, 500], [500, 1000] [1000, 2000], [2000, 4000]

jobTrace-SC-dec.obj: SC means considering "Scheduling Class"

In the simulation, you just need to select one obj file as the input parameter. (see prop.config)