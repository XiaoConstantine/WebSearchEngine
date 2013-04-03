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
import java.util.Iterator;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
	// map each file name to an integer, which is the index of the vector
	private Vector<String> documents = new Vector<String>();
	// key is the source file index, values are the list of outlink file indices
	private HashMap<Integer, ArrayList<Integer>> corpusGraph = new HashMap<Integer, ArrayList<Integer>>();
	// key is the file name, value is the page rank value
	private HashMap<Integer, Double> pagerankValues = new HashMap<Integer, Double>();
	// variables for calculating the page rank value
	private final static double lambda = 0.9;
	private final static int iteration = 2;

	public CorpusAnalyzerPagerank(Options options) {
		super(options);
	}

	/**
	 * This function processes the corpus as specified inside {@link _options}
	 * and extracts the "internal" graph structure from the pages inside the
	 * corpus. Internal means we only store links between two pages that are
	 * both inside the corpus.
	 * 
	 * Note that you will not be implementing a real crawler. Instead, the
	 * corpus you are processing can be simply read from the disk. All you need
	 * to do is reading the files one by one, parsing them, extracting the links
	 * for them, and computing the graph composed of all and only links that
	 * connect two pages that are both in the corpus.
	 * 
	 * Note that you will need to design the data structure for storing the
	 * resulting graph, which will be used by the {@link compute} function.
	 * Since the graph may be large, it may be necessary to store partial graphs
	 * to disk before producing the final graph.
	 * 
	 * @throws IOException
	 */
	@Override
	public void prepare() throws IOException {
		System.out.println("Preparing " + this.getClass().getName());
		
		File folder = new File(_options._corpusPrefix);
		File[] files = folder.listFiles();

        System.out.println("Read file");
		int docid = 0;
		int round = 0;
		
		// map each file name to an integer
		int inValidNum = 0;
		for (int i = 0; i < files.length; i++) {
			// ignore the hidden files
			System.out.println(files[i].getName());
			if(isValidDocument(files[i]) == false){
                 inValidNum++;
			}else{
                 documents.add((i-inValidNum), files[i].getName());
			}
		}
        System.out.println("Finished read file");      
		if(files.length % 500 == 0) round = files.length/500;
		else round = files.length/500 + 1;
		
		for(int i = 0; i < round; ++i){
			for(int j = 0; j < 500 && (i*500 + j < files.length); ++j){
				if (isValidDocument(files[docid])) {
					processDocument(files[docid]);
					//++docid;
				}
				docid++;
			}
			writeGraph(i);
			
            System.out.println("round" + i + "\n");
		}

		return;
	}

	/**
	 * find out the out links in a file and add them to the graph
	 * @param file
	 * @throws IOException
	 */
	public void processDocument(File file) throws IOException {
		HeuristicLinkExtractor extractor = new HeuristicLinkExtractor(file);
		
		String fileName = extractor.getLinkSource();
		String outLink;
		int index = documents.indexOf(fileName);
		ArrayList<Integer> outLinks = new ArrayList<Integer>();
		
		// find the links which point to the file in corpus
		while ((outLink = extractor.getNextInCorpusLinkTarget()) != null) {
			if (documents.contains(outLink)) outLinks.add(documents.indexOf(outLink));
		}
		// add the file information to the graph
		corpusGraph.put(index, outLinks);
		
		return;
	}
	
	/**
	 * write the current graph to disk
	 * @param round
	 */
	public void writeGraph(int round) {
		try {
			String fileName = _options._indexPrefix + "/corpusGraph";
			File graphFile = new File(fileName);
			ArrayList<Integer> outLinks;
			StringBuilder sb;
			
			// first round, delete the previous graph file and write a new one			
			if (round == 0) {
				graphFile.delete();
				graphFile.createNewFile();
			}
			
			// append current graph to the end of the file
			BufferedWriter bw = new BufferedWriter(new FileWriter(graphFile, true));
			
			ArrayList<Integer> sourceFiles = new ArrayList<Integer>(corpusGraph.keySet());
			Collections.sort(sourceFiles);
			
			for (Integer source : sourceFiles) {
				sb = new StringBuilder();
				// source file index
				sb.append(source.toString() + ":");
				// outlink file indices
				outLinks = corpusGraph.get(source);
				for (int i = 0; i < outLinks.size(); ++i) {
					if (i == outLinks.size() - 1) sb.append(outLinks.get(i).toString());
					else sb.append(outLinks.get(i).toString() + ",");
				}
				sb.append("\n");
				bw.write(sb.toString());
			}
			bw.close();
			
			// clear the current graph
			for (ArrayList<Integer> links : corpusGraph.values()) {
				links.clear();
			}
			corpusGraph.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * This function computes the PageRank based on the internal graph generated
	 * by the {@link prepare} function, and stores the PageRank to be used for
	 * ranking.
	 * 
	 * Note that you will have to store the computed PageRank with each document
	 * the same way you do the indexing for HW2. I.e., the PageRank information
	 * becomes part of the index and can be used for ranking in serve mode.
	 * Thus, you should store the whatever is needed inside the same directory
	 * as specified by _indexPrefix inside {@link _options}.
	 * 
	 * @throws IOException
	 */
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());
		
//		File folder = new File(_options._corpusPrefix);
//		File[] files = folder.listFiles();
//		
//		// map each file name to an integer
//		for (int i = 0; i < files.length; ++i) {
//			// ignore the hidden files
//			if (isValidDocument(files[i])) documents.add(i, files[i].getName());
//		}
		
		readGraph();		
		
		System.out.println("lambda: " + lambda + ", iteration: " + iteration);
		// initialize pagerankValues
		int fileNums = documents.size();
		for (int i = 0; i < fileNums; ++i) {
			pagerankValues.put(i, (double) (1.0 / fileNums));
		}

		for (int i = 0; i < iteration; ++i) {
			computePagerank();
		}
		System.out.println("Computing finished, start writing");
		
		writePagerank();
		return;
	}

	/**
	 * read the graph from disk.
	 */
	public void readGraph() {
		try {
			String fileName = _options._indexPrefix + "/corpusGraph";
			File graphFile = new File(fileName);
			Integer source;
			ArrayList<Integer> outLinks;
	
			BufferedReader br = new BufferedReader(new FileReader(graphFile));
			
			String content;
			while ((content = br.readLine()) != null) {
				String[] results = content.split(":");
				// source file index
				source = Integer.parseInt(results[0]);
				// outlinks of source file
				outLinks = new ArrayList<Integer>();
				if (results.length == 2) {
					String[] links = results[1].split(",");
					for (int i = 0; i < links.length; ++i) {
						outLinks.add(Integer.parseInt(links[i]));
					}
				}
				corpusGraph.put(source, outLinks);
				//System.out.println("file " + source + " have " + corpusGraph.get(source).size() + " out links");
			}
			
			br.close();
			
			System.out.println(documents.size() + " document, " + corpusGraph.size() + " files, readGraph finished!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * compute the pagerank values for every file in the corpus
	 */
	public void computePagerank() {
		ArrayList<Integer> outLinks;
		HashMap<Integer, Double> tmpPagerankValues = new HashMap<Integer, Double>();
		int outDegree = 0, fileNums = documents.size();
		double tmpValue = 0.0, danglingValue = 0.0, avgDanglingValue = 0.0;

		//initialize the tmp map
		for (int i = 0; i < fileNums; ++i) {
			tmpPagerankValues.put(i, 0.0);
		}

		ArrayList<Integer> sourceFiles = new ArrayList<Integer>(corpusGraph.keySet());
		Collections.sort(sourceFiles);
		
		for (Integer source : sourceFiles) {
			outLinks = corpusGraph.get(source.intValue());
			outDegree = outLinks.size();
			
			// calculate the pagerank value for every outlink
			for (Integer outLink : outLinks) {
				tmpValue = tmpPagerankValues.get(outLink.intValue()) + (double) (pagerankValues.get(source.intValue()) / outDegree);
				tmpPagerankValues.put(outLink.intValue(), tmpValue);
			}
			
//			// for dangling node without outlink
//			if (outDegree == 0) {
//				danglingValue += pagerankValues.get(source.intValue());
//			}			
		}
		
//		// distribute the danglingValue to every node
//		avgDanglingValue = danglingValue / (double) fileNums;
		for (int i = 0; i < fileNums; ++i) {
			pagerankValues.put(i, tmpPagerankValues.get(i) + avgDanglingValue);
		}
		
		// calculate the final pagerank value
		for (int i = 0; i < fileNums; ++i) {
			double value = lambda * pagerankValues.get(i) + (1 - lambda) * (double) (1.0 / fileNums);
			pagerankValues.put(i, value);
		}
		
	}
	
	/**
	 * write the pagerank values to disk.
	 * format is "filename:value".
	 */
	public void writePagerank() {
		try {
			String fileName = _options._indexPrefix + "/pagerank";
			File pagerankFile = new File(fileName);
			ArrayList<Integer> outLinks;
			StringBuilder sb;
			
			// delete the previous graph file and write a new one			
			pagerankFile.delete();
			pagerankFile.createNewFile();

			BufferedWriter bw = new BufferedWriter(new FileWriter(pagerankFile));
			
			ArrayList<Integer> sourceFiles = new ArrayList<Integer>(pagerankValues.keySet());
			Collections.sort(sourceFiles);
			
			for (Integer source : sourceFiles) {
				sb = new StringBuilder();
				// source file name
				sb.append(documents.get(source.intValue()).toString() + ":");
				// pagerank value
				sb.append(pagerankValues.get(source.intValue()).toString() + "\n");
				bw.write(sb.toString());
			}
			bw.close();
			
			System.out.println("Finished writing to " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * During indexing mode, this function loads the PageRank values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());

		String fileName = _options._indexPrefix + "/pagerank";
		File pagerankFile = new File(fileName);
		
		HashMap<String, Double> rankValues = new HashMap<String, Double>();
		String sourceFile;
		double value;

		BufferedReader br = new BufferedReader(new FileReader(pagerankFile));

		String content;
		while ((content = br.readLine()) != null) {
			String[] results = content.split(":");
			// source file
			sourceFile = results[0];
			// pagerank value of source file
			value = Double.parseDouble(results[1]);
			
			rankValues.put(sourceFile, value);
		}

		br.close();
		System.out.println(rankValues.size() + " files, load Pagerank finished!");
		// return the hashmap<filename, value>.
		return rankValues;
	}
}
