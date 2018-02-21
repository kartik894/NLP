package nlp.lm;

import java.io.*;
import java.util.*;

/** 
 * @author Kartik Sathyanarayanan
 * A simple bigram language model that uses simple fixed-weight interpolation
 * with a unigram model for smoothing.
*/

public class BidirectionalBigramModel {
	
	public BigramModel b;
	
	public BackwardBigramModel bb;
	
	/** Interpolation weight for bigram model */
    public double lambda1 = 0.5;

    /** Interpolation weight for backwardbigram model */
    public double lambda2 = 0.5;
	
	public BidirectionalBigramModel() {
		b = new BigramModel();
		bb = new BackwardBigramModel();
	}
	
	public static int wordCount (List<List<String>> sentences) {
		int wordCount = 0;
		for (List<String> sentence : sentences) {
		    wordCount += sentence.size();
		}
		return wordCount;
	}
	
	public void train(List<List<String>> sentences) {
		System.out.println("Training...");
		b.train(sentences);
		bb.train(sentences);
	}
	
	/** Like test1 but excludes predicting end-of-sentence when computing perplexity */
    public void test2 (List<List<String>> sentences) {
    	double totalLogProb = 0;
		double totalNumTokens = 0;
		for (List<String> sentence : sentences) {
		    totalNumTokens += sentence.size();
		    double sentenceLogProb = sentenceLogProb2(sentence);
		    //	    System.out.println(sentenceLogProb + " : " + sentence);
		    totalLogProb += sentenceLogProb;
		}
		double perplexity = Math.exp(-totalLogProb / totalNumTokens);
		System.out.println("Word Perplexity = " + perplexity );
    }
    
    /** Like sentenceLogProb but excludes predicting end-of-sentence when computing prob */
    public double sentenceLogProb2 (List<String> sentence) {
		double[] bigramTokenProbs = b.sentenceTokenProbs(sentence);
		double[] backwardbigramTokenProbs = bb.sentenceTokenProbs(sentence);
	
		double totalLogProb = 0.0;
		
		for (int i = 0, j = bigramTokenProbs.length - 2; i < bigramTokenProbs.length && j >= 0 ; i++, j--) {
			totalLogProb += Math.log(lambda1 * bigramTokenProbs[i] + lambda2 * backwardbigramTokenProbs[j]);
		}
		return totalLogProb;
    }
    
    
    /** Train and test a bi-directional bigram model.
     *  Command format: "nlp.lm.BidirectionalBigramModel [DIR]* [TestFrac]" where DIR 
     *  is the name of a file or directory whose LDC POS Tagged files should be 
     *  used for input data; and TestFrac is the fraction of the sentences
     *  in this data that should be used for testing, the rest for training.
     *  0 < TestFrac < 1
     *  Uses the last fraction of the data for testing and the first part
     *  for training.
     */
    public static void main(String[] args) throws IOException {
	// All but last arg is a file/directory of LDC tagged input data
	File[] files = new File[args.length - 1];
	for (int i = 0; i < files.length; i++) 
	    files[i] = new File(args[i]);
	// Last arg is the TestFrac
	double testFraction = Double.valueOf(args[args.length -1]);
	// Get list of sentences from the LDC POS tagged input files
	List<List<String>> sentences = 	POSTaggedFile.convertToTokenLists(files);
	int numSentences = sentences.size();
	// Compute number of test sentences based on TestFrac
	int numTest = (int)Math.round(numSentences * testFraction);
	// Take test sentences from end of data
	List<List<String>> testSentences = sentences.subList(numSentences - numTest, numSentences);
	// Take training sentences from start of data
	List<List<String>> trainSentences = sentences.subList(0, numSentences - numTest);
	System.out.println("# Train Sentences = " + trainSentences.size() + 
			   " (# words = " + wordCount(trainSentences) + 
			   ") \n# Test Sentences = " + testSentences.size() +
			   " (# words = " + wordCount(testSentences) + ")");
	// Create a bigram model and train it.
	BidirectionalBigramModel model = new BidirectionalBigramModel();
	
	model.train(trainSentences);
	model.test2(trainSentences);
	
	System.out.println("Testing...");
	model.test2(testSentences);
	
//	// Test bigram model on training data using test and test2
//	b_model.test(trainSentences);
//	b_model.test2(trainSentences);
//	
//	// Test backward bigram model on training data using test and test2
//	bb_model.test(trainSentences);
//	bb_model.test2(trainSentences);
	
//	System.out.println("Testing...");
	
//	// Test bigram model on test data using test and test2
//	b_model.test(testSentences);
//	b_model.test2(testSentences);
//	
//	// Test bigram model on test data using test and test2
//	bb_model.test(testSentences);
//	bb_model.test2(testSentences);
    }

}
