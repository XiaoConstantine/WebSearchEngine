package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Bhattacharyya {
	// format is <query, <term, prob>>
	private static HashMap<String, HashMap<String, Double>> prfResults = new HashMap<String, HashMap<String, Double>>();
	// format is <query1 <query2, coefficient>>
	private static HashMap<String, HashMap<String, Double>> bhResults = new HashMap<String, HashMap<String, Double>>();
	
	public static void readPrf(String prfFileName) {
		try {
			File prfFile = new File(prfFileName);
			
			BufferedReader br;
			BufferedReader prfBr = new BufferedReader(new FileReader(prfFile));
			String prfInfo;
			while ((prfInfo = prfBr.readLine()) != null){
				String[] tmpResults = prfInfo.split(":");
				String query = tmpResults[0];
				String fileName = tmpResults[1];
				br = new BufferedReader(new FileReader(fileName));
				
				String content;
				HashMap<String, Double> termProb = new HashMap<String, Double>();
				while ((content = br.readLine()) != null) {
					String[] results = content.split("\t");
					termProb.put(results[0], Double.parseDouble(results[1]));
				}
				prfResults.put(fileName, termProb);
				br.close();
			}
			prfBr.close();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void calculateCoef() {
		System.out.println("Computing Bhattacharyya coefficient...");
		for (String query1 : prfResults.keySet()) {
			// map for storing all the pair in query1
			HashMap<String, Double> tmpMap = new HashMap<String, Double>();
			for (String query2 : prfResults.keySet()) {
				if (query1.equals(query2) == false) {
					double coef = 0.0;
					for (String term : prfResults.get(query1).keySet()) {
						// only compute the term which is contained in both queries
						if (prfResults.get(query2).containsKey(term)) {
							coef += Math.sqrt(prfResults.get(query1).get(term) * prfResults.get(query2).get(term));						
						}
					}
					tmpMap.put(query2, coef);
				}
			}
			bhResults.put(query1, tmpMap);
		}
	}
	
	public static void writePrf(String outputName) {
		try {
			File outputFile = new File(outputName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			for (String query1 : bhResults.keySet()) {
				for (String query2 : bhResults.get(query1).keySet()) {
					bw.write(query1 + "\t" + query2 + "\t" + bhResults.get(query1).get(query2) + "\n");
				}
			}
			bw.close();
			
			System.out.println("Finished writing to " + outputName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Must have 2 arguments: path-to-prf-output-directory path-to-output");
			System.exit(-1);
		}
		
		String prfFileName = args[0];
		String outputName = args[1];
		
		readPrf(prfFileName);
		calculateCoef();
		writePrf(outputName);

	}

}
