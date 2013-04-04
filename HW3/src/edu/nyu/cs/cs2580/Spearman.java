package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * cs2580: For calculating Spearman's rank correlation coefficient.
 * @author ZhiZeng
 * Apr 3, 2013
 */
public class Spearman {
	// store the pagerank values
	private static HashMap<String, Double> pagerankValues = new HashMap<String, Double>();
	// store the numview values
	private static HashMap<String, Integer> numViewValues = new HashMap<String, Integer>();
	// store the rank of pagerank value and numview value of each file
	private static HashMap<String, ArrayList<Double>> fileRanks = new HashMap<String, ArrayList<Double>>();
	
	/**
	 * load data from disk.
	 * @param prFileName
	 * @param nvFileName
	 * @throws IOException
	 */
	public static void loadData(String prFileName, String nvFileName) throws IOException {
		// load page rank values
		File pagerankFile = new File(prFileName);
		BufferedReader br = new BufferedReader(new FileReader(pagerankFile));
		String content;
		
		while ((content = br.readLine()) != null) {
			String[] results = content.split(":");
			// read the content into hashmap
			pagerankValues.put(results[0], Double.parseDouble(results[1]));
		}
		br.close();
		
		// load numView values
		File numViewFile = new File(nvFileName);
		br = new BufferedReader(new FileReader(numViewFile));

		while ((content = br.readLine()) != null) {
			String[] results = content.split(":");
			// read the content into the hashmap
			numViewValues.put(results[0], Integer.parseInt(results[1]));
		}		
		br.close();
		
		return;
	}
	
	/**
	 * compute the rank of pagerank value and numview value of each file
	 */
	public static void computeRanks() {
		// sort the pagerank values and numView values in descending order without duplicates
		ArrayList<Double> pgValues = new ArrayList<Double>(pagerankValues.values());
		ArrayList<Integer> nvValues = new ArrayList<Integer>(numViewValues.values());
		
		Collections.sort(pgValues, Collections.reverseOrder());
		Collections.sort(nvValues, Collections.reverseOrder());
		//System.out.println("pagerank: " + pgValues.size() + ", numview: " + nvValues.size());
		
		// compute each file's rank
		ArrayList<Double> ranks; // index 0 is pgValueRank, index 1 is nvValueRank		
		for (String file : pagerankValues.keySet()) {
			ranks = new ArrayList<Double>();
			double x = 0.0, y = 0.0;
			int firstIndex = 0, lastIndex = 0;
			
			// calculate x
			firstIndex = pgValues.indexOf(pagerankValues.get(file));
			lastIndex = pgValues.lastIndexOf(pagerankValues.get(file));
			
			if (firstIndex == lastIndex) x = firstIndex; // no duplicates
			else { // handle duplicates
				int sum = 0;
				for (int i = firstIndex; i <= lastIndex; ++i) {
					sum += i;
				}
				x = sum / (lastIndex - firstIndex + 1);
			}
			
			// calculate y
			firstIndex = nvValues.indexOf(numViewValues.get(file));
			lastIndex = nvValues.lastIndexOf(numViewValues.get(file));
			
			if (firstIndex == lastIndex) y = firstIndex; // no duplicates
			else { // handle duplicates
				int sum = 0;
				for (int i = firstIndex; i <= lastIndex; ++i) {
					sum += i;
				}
				y = sum / (lastIndex - firstIndex + 1);
			}
			
			// add x and y to the map
			ranks.add(x);
			ranks.add(y);
//			if (pgValues.indexOf(pagerankValues.get(file)) == 0) System.out.println("pagerank first: " + file);
//			if (nvValues.indexOf(numViewValues.get(file)) == 0) System.out.println("numview first: " + file);
			fileRanks.put(file, ranks);
		}
		
		return;
	}
	
	/**
	 * compute the spearman correlation coefficient
	 */
	public static void computeCoefficient() {
		int fileNums = fileRanks.size();
		double x = 0.0, y = 0.0;
		double coefficient = 0.0;
				
		// calculate coefficient
		double xySum = 0.0;
		for (String file : fileRanks.keySet()) {
			x = fileRanks.get(file).get(0);
			y = fileRanks.get(file).get(1);
			//System.out.println(x + " " + y);
			xySum += (x - y) * (x - y);
		}
		System.out.println("xySum is " + xySum + ", n is " + fileNums);
		coefficient = 1 - ((6 * xySum) / (fileNums * (fileNums * fileNums - 1)));
		
		System.out.println("The Spearman correlation coefficient is:" + coefficient);
		return;
	}
	

	/**
	 * @param args
	 * args[1]: path to pagerank file
	 * args[2]: path to numView file
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Must provide two arguments: path-to-pagerank path-to-numView");
			System.exit(-1);
		}
		
		try {
			String prFileName = args[0];
			String nvFileName = args[1];
			
			Spearman.loadData(prFileName, nvFileName);
			Spearman.computeRanks();
			Spearman.computeCoefficient();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
