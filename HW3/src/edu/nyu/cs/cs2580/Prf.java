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
	
	public String queryExpansion() throws IOException {
		ArrayList<DocumentIndexed> docs = new ArrayList<DocumentIndexed>();
		for (ScoredDocument sd : scoredDocs) {
			docs.add((DocumentIndexed)sd.get_doc());
		}

		// read and organize the top k documents term frequency
		HashMap<String, Integer> docTermFrequency;
		long docsTotalTermFrequency = 0;
		for (DocumentIndexed doc : docs) {
			docTermFrequency = doc.getDocTermFrequency();
			for (String term : docTermFrequency.keySet()) {
				int newFreq = 0;
				if (termFrequency.containsKey(term)) newFreq = termFrequency.get(term) + docTermFrequency.get(term);					
				else newFreq = docTermFrequency.get(term);
				termFrequency.put(term, newFreq);
			}
			//System.out.println(doc.getTitle() + ", " + doc.getDocTotalTermFrequency());
			docsTotalTermFrequency += doc.getDocTotalTermFrequency();
		}
			
		// remove stop word		
		int stopwords = 0;
		ArrayList<String> terms = new ArrayList<String>(termFrequency.keySet());
		for (String term : terms) {
			double freq = (double) termFrequency.get(term) / (double) docsTotalTermFrequency;
			if (freq > 0.005) {
				//System.out.println(term + ", " + freq);
				termFrequency.remove(term);
				stopwords++;
			}
		}
		//System.out.println("remove " + stopwords + " stopwords");
		
		// get the most numTerms frequent terms
		ArrayList<Integer> freqValues = new ArrayList<Integer>(termFrequency.values());
		Collections.sort(freqValues, Collections.reverseOrder());
		
		ArrayList<String> resultTerms = new ArrayList<String>();
		for (String term : termFrequency.keySet()) {
			if (freqValues.indexOf(termFrequency.get(term)) < numTerms) {
				resultTerms.add(term);
			}
		}
		//resultTerms = (ArrayList<String>) resultTerms.subList(0, numTerms - 1);
		
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < numTerms; ++i) {
			results.add(resultTerms.get(i));
		}
		
		long totalTermFrequency = 0;		
		for (String term : results) {
			totalTermFrequency += termFrequency.get(term);
		}
		
		// output the result
//		String folderName = option._indexPrefix + "/prf";
//		File outputFolder = new File(folderName);
//		if (outputFolder.exists() == false) outputFolder.mkdir();
//		String fileName = folderName + "/" + query + ".tsv";	
//		File outputFile = new File(fileName);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		StringBuilder sb = new StringBuilder();
		
		for (String term : resultTerms) {
			double prob = (double) termFrequency.get(term) / totalTermFrequency;
			sb.append(term + "\t" + prob + "\n");
			System.out.println(term + "\t" + prob + "\n");
		}
//		bw.close();
		
		//System.out.println("Finished expansion " + query);
		return sb.toString();
	}
}
