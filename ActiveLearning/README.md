# To compile,
javac -cp .:stanford-corenlp.jar src/deParser/DependencyParserAPI.java
javac -cp .:stanford-corenlp.jar src/deParser/DependencyParserAPIUsage.java

# To run,
The arguments are <seed-set> <unlabeled-set> <test-set> <number-of-seed-sentences> <number-of-unlabeled-sentences> <embedding-path> <model-path> <test-annotations-path> <mode - {random, length, raw, margin}>
java -cp .:stanford-corenlp.jar deParser.DependencyParserAPIUsage \
penn-dependencybank/wsj_initial.conllx \ 
penn-dependencybank/wsj_unlabeled.conllx \ 
penn-dependencybank/wsj_test.conllx \
50 \
5000 \
penn-dependencybank/en-cw.txt \
outputs/model_6001 \
outputs/test_annotation.conllx \
random