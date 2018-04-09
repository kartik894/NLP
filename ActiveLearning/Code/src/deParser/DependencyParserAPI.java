package deParser;

import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.util.Pair;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Kartik S on 3/31/18.
 */

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
        prop.setProperty("maxIter", "500");
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

			// System.out.println("Done");
			
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
		
		writeSentencesToFile(trainSentenceList, trainPath);
		
		List<DependencyTree> predictedParses = TrainAndTest(seedPath, trainPath);
		
		int no_of_sentences = trainSentenceList.size();
		int ans = 0, iterations = 0;
		Random rand;
		while(!trainSentenceList.isEmpty() && iterations < 20) {
			int no_of_words = 0;
			while(no_of_words <= 1500 && no_of_sentences > 0) {
				rand = new Random();
				int val = rand.nextInt(no_of_sentences);
				// System.out.println(val);
				Pair <String, Integer> elem = trainSentenceList.get(val);
				seedSentenceList.add(elem);
				trainSentenceList.remove(val);
				no_of_words += elem.second();
				no_of_sentences -= 1;
				ans += 1;
			}
			if(no_of_sentences <= 0) break;
			System.out.printf("Number of words = %d. Number of Sentences = %d\nseed size = %d, training size = %d\n", no_of_words, ans, seedSentenceList.size(), trainSentenceList.size());
			
			writeSentencesToFile(seedSentenceList, seedPath);
			writeSentencesToFile(trainSentenceList, trainPath);
			predictedParses = TrainAndTest(seedPath, trainPath);
			no_of_sentences = trainSentenceList.size();
			iterations++;
		}
		predictedParses = TrainAndTest(seedPath, testPath);
	}
	
	// Train by picking 1500 words with least raw score
	void RawScoreTrain(int no_of_seed_sentences, int no_of_unlabeled_sentences) {
		// load seed data
		String seedPath = "penn-dependencybank/rawScore_seed.conllx";
		ArrayList< Pair <String, Integer> > seedSentenceList = loadSentencesFromFile(initPath, no_of_seed_sentences);
		writeSentencesToFile(seedSentenceList, seedPath);
		
		// load training data
		String trainPath = "penn-dependencybank/rawScore_train.conllx";
		ArrayList< Pair <String, Integer> > trainSentenceList = loadSentencesFromFile(unlabeledPath, no_of_unlabeled_sentences);
		
		writeSentencesToFile(trainSentenceList, trainPath);
		
		List<DependencyTree> predictedParses = TrainAndTest(seedPath, trainPath);
		
		ArrayList< Pair<Integer, Double> > rawScoreToSentenceMap = new ArrayList<Pair <Integer, Double> > ();
		
		for(int i = 0; i < predictedParses.size(); i++) {
//			System.out.println(predictedParses.get(i).RawScore);
//			System.out.println(trainSentenceList.get(i).second());
//			System.out.println(predictedParses.get(i).RawScore * 0.5/trainSentenceList.get(i).second());
			rawScoreToSentenceMap.add(new Pair<Integer, Double> (i, predictedParses.get(i).RawScore * 0.5/trainSentenceList.get(i).second()));
		}
		
		rawScoreToSentenceMap.sort(new Comparator<Pair<Integer, Double>>() {
	        @Override
	        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
	            if (o1.second() > o2.second()) {
	                return 1;
	            } else {
	                return -1;
	            }
	        }
	    });
		// Check if sorted
//		for(Pair<Integer, Double> elem : rawScoreToSentenceMap) {
//			System.out.println(elem.first() + "\n" + elem.second());
//		}
		ArrayList<Integer> toDel = new ArrayList<Integer> ();
		int no_of_sentences = trainSentenceList.size();
		int ans = 0, iterations = 0;
		while(!trainSentenceList.isEmpty() && iterations < 20) {
			int no_of_words = 0;
			int j = 0;
			toDel.clear();
			while(no_of_words <= 1500 && no_of_sentences > 0) {
				// rand = new Random();
				// int val = rand.nextInt(no_of_sentences);
				// System.out.println(val);
				Pair <String, Integer> elem = trainSentenceList.get(rawScoreToSentenceMap.get(j).first());
				seedSentenceList.add(elem);
				toDel.add(rawScoreToSentenceMap.get(j).first());
				// trainSentenceList.remove((int)rawScoreToSentenceMap.get(j).first());
				// System.out.printf("rawScore least index = %d\n", rawScoreToSentenceMap.get(j).first());
				no_of_words += elem.second();
				no_of_sentences -= 1;
				ans += 1;
				j += 1;
			}
			toDel.sort(new Comparator<Integer> () {
				public int compare(Integer a, Integer b) {
					if(a < b) return 1;
					else if (a == b) return 0;
					else return -1;
				}
			});
			for(int x : toDel) {
				trainSentenceList.remove(x);
			}
			if(no_of_sentences <= 0) break;
			System.out.printf("Number of words = %d. Number of Sentences = %d\nseed size = %d, training size = %d\n", no_of_words, ans, seedSentenceList.size(), trainSentenceList.size());
			
			writeSentencesToFile(seedSentenceList, seedPath);
			writeSentencesToFile(trainSentenceList, trainPath);
			predictedParses = TrainAndTest(seedPath, trainPath);
			no_of_sentences = trainSentenceList.size();
			
			rawScoreToSentenceMap.clear();
			
			for(int i = 0; i < predictedParses.size(); i++) {
				rawScoreToSentenceMap.add(new Pair<Integer, Double> (i, predictedParses.get(i).RawScore * 0.5 / trainSentenceList.get(i).second()));
			}
			rawScoreToSentenceMap.sort(new Comparator<Pair<Integer, Double>>() {
		        @Override
		        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
		            if (o1.second() > o2.second()) {
		                return 1;
		            } else {
		                return -1;
		            }
		        }
		    });
			iterations++;
			
		}
		predictedParses = TrainAndTest(seedPath, testPath);
	}
	
	// Train by picking 1500 words with least margin score
	void MarginScoreTrain(int no_of_seed_sentences, int no_of_unlabeled_sentences) {
		// load seed data
		String seedPath = "penn-dependencybank/marginScore_seed.conllx";
		ArrayList< Pair <String, Integer> > seedSentenceList = loadSentencesFromFile(initPath, no_of_seed_sentences);
		writeSentencesToFile(seedSentenceList, seedPath);
		
		// load training data
		String trainPath = "penn-dependencybank/marginScore_train.conllx";
		ArrayList< Pair <String, Integer> > trainSentenceList = loadSentencesFromFile(unlabeledPath, no_of_unlabeled_sentences);
		
		writeSentencesToFile(trainSentenceList, trainPath);
		
		List<DependencyTree> predictedParses = TrainAndTest(seedPath, trainPath);
		
		ArrayList< Pair<Integer, Double> > rawScoreToSentenceMap = new ArrayList<Pair <Integer, Double> > ();
		
		for(int i = 0; i < predictedParses.size(); i++) {
			rawScoreToSentenceMap.add(new Pair<Integer, Double> (i, predictedParses.get(i).MarginScore * 0.5/trainSentenceList.get(i).second()));
		}
		rawScoreToSentenceMap.sort(new Comparator<Pair<Integer, Double>>() {
	        @Override
	        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
	            if (o1.second() > o2.second()) {
	                return 1;
	            } else {
	                return -1;
	            }
	        }
	    });
		
		ArrayList<Integer> toDel = new ArrayList<Integer> ();
		int no_of_sentences = trainSentenceList.size();
		int ans = 0, iterations = 0;
		while(!trainSentenceList.isEmpty() && iterations < 20) {
			toDel.clear();
			int no_of_words = 0;
			int j = 0;
			while(no_of_words <= 1500 && no_of_sentences > 0) {
				// rand = new Random();
				// int val = rand.nextInt(no_of_sentences);
				// System.out.println(val);
				Pair <String, Integer> elem = trainSentenceList.get(rawScoreToSentenceMap.get(j).first());
				seedSentenceList.add(elem);
				toDel.add(rawScoreToSentenceMap.get(j).first());
				// trainSentenceList.remove(rawScoreToSentenceMap.get(j).first());
				no_of_words += elem.second();
				no_of_sentences -= 1;
				ans += 1;
				j += 1;
				
			}
			toDel.sort(new Comparator<Integer> () {
				public int compare(Integer a, Integer b) {
					if(a < b) return 1;
					else if (a == b) return 0;
					else return -1;
				}
			});
			for(int x : toDel) {
				// System.out.println(x);
				trainSentenceList.remove(x);
			}
			if(no_of_sentences <= 0) break;
			System.out.printf("Number of words = %d. Number of Sentences = %d\nseed size = %d, training size = %d\n", no_of_words, ans, seedSentenceList.size(), trainSentenceList.size());
			
			writeSentencesToFile(seedSentenceList, seedPath);
			writeSentencesToFile(trainSentenceList, trainPath);
			predictedParses = TrainAndTest(seedPath, trainPath);
			no_of_sentences = trainSentenceList.size();
			
			rawScoreToSentenceMap.clear();
			
			for(int i = 0; i < predictedParses.size(); i++) {
				rawScoreToSentenceMap.add(new Pair<Integer, Double> (i, predictedParses.get(i).MarginScore * 0.5/trainSentenceList.get(i).second()));
			}
			rawScoreToSentenceMap.sort(new Comparator<Pair<Integer, Double>>() {
		        @Override
		        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
		            if (o1.second() > o2.second()) {
		                return 1;
		            } else {
		                return -1;
		            }
		        }
		    });
			
			iterations++;
		}
		predictedParses = TrainAndTest(seedPath, testPath);
	}
	
	// Train by picking 1500 words with longest sentence length
	void LongSentenceTrain(int no_of_seed_sentences, int no_of_unlabeled_sentences) {
		// load seed data
		String seedPath = "penn-dependencybank/longS_seed.conllx";
		ArrayList< Pair <String, Integer> > seedSentenceList = loadSentencesFromFile(initPath, no_of_seed_sentences);
		writeSentencesToFile(seedSentenceList, seedPath);
		
		// load training data
		String trainPath = "penn-dependencybank/longS_train.conllx";
		ArrayList< Pair <String, Integer> > trainSentenceList = loadSentencesFromFile(unlabeledPath, no_of_unlabeled_sentences);
		writeSentencesToFile(trainSentenceList, trainPath);
		
		List<DependencyTree> predictedParses = TrainAndTest(seedPath, trainPath);
		// Sort in descending order of sentence length. Longest sentences seem to be the most difficult to annotate.
		trainSentenceList.sort(new Comparator<Pair<String, Integer>>() {
	        @Override
	        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
	            if (o1.second() < o2.second()) {
	                return 1;
	            } else if (o1.second() == o2.second()) {
	            	return 0;
	            } else {
	                return -1;
	            }
	        }
	    });
		// Check if sorted
//		for(Pair<String, Integer> elem : trainSentenceList) {
//			System.out.println(elem.first() + "\n" + elem.second());
//		}
		
		int no_of_sentences = trainSentenceList.size();
		int ans = 0, iterations = 0;
		while(!trainSentenceList.isEmpty() && iterations < 20) {
			int no_of_words = 0;
			while(no_of_words <= 1500 && no_of_sentences > 0) {
				// rand = new Random();
				// int val = rand.nextInt(no_of_sentences);
				// System.out.println(val);
				Pair <String, Integer> elem = trainSentenceList.get(0);
				seedSentenceList.add(elem);
				trainSentenceList.remove(0);
				no_of_words += elem.second();
				no_of_sentences -= 1;
				ans += 1;
			}
			if(no_of_sentences <= 0) break;
			System.out.printf("Number of words = %d. Number of Sentences = %d\nseed size = %d, training size = %d\n", no_of_words, ans, seedSentenceList.size(), trainSentenceList.size());
			
			writeSentencesToFile(seedSentenceList, seedPath);
			writeSentencesToFile(trainSentenceList, trainPath);
			predictedParses = TrainAndTest(seedPath, trainPath);
			no_of_sentences = trainSentenceList.size();
			iterations++;
		}
		predictedParses = TrainAndTest(seedPath, testPath);
	}
	
}
