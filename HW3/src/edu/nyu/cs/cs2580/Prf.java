package edu.nyu.cs.cs2580;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

public class Prf {
	private Vector<ScoredDocument> scoredDocs = new Vector<ScoredDocument>();
	private int numTerms;
	private String query;
	private Options option;
	private HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
	
	public Prf(Vector<ScoredDocument> scoredDocs, QueryHandler.CgiArguments cgiArgs, Options option) {
		this.scoredDocs = scoredDocs;
		this.numTerms = cgiArgs.get_numTerms();
		this.query = cgiArgs._query;
		this.option = option;
	}
	
	public void queryExpansion() throws IOException {
		ArrayList<DocumentIndexed> docs = new ArrayList<DocumentIndexed>();
		for (ScoredDocument sd : scoredDocs) {
			docs.add((DocumentIndexed)sd.get_doc());
		}
		
		// read and organize the top k documents term frequency
		HashMap<String, Integer> docTermFrequency;
		for (DocumentIndexed doc : docs) {
			docTermFrequency = doc.getDocTermFrequency();
			for (String term : docTermFrequency.keySet()) {
				int newFreq = 0;
				if (termFrequency.containsKey(term)) newFreq = termFrequency.get(term) + docTermFrequency.get(term);					
				else newFreq = docTermFrequency.get(term);
				termFrequency.put(term, newFreq);
			}
		}
		
		// get the most numTerms frequent terms
		ArrayList<Integer> freqValues = new ArrayList<Integer>(termFrequency.values());
		Collections.sort(freqValues, Collections.reverseOrder());
		
		ArrayList<String> resultTerms = new ArrayList<String>();
		for (String term : termFrequency.keySet()) {
			if (freqValues.indexOf(termFrequency.get(term)) < numTerms) {
				resultTerms.add(term);
			}
		}
		resultTerms = (ArrayList<String>) resultTerms.subList(0, numTerms - 1);
		long totalTermFrequency = 0;		
		for (String term : resultTerms) {
			totalTermFrequency += termFrequency.get(term);
		}
		
		// output the result
		String folderName = option._indexPrefix + "/prf";
		String fileName = folderName + "/" + query + ".tsv";	
		File outputFile = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		for (String term : resultTerms) {
			double prob = (double) termFrequency.get(term) / totalTermFrequency;
			bw.write(term + "\t" + prob + "\n");
		}
		
		return;
	}
}
