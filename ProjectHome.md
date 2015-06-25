On a **REAL** cluster or data center with VM instances deployed, `GloudSim` can help you to reproduce job/task events and emulate resource utilization based on Google trace. The emulated environment will be as close as possible to the Google trace, which involves 4000 application types and 25 million tasks.

  * simulating Jobs/tasks, failure events, memory utilization
  * queuing jobs when necessary
  * calling BLCR to perform REAL checkpoint/restart behaviors
  * optimizing checkpoint intervals based on Young’s formula and other formula
  * generating/analyzing log
  * generating distribution easily

For further information about Google trace, please see https://code.google.com/p/googleclusterdata/