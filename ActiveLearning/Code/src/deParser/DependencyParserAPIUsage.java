package deParser;

//import edu.stanford.nlp.parser.nndep.DependencyParser;
//import edu.stanford.nlp.parser.nndep.DependencyTree;
//import edu.stanford.nlp.util.ScoredObject;
//import edu.stanford.nlp.util.ScoredComparator;
//import edu.stanford.nlp.util.Pair;
//
//import java.util.Properties;
//import java.util.List;
//import java.util.ArrayList;

/**
 * Created by Kartik S on 3/31/18.
 */

public class DependencyParserAPIUsage {
    public static void main(String[] args) {
        // Seed Data path
        String initPath = args[0]; // "penn-dependencybank/wsj_initial.conllx";
        // Train Data path
        String unlabeledPath = args[1]; // "penn-dependencybank/wsj_unlabeled.conllx";
        // Test Data Path
        String testPath = args[2]; // "penn-dependencybank/wsj_test.conllx";
        // number of seed sentences
        int seed_size = Integer.parseInt(args[3]);
        // number of unlabeled sentences
        int train_size = Integer.parseInt(args[4]);
        // Path to embedding vectors file
        String embeddingPath = args[5]; // "penn-dependencybank/en-cw.txt";
        // Path where model is to be saved
        String modelPath = args[6]; // "outputs/model_5004";
        // Path where test data annotations are stored
        String testAnnotationsPath = args[7]; // = "outputs/test_annotation.conllx";
        // mode of active learning
        String mode = args[8];
        
        DependencyParserAPI elem = new DependencyParserAPI(initPath, unlabeledPath, testPath, embeddingPath, modelPath, testAnnotationsPath);
        if(mode.equalsIgnoreCase("random"))
        	elem.RandomTrain(seed_size, train_size);
        else if(mode.equalsIgnoreCase("length"))
        	elem.LongSentenceTrain(seed_size, train_size);
        else if(mode.equalsIgnoreCase("raw"))
        	elem.RawScoreTrain(seed_size, train_size);
        else if(mode.equalsIgnoreCase("margin"))
        	elem.MarginScoreTrain(seed_size, train_size);


    }
}

