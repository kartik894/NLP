package deParser;

import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.util.ScoredObject;
import edu.stanford.nlp.util.ScoredComparator;
import edu.stanford.nlp.util.Pair;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by Kartik S on 3/31/18.
 */

public class DependencyParserAPIUsage {
    public static void main(String[] args) {
        // Seed Data path
        String initPath = "penn-dependencybank/wsj_initial.conllx";
        // Train Data path
        String unlabeledPath = "penn-dependencybank/wsj_unlabeled.conllx";
        // Test Data Path
        String testPath = "penn-dependencybank/wsj_test.conllx";
        // Path to embedding vectors file
        String embeddingPath = "penn-dependencybank/en-cw.txt";
        // Path where model is to be saved
        String modelPath = "outputs/model1";
        // Path where test data annotations are stored
        String testAnnotationsPath = "outputs/test_annotation.conllx";
        
        DependencyParserAPI elem = new DependencyParserAPI(initPath, unlabeledPath, testPath, embeddingPath, modelPath, testAnnotationsPath);
        
        elem.RandomTrain(50, 5000);
//        ArrayList< Pair<String, Integer> > ans = elem.loadSentencesFromFile(initPath, 6);
//        
//        elem.writeSentencesToFile(ans, "penn-dependencybank/yolo.conllx");

//        // Configuring propreties for the parser. A full list of properties can be found
//        // here https://nlp.stanford.edu/software/nndep.shtml
//        Properties prop = new Properties();
//        prop.setProperty("maxIter", "50");
//        DependencyParser p = new DependencyParser(prop);
//
//        // Argument 1 - Training Path
//        // Argument 2 - Dev Path (can be null)
//        // Argument 3 - Path where model is saved
//        // Argument 4 - Path to embedding vectors (can be null)
//        p.train(trainPath, null, modelPath, embeddingPath);
//
//        // Load a saved path
//        DependencyParser model = DependencyParser.loadFromModelFile(modelPath);
//
//        // Test model on test data, write annotations to testAnnotationsPath
//        System.out.println(model.testCoNLL(testPath, testAnnotationsPath));
//
//        // returns parse trees for all the sentences in test data using model, this function does not come with default parser and has been written for you
//        List<DependencyTree> predictedParses = model.testCoNLLProb(testPath);
//
//        // By default NN parser does not give you any probability 
//        // https://cs.stanford.edu/~danqi/papers/emnlp2014.pdf explains that the parsing is performed by picking the transition with the highest output in the final layer 
//        // To get a certainty measure from the final layer output layer, we take use a softmax function.
//        // For Raw Probability score We sum the logs of probability of every transition taken in the parse tree to get the following metric
//        // For Margin Probability score we sum the log of margin between probabilities assigned to two top transitions at every step
//        // Following line prints that probability metrics for 12-th sentence in test data
//        // all probabilities in log space to reduce numerical errors. Adjust your code accordingly!
//        System.out.printf("Raw Probability: %f\n",predictedParses.get(12).RawScore);
//        System.out.printf("Margin Probability: %f\n",predictedParses.get(12).MarginScore);
//
//
//        // You probably want to use the ScoredObject and scoredComparator classes for this assignment
//        // https://nlp.stanford.edu/nlp/javadoc/javanlp-3.6.0/edu/stanford/nlp/util/ScoredObject.html
//        // https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/util/ScoredComparator.html

    }
}

