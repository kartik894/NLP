package deParser;

import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.util.Pair;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DependencyParserAPI {
	
	String initPath; // = "penn-dependencybank/wsj_initial.conllx";
	String unlabeledPath;
	String testPath;
	String embeddingPath; // = "penn-dependencybank/en-cw.txt";
    // Path where model is to be saved
    String modelPath; // = "outputs/model1";
    // Path where test data annotations are stored
    String testAnnotationsPath; // = "outputs/test_annotation.conllx";
    ArrayList< Pair <String, Integer> > sentenceList; // = new ArrayList<Pair<Float,Short>>();

	
	DependencyParserAPI(String initPath, String unlabeledPath, String testPath, String embeddingPath, String modelPath, String testAnnotationsPath) {
		this.initPath = initPath;
		this.unlabeledPath = unlabeledPath;
		this.testPath = testPath;
		this.embeddingPath = embeddingPath;
		this.modelPath = modelPath;
		this.testAnnotationsPath = testAnnotationsPath;
		this.sentenceList = new ArrayList< Pair <String, Integer> >();
	}
	
	List<DependencyTree> TrainAndTest(String trainPath, String testPath) {
		// Configuring properties for the parser. A full list of properties can be found
        // here https://nlp.stanford.edu/software/nndep.shtml
        Properties prop = new Properties();
        prop.setProperty("maxIter", "20");
        DependencyParser p = new DependencyParser(prop);

        // Argument 1 - Training Path
        // Argument 2 - Dev Path (can be null)
        // Argument 3 - Path where model is saved
        // Argument 4 - Path to embedding vectors (can be null)
        p.train(trainPath, null, modelPath, embeddingPath);

        // Load a saved path
        DependencyParser model = DependencyParser.loadFromModelFile(modelPath);

        // Test model on test data, write annotations to testAnnotationsPath
        System.out.println(model.testCoNLL(testPath, testAnnotationsPath));

        // returns parse trees for all the sentences in test data using model, this function does not come with default parser and has been written for you
        List<DependencyTree> predictedParses = model.testCoNLLProb(testPath);
        
        return predictedParses;
	}
	
	ArrayList< Pair <String, Integer> > loadSentencesFromFile(String loadPath, int no_of_sentences) {
		ArrayList< Pair <String, Integer> > senList = new ArrayList< Pair <String, Integer> >();
		try {
			
			File file = new File(loadPath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line, prevline = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (no_of_sentences == 0) break;
				// System.out.println(line);
				stringBuffer.append(line);
				// System.out.println(line.toString().toCharArray().length);
				if(line.toString().length() == 0) {
					
					String[] parts = prevline.split("\t");
					Pair<String, Integer> elem = new Pair<String, Integer> ( stringBuffer.toString(), Integer.parseInt(parts[0]) );
					senList.add(elem);
					stringBuffer.delete(0, stringBuffer.length());
					no_of_sentences -= 1;
					continue;
				}
				stringBuffer.append("\n");
				prevline = line;
				
			}
			fileReader.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Contents of file:");
				// Pair<String, Integer> elem = new Pair<String, Integer>();
//				for(Pair<String, Integer> elem : sentenceList) {
//					System.out.println(elem.first() + "\n" + elem.second());
//				}
		return senList;
		
		
	}
	
	void writeSentencesToFile(ArrayList< Pair<String, Integer> > sentences, String writePath) {
		String content = new String();
		for(Pair<String, Integer> elem : sentences) {
			content = content + elem.first() + "\n";
		}
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(writePath);
			bw = new BufferedWriter(fw);
			bw.write(content);

			System.out.println("Done");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	}
	
	// Train by picking 1500 random words from the unlabeled set
	void RandomTrain(int no_of_seed_sentences, int no_of_unlabeled_sentences) {
		// load seed data
		String seedPath = "penn-dependencybank/random_seed.conllx";
		ArrayList< Pair <String, Integer> > seedSentenceList = loadSentencesFromFile(initPath, no_of_seed_sentences);
		writeSentencesToFile(seedSentenceList, seedPath);
		
		// load training data
		String trainPath = "penn-dependencybank/random_train.conllx";
		ArrayList< Pair <String, Integer> > trainSentenceList = loadSentencesFromFile(unlabeledPath, no_of_unlabeled_sentences);
		
		System.out.printf("Initially, seed size = %d, training size = %d", seedSentenceList.size(), trainSentenceList.size());
		writeSentencesToFile(trainSentenceList, trainPath);
		
		List<DependencyTree> predictedParses = TrainAndTest(seedPath, trainPath);
		
		int no_of_sentences = predictedParses.size();
		
		Random rand;
		while(!trainSentenceList.isEmpty()) {
			int no_of_words = 0;
			while(no_of_words <= 1500) {
				rand = new Random();
				int val = rand.nextInt(no_of_sentences);
				// System.out.println(val);
				Pair <String, Integer> elem = trainSentenceList.get(val);
				seedSentenceList.add(elem);
				trainSentenceList.remove(val);
				no_of_words += elem.second();
				no_of_sentences -= 1;
			}
			// System.out.printf("Number of words = %d. Number of Sentences = %d\nseed size = %d, training size = %d\n", no_of_words, ans, seedSentenceList.size(), trainSentenceList.size());
			
			writeSentencesToFile(seedSentenceList, seedPath);
			writeSentencesToFile(trainSentenceList, trainPath);
			predictedParses = TrainAndTest(seedPath, trainPath);
			no_of_sentences = predictedParses.size();
			
		}
		
	}
	
	// Train by picking 1500 words with least raw score
	void RawScoreTrain() {
		
	}
	
	// Train by picking 1500 words with least margin score
	void MarginScoreTrain() {
		
	}
	
	// Train by picking 1500 words with longest sentence length
	void LongSentenceTrain() {
		
	}
	
}
