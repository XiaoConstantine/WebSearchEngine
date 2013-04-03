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
	private static HashMap<String, ArrayList<Integer>> fileRanks = new HashMap<String, ArrayList<Integer>>();
	
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
		ArrayList<Double> pgValues = new ArrayList<Double>(new HashSet<Double>(pagerankValues.values()));
		ArrayList<Integer> nvValues = new ArrayList<Integer>(new HashSet<Integer>(numViewValues.values()));
		
		Collections.sort(pgValues, Collections.reverseOrder());
		Collections.sort(nvValues, Collections.reverseOrder());
//		System.out.println("pagerank: " + pgValues.size() + ", numview: " + nvValues.size());
		
		// compute each file's rank
		ArrayList<Integer> ranks; // index 0 is pgValueRank, index 1 is nvValueRank		
		for (String file : pagerankValues.keySet()) {
			ranks = new ArrayList<Integer>();
			ranks.add(pgValues.indexOf(pagerankValues.get(file)));
			ranks.add(nvValues.indexOf(numViewValues.get(file)));
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
		double z = 0.0;
		int x = 0, y = 0;
		double coefficient = 0.0;
		
		// calculate z
		int sum = 0;
		for (String file : fileRanks.keySet()) {
			x = fileRanks.get(file).get(0);
			sum += x;
		}
		z = sum / fileNums;
		
		// calculate coefficient
		double xzyzSum = 0.0, xzSquareSum = 0.0, yzSquareSum = 0.0;
		for (String file : fileRanks.keySet()) {
			x = fileRanks.get(file).get(0);
			y = fileRanks.get(file).get(1);
			xzyzSum += (x - z) * (y - z);
			xzSquareSum += (x - z) * (x - z);
			yzSquareSum += (y - z) * (y - z);
		}
		coefficient = xzyzSum / (xzSquareSum * yzSquareSum);
		
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
