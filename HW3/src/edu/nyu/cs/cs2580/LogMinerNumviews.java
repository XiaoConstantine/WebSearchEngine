package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {
	// key is the file name, value is the numView
	HashMap<String, Integer> numViewValues = new HashMap<String, Integer>();
	
	
	public LogMinerNumviews(Options options) {
		super(options);
	}

	/**
	 * This function processes the logs within the log directory as specified by
	 * the {@link _options}. The logs are obtained from Wikipedia dumps and have
	 * the following format per line: [language]<space>[article]<space>[#views].
	 * Those view information are to be extracted for documents in our corpus
	 * and stored somewhere to be used during indexing.
	 * 
	 * Note that the log contains view information for all articles in Wikipedia
	 * and it is necessary to locate the information about articles within our
	 * corpus.
	 * 
	 * @throws IOException
	 */
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());

		// initialize the numview for all the file in corpus
		File folder = new File(_options._corpusPrefix);
		File[] files = folder.listFiles();
		
		for (int i = 0; i < files.length; ++i) {
			numViewValues.put(files[i].getName(), 0);
		}
		
		computeNumView();
		writeNumView();

		return;
	}
	
	/**
	 * read the log and update numview
	 */
	public void computeNumView() {
		try {
			String fileName = _options._logPrefix + "/20130301-160000.log";
			File logFile = new File(fileName);
	
			BufferedReader br = new BufferedReader(new FileReader(logFile));
	
			String content;
			while ((content = br.readLine()) != null) {
				String[] results = content.split(" ");
				//System.out.println(results[0] + "\t" + results[1] + "\t" + results[2]);
				// only handle the file in corpus
				if (numViewValues.containsKey(results[1])) {
					if (results.length == 3 && results[2].matches("[0-9]*")){
						int newValue = numViewValues.get(results[1]) + Integer.parseInt(results[2]);
						numViewValues.put(results[1], newValue);
					}
				}
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * write the numView of each file in corpus to disk
	 * format: "fileName:numView"
	 */
	public void writeNumView() {
		try {
			String fileName = _options._indexPrefix + "/numView";
			File numViewFile = new File(fileName);
	
			BufferedWriter bw = new BufferedWriter(new FileWriter(numViewFile));
			
			ArrayList<String> files = new ArrayList<String>(numViewValues.keySet());
			Collections.sort(files);
			
			StringBuilder sb;
			for (String file : files) {
				sb = new StringBuilder();
				// file name
				sb.append(file + ":");
				// numView
				sb.append(numViewValues.get(file) + "\n");
				bw.write(sb.toString());
			}
			
			bw.close();
			
			System.out.println("Finished writing to " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * During indexing mode, this function loads the NumViews values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		
		String fileName = _options._indexPrefix + "/numView";
		File numViewFile = new File(fileName);

		BufferedReader br = new BufferedReader(new FileReader(numViewFile));

		String content;
		while ((content = br.readLine()) != null) {
			String[] results = content.split(":");
			// read the content into the hashmap
			numViewValues.put(results[0], Integer.parseInt(results[1]));
		}
		
		br.close();
		
		// return a hash map, key is the file name, value is the numView
		return numViewValues;
	}
}
