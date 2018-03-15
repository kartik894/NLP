source ~/.bashrc
source activate tensorflow_gpu
python pos_bilstm.py /projects/nlp/penn-treebank3/tagged/pos/wsj $HOME/NLP/pos_train_dir standard train 0
source deactivate