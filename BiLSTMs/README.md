## Steps to execute Code (on streetpizza.cs.utexas.edu machine only)
1. Install Anaconda2 with tensorflow-gpu env and add all environment variables to ~/.bashrc
2. Add the absolute path to the BiLSTMs directory in code/condor_script.sh and code/submit.sh
3. Command to execute the code (5 arguments): 
$ python pos_bilstm.py <path to wsj dataset> <path to write the checkpointed models> standard train <0 : baseline, 1 : add features to input, 2 : add features to output>
Write this command to code/condor_script.sh

# To run locally use:
Train:
$ python pos_bilstm.py <path to wsj dataset> <path to write the checkpointed models> standard train <0 : baseline, 1 : add features to input, 2 : add features to output>
Test:
$ python pos_bilstm.py <path to wsj dataset> <path to the checkpointed models> standard test <0 : baseline, 1 : add features to input, 2 : add features to output>
