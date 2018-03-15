universe = vanilla
Initialdir = $HOME/BiLSTMs/code
Executable = /lusr/bin/bash
Arguments = $HOME/BiLSTMs/code/condor_script.sh
+Group   = "GRAD"
+Project = "INSTRUCTIONAL"
+ProjectDescription = "Assignment for CS388"
Requirements = (TARGET.GPUSlot && Eldar == True)
getenv = True
request_GPUs = 1
+GPUJob = true
Log = /scratch/cluster/kartik/condor.log
Error = $HOME/NLP/condor.err
Output = $HOME/NLP/condor.out
Notification = complete
Notify_user = kartik@cs.utexas.edu
Queue 1