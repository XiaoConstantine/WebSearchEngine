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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer {
	/*
	 * Invert-Index DS: LinkedList with Term & Doc_id which appeared.Since we
	 * have method: corpusDocFrequencyByTerm(Term) & corpusTermFrequency(Term).
	 * We should have a HashMap<String, Integer> to return like hw1?
	 */

	// Map list, 6 maps in total
	// Map the term to the doc id that it appears in
	// map 0 for terms start with a-e, map 1 for terms start with f-j, map 2 for terms start with k-o,
	// map 3 for terms start with p-t, map 4 for terms start with u-z, map 5 for numbers
	private List<HashMap<String, Vector<Integer>>> dicts = new ArrayList<HashMap<String, Vector<Integer>>>();
	
	// Maps the number of the times the term appears in the corpus
	private Map<String, Integer> termFrequency = new HashMap<String, Integer>();
	// Stores all Document in memory.
	private Vector<Document> documents = new Vector<Document>();
	// Track the the total term frequency of a document
	private long docTotalTermFrequency = 0;

	/**
	 * Constructor
	 * 
	 * @param options
	 */
	public IndexerInvertedDoconly(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
		// 6 dictionary maps
		// map 0 for terms start with a-e, map 1 for terms start with f-j, map 2 for terms start with k-o,
		// map 3 for terms start with p-t, map 4 for terms start with u-z, map 5 for numbers
		for (int i = 0; i < 6; ++i)	dicts.add(new HashMap<String, Vector<Integer>>());
	}

	@Override
	public void constructIndex() throws IOException {
		String dirPath = _options._corpusPrefix; // simple directory
		
		if (dirPath.contains("simple")) {
			// for simple corpus
			String corpusFile = dirPath + "/corpus.tsv";
			System.out.println("Construct index from: " + corpusFile);
			
			BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					//System.out.println(_numDocs);
					parseDocument(line, _numDocs);
					++_numDocs;
				}
			} finally {
				reader.close();
			}
		} else { //for wiki corpus 
			File dir = new File(dirPath); 
			File[] files = dir.listFiles(); // all the files
			int fileNum = files.length;
			int roundTime;
			
			if (fileNum % 500 == 0) roundTime = fileNum / 500;
			else roundTime = fileNum / 500 + 1;

			for (int i = 0; i < roundTime; ++i) {
				System.out.println("rountTime " + i);
				for (int j = 0; j < 500 && (i * 500 + j < fileNum); ++j) {
					processDocument(files[_numDocs], _numDocs);
					++_numDocs;
				}
				System.out.println(_numDocs + " files");
				if (i == 0) { // first round, write the data into tmp files
					writeIndex();
				} else { // merge the data from tmp files and current data, and write to new files
					mergeAndWriteIndex(i, roundTime);
				}
			}
		}
		
		System.out.println("Finish indexing " + _numDocs + " files");
		System.out.println("Terms: " + _totalTermFrequency);
		System.out.println("Unique terms: " + termFrequency.keySet().size());
		
		// remove the stop words
		Vector<String> stopwords = new Vector<String>();

		for (String stopword: termFrequency.keySet()) {
			if ((float) termFrequency.get(stopword) / _totalTermFrequency > 0.06) {
				stopwords.add(stopword);
			}
		}
		
		for (String stopword: stopwords) {
			removeStopword(stopword);
		}
		
		
		System.out.println("Removed " + stopwords.size() + " stopwords");
		System.out.println("Terms without stopwords: " + _totalTermFrequency);
		

		// write the termFrequency and documents to disk
		BufferedWriter bw;
		String termFreqFile = _options._indexPrefix + "/termFrequency.idx";
		System.out.println("TermFrequency: writing to " + termFreqFile);
		bw = new BufferedWriter(new FileWriter(termFreqFile));
		writeTermFrequency(bw);
		bw.close();
		
		String docFile = _options._indexPrefix + "/documents.idx";
		System.out.println("Documents: writing to " + docFile);
		bw = new BufferedWriter(new FileWriter(docFile));
		writeDocuments(bw);
		bw.close();
	}

	
	/**
	 * Write the termFrequency to file
	 * @param bw
	 * @throws IOException
	 */
	public void writeTermFrequency(BufferedWriter bw) throws IOException {
		StringBuilder sb;
		// termFrequency format: term; frequency
		for (String term : termFrequency.keySet()) {
			sb = new StringBuilder();
			sb.append(term + ";");
			sb.append(termFrequency.get(term) + "\n");;
			bw.write(sb.toString());
		}
	}
	
	/**
	 * Write the documents to file
	 * @param bw
	 * @throws IOException
	 */
	public void writeDocuments(BufferedWriter bw) throws IOException {
		StringBuilder sb;
		// doc format: docid, title, url, pageRank, numViews
		for (Document doc : documents) {
			sb = new StringBuilder();
			sb.append(doc._docid + ";");
			sb.append(doc.getTitle() + ";");
			sb.append(doc.getUrl() + ";");
			sb.append(doc.getPageRank() + ";");
			sb.append(doc.getNumViews() + "\n");
			bw.write(sb.toString());
		}
	}

	/**
	 * For parsing simple documents
	 * @param content
	 * @param docid
	 */
	public void parseDocument(String content, int docid) {
	    Scanner s = new Scanner(content).useDelimiter("\t");
		docTotalTermFrequency = 0;
		
	    String title = s.next();
		// parse the title
	    parseSimpleDoc(title, docid);
	    // parse the body
	    parseSimpleDoc(s.next(), docid);
	    
	    s.close();
	    //System.out.println("close scanner");
	    DocumentIndexed doc = new DocumentIndexed(docid,this);
	    doc.setTitle(title);
	    doc.setUrl(Integer.toString(docid));
	    doc.setDocTotalTermFrequency(docTotalTermFrequency);
	    //System.out.println("finish setting doc");
	    documents.add(docid, doc);
	    //System.out.println("Added doc to vector");
	}

	/**
	 * For parsing simple document
	 * @param content
	 * @param docTermFreq
	 * @param docid
	 */
	public void parseSimpleDoc(String content, int docid) {
		Scanner s = new Scanner(content); // split by white space
		
		while (s.hasNext()) {
			String token = stem(s.next());
			//System.out.println(token);
			
			// check the stemmed token
			processWord(token, docid);
		}
		
		s.close();
	}
	
	/**
	 * For processing the wiki documents
	 * 
	 * @param file
	 * @param docid
	 */
	public void processDocument(File file, int docid) throws IOException {
		// refresh the data
		docTotalTermFrequency = 0;
		// parse the title
		String title = file.getName();
		title = title.replaceAll("\\pP|\\pS|\\pC", " ");
		//System.out.println(title);
		Scanner s = new Scanner(title).useDelimiter(" ");
		while (s.hasNext()) {
			String token = stem(s.next());
			if (token.matches("[0-9a-z]*") == false || token.isEmpty()) continue;
			// check the stemmed token
			processWord(token, docid);
		}
		s.close();
		
		// read the file, set the doc
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			int scriptFlag = 0; // 0 means no script, 1 means <script>, 2 means <script ...>
			String line = null;

			while ((line = reader.readLine()) != null) {
				// remove the content in <script>
				while (line.contains("<script") || line.contains("</script>")) {
					if (scriptFlag == 2) { // in <script ...
						if (line.contains(">")) {
							line = line.substring(line.indexOf(">") + 1);
							scriptFlag = 1;
						}
					} 
					if (scriptFlag == 1) { // in <script> ... 
						if (line.contains("</script>")) {
							line = line.substring(line.indexOf("</script>") + 9);
							scriptFlag = 0;
						}
					}
					if (scriptFlag == 0) {
						if (line.contains("<script>")) {
							// parse the no script content and check the remain string
							processWikiDoc(line.substring(0, line.indexOf("<script>")), docid);
							scriptFlag = 1;
							line = line.substring(line.indexOf("<script>") + 8);
						} else if (line.contains("<script")) {
							// parse the no script content and check the remain string
							processWikiDoc(line.substring(0, line.indexOf("<script")), docid);
							scriptFlag = 2;
							line = line.substring(line.indexOf("<script") + 7);
							if (line.contains(">")) {
								line = line.substring(line.indexOf(">") + 1);
								scriptFlag = 1;
							}
						} 
					}
				}
				
				if (scriptFlag != 0) continue;
				
				// parse the content, add them into invertedList
				processWikiDoc(line, docid);
			}
			DocumentIndexed doc = new DocumentIndexed(docid,this);
			doc.setTitle(file.getName());
			doc.setUrl(file.getName());
			doc.setDocTotalTermFrequency(docTotalTermFrequency);
			documents.add(docid, doc);
		} finally {
			reader.close();
		}

	}

	/**
	 * Process the content of the document with docid
	 * @param content
	 * @param docid
	 */
	public void processWikiDoc(String content, int docid) {
		String pureText;
		pureText = content.replaceAll("<[^>]*>", " ");
		pureText = pureText.replaceAll("\\pP|\\pS|\\pC", " ");
		Scanner s = new Scanner(pureText).useDelimiter(" ");
		
		while (s.hasNext()) {
			String token = stem(s.next());
			// only consider numbers and english
			if (token.matches("[0-9a-z]*") == false || token.isEmpty()) continue;
			// check the stemmed token
			processWord(token, docid);	
		}
		s.close();
	}
	
	/**
	 * Process a word
	 * @param token
	 * @param docid
	 */
	public void processWord(String token, int docid) {
		Map<String, Vector<Integer>> dict = null;
		
		int dictIdx = (token.charAt(0) - 'a') / 5;
		if (dictIdx >= 0 && dictIdx < 5) dict = dicts.get(dictIdx);
		else if (dictIdx == 5) dict = dicts.get(4);
		else dict = dicts.get(5);
			
		Vector<Integer> tmp;
		if (dict.containsKey(token)) {
			tmp = dict.get(token);
			if (tmp.contains(docid) == false){
				tmp.add(docid); // add the doc to the term's appearance list
				dict.put(token, tmp);
			}
			termFrequency.put(token, termFrequency.get(token) + 1);
		} else { // new term for this round
			tmp = new Vector<Integer>();
			tmp.add(docid); // add the doc to the term's appearance list
			dict.put(token, tmp);
			if (termFrequency.containsKey(token)) termFrequency.put(token, termFrequency.get(token) + 1);
			else termFrequency.put(token, 1);
		}	
		++docTotalTermFrequency;
		++_totalTermFrequency;
	}
	
	/**
	 * Stem a token in documents, temporarily static for testing
	 * 
	 * @param token
	 * @return the stemmed token
	 */
	public String stem(String token) {
		token = token.toLowerCase();
		Stemmer stemmer = new Stemmer();
		stemmer.add(token.toCharArray(), token.toCharArray().length);
		stemmer.stem();
		return stemmer.toString();
	}
	
	/**
	 * Remove the stopword, update the index file
	 * @param stopword
	 * @throws IOException
	 */
	public void removeStopword(String stopword) throws IOException {
		_totalTermFrequency -= corpusTermFrequency(stopword);
		termFrequency.remove(stopword);
		
		String initial, fileName, tmpFileName;
		File file, tmpFile;
		
		int idx = (stopword.charAt(0) - 'a') / 5;
		switch (idx) {
			case 0: 
				initial = "a";
				break;
			case 1: 
				initial = "f";
				break;
			case 2:
				initial = "k";
				break;
			case 3:
				initial = "p";
			case 4:
			case 5:
				initial = "u";
				break;
			default:
				initial = "num";	
		}
		
		fileName = _options._indexPrefix + "/" + initial + ".idx";
		tmpFileName = _options._indexPrefix + "/" + initial + "Tmp" + ".idx";
		
		file = new File(fileName);
		tmpFile = new File(tmpFileName);
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
		
		String record, term;
		while ((record = br.readLine()) != null) {
			term = record.split(";")[0];
			if (term.equals(stopword)) continue;
			else {
				bw.write(record + "\n");
			}
		}
		br.close();
		bw.close();
		file.delete();
		tmpFile.renameTo(file);		
	}
	
	/**
	 * Clean the hashmaps 
	 */
	public void refresh() {
		for (HashMap<String, Vector<Integer>> dict : dicts) {
			dict.clear();
		}
	}
	
	/**
	 * For the first 2000 files, write the data to tmp files
	 * @throws IOException
	 */
	public void writeIndex() throws IOException {
		String fileName;
		List<String> orderedTerms;
		HashMap<String, Vector<Integer>> dict;
		BufferedWriter bw;
		
		// num terms
		fileName = _options._indexPrefix + "/numTmp0.idx";
		bw = new BufferedWriter(new FileWriter(fileName));
		dict = dicts.get(5);
		orderedTerms = new ArrayList<String>(dict.keySet());
		
		Collections.sort(orderedTerms);
		writeIndexHelper(bw, orderedTerms, dict);
		bw.close();
		
		//System.out.println("Write to " + fileName);
		
		// english terms
		for (int i = 0; i < 5; ++i) {
			char initial = (char) (i * 5 + 'a');
			fileName = _options._indexPrefix + "/" + initial + "Tmp0.idx";
			bw = new BufferedWriter(new FileWriter(fileName));
			dict = dicts.get(i);
			orderedTerms = new ArrayList<String>(dict.keySet());
			
			Collections.sort(orderedTerms);
			writeIndexHelper(bw, orderedTerms, dict);
			bw.close();
			//System.out.println("Write to " + fileName);
		}
		
		refresh();
	}
	
	/**
	 * Helper method for writeIndex
	 * @param bw
	 * @param orderedTerms
	 * @param dict
	 * @throws IOException
	 */
	public void writeIndexHelper(BufferedWriter bw, List<String> orderedTerms, 
				HashMap<String, Vector<Integer>> dict) throws IOException {
		StringBuilder sb;
		for (String term : orderedTerms) { // write the terms alphabetically
			sb = new StringBuilder();
			// separate term and its doc ids by semicolon
			sb.append(term + ";");
			// separate the doc ids by white space
			for (int docid : dict.get(term)) {
				sb.append(" " + docid);
			}
			sb.append("\n");
			bw.write(sb.toString());
		}
	}

	/**
	 * For the non-first round writing.
	 * Merge the current data and previous tmp files
	 * @param i
	 * @throws IOException 
	 */
	public void mergeAndWriteIndex(int currentRound, int roundTime) throws IOException {
		String oldFileName, newFileName;
		File oldFile, newFile;
		BufferedReader br;
		BufferedWriter bw;
		HashMap<String, Vector<Integer>> dict;
		List<String> orderedTerms;
		
		// num terms

		oldFileName = _options._indexPrefix + "/" + "numTmp" + (currentRound - 1) + ".idx";
		oldFile = new File(oldFileName);
		br = new BufferedReader(new FileReader(oldFile));
		
		//System.out.println("Read from " + oldFileName);
		
		if (currentRound == roundTime - 1) newFileName = _options._indexPrefix + "/" + "num" + ".idx"; // final index file
		else newFileName = _options._indexPrefix + "/" + "numTmp" + currentRound + ".idx";
		newFile = new File(newFileName);
		bw = new BufferedWriter(new FileWriter(newFile));
		
		dict = dicts.get(5);
		orderedTerms = new ArrayList<String>(dict.keySet());		
		Collections.sort(orderedTerms);
		
		mergeAndWriteIndexHelper(br, bw, orderedTerms, dict);

		br.close();
		bw.close();
		oldFile.delete();
		//System.out.println("Write to " + newFileName);
		
		// english terms
		for (int i = 0; i < 5; ++i) {
			char initial = (char) (i * 5 + 'a');
			
			oldFileName = _options._indexPrefix + "/" + initial + "Tmp" + (currentRound - 1) + ".idx";
			oldFile = new File(oldFileName);
			br = new BufferedReader(new FileReader(oldFile));
			
			//System.out.println("Read from " + oldFileName);
			
			if (currentRound == roundTime - 1) newFileName = _options._indexPrefix + "/" + initial + ".idx"; // final index file
			else newFileName = _options._indexPrefix + "/" + initial + "Tmp" + currentRound + ".idx";
			newFile = new File(newFileName);
			bw = new BufferedWriter(new FileWriter(newFile));
			
			dict = dicts.get(i);
			orderedTerms = new ArrayList<String>(dict.keySet());
			
			Collections.sort(orderedTerms);
			mergeAndWriteIndexHelper(br, bw, orderedTerms, dict);
			br.close();
			bw.close();
			oldFile.delete();
			//System.out.println("Write to " + newFileName);
		}
		
		refresh();
	}
	
	/**
	 * Helper method for mergeAndWriteIndex
	 * @param br
	 * @param bw
	 * @param orderedTerms
	 * @param dict
	 * @throws IOException
	 */
	public void mergeAndWriteIndexHelper(BufferedReader br, BufferedWriter bw, 
				List<String> orderedTerms, HashMap<String, Vector<Integer>> dict) throws IOException {
		String prevRecord;
		StringBuilder sb;
		int termIndex = 0;
		prevRecord = br.readLine();
		while ((prevRecord != null) && termIndex < orderedTerms.size()) {
			sb = new StringBuilder();
			String prevTerm = prevRecord.split(";")[0], newTerm = orderedTerms.get(termIndex);
			
			if (prevTerm.equals(newTerm)) { // merge the doc ids
				sb.append(prevRecord);
				for (int docid : dict.get(newTerm)) {
					sb.append(" " + docid);
				}
				sb.append("\n");
				bw.write(sb.toString());
				prevRecord = br.readLine();
				termIndex++;
			} else if (prevTerm.compareTo(newTerm) < 0) { // prevTerm is alphabetically smaller than newTerm, write prevRecord
				sb.append(prevRecord + "\n");
				bw.write(sb.toString());
				prevRecord = br.readLine();
			} else if (prevTerm.compareTo(newTerm) > 0) { // prevTerm is alphabetically larger than newTerm, write newTerm
				sb.append(newTerm + ";");
				for (int docid : dict.get(newTerm)) {
					sb.append(" " + docid);
				}
				sb.append("\n");
				bw.write(sb.toString());
				termIndex++;
			}
			
		}

		// write the remaining records in previous index to new files
		while (prevRecord != null) {
			sb = new StringBuilder();
			sb.append(prevRecord + "\n");
			bw.write(sb.toString());
			prevRecord = br.readLine();
		}
		// write the remaining current data to new files
		while (termIndex < orderedTerms.size()) {
			String term = orderedTerms.get(termIndex);
			sb = new StringBuilder();
			sb.append(term + ";");
			for (int docid : dict.get(term)) {
				sb.append(" " + docid);
			}
			sb.append("\n");
			bw.write(sb.toString());
			termIndex++;
		}
	}
	
	
	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		loadTermFrequency();
		loadDocuments();

	    System.out.println(_numDocs + " files loaded " +
	    		"with " + Long.toString(_totalTermFrequency) + " terms!");
	    
//	    System.out.println("zoo: " + corpusTermFrequency("zoo"));
//	    System.out.println("docids: " + corpusDocFrequencyByTerm("zoo"));
//	    System.out.println("next after 110: " + next("zoo", 110));
	}
	
	/**
	 * Load the term frequency information from file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadTermFrequency() throws IOException, ClassNotFoundException {
	    String termFreqFile = _options._indexPrefix + "/termFrequency.idx";
	    System.out.println("Load termFrequency from: " + termFreqFile);
	    
	    BufferedReader br = new BufferedReader(new FileReader(termFreqFile));
	    String record;
	    while ((record = br.readLine()) != null) {
	    	String[] results = record.split(";");
	    	termFrequency.put(results[0], Integer.parseInt(results[1]));
	    	_totalTermFrequency += Integer.parseInt(results[1]);
	    }
	}
	
	/**
	 * Load the documents from file
	 * @throws IOException
	 */
	public void loadDocuments() throws IOException {
	    String docFile = _options._indexPrefix + "/documents.idx";
	    System.out.println("Load documents from: " + docFile);
	    
	    BufferedReader br = new BufferedReader(new FileReader(docFile));
	    String record;
	    while ((record = br.readLine()) != null) {
	    	String[] results = record.split(";");
	    	DocumentIndexed doc = new DocumentIndexed(Integer.parseInt(results[0]), this);
	    	doc.setTitle(results[1]);
	    	doc.setUrl(results[2]);
	    	//System.out.println(documents.size() + " " + results[3]);
	    	doc.setPageRank(Float.parseFloat(results[3]));
	    	doc.setNumViews(Integer.parseInt(results[4]));
	    	documents.add(doc);
	    }
	    _numDocs = documents.size();
	}
	
	@Override
	public Document getDoc(int docid) {
		if (docid < 0 || docid >= documents.size()) return null;
		else return documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		Vector<String> words = query._tokens;
		Vector<Integer> indices = new Vector<Integer>();
		//System.out.println("docid is " + docid);
		for (String word : words) {
			word = stem(word);
			int id = next(word, docid);
			//System.out.println(word + " in doc " + id);
			if (id == -1) return null;
			else indices.add(id);
		}
		//System.out.println(indices.toString() + " " + allEquals(indices));
		if (allEquals(indices) == true) {
			//System.out.println("all in " + indices.get(0));
			return documents.get(indices.get(0));
		}
		else return nextDoc(query, maxID(indices) - 1);
	}

	/**
	 * Find the next document that term appears in
	 * @param term
	 * @param docid
	 * @return the id of the doc
	 */
	public int next(String term, int docid) {
		if (termFrequency.containsKey(term) == false) return -1;

		String initial, fileName;
		HashMap<String, Vector<Integer>> dict;
		int idx = (term.charAt(0) - 'a') / 5;
		
		switch (idx) {
			case 0: 
				initial = "a";
				dict = dicts.get(0);
				break;
			case 1: 
				initial = "f";
				dict = dicts.get(1);
				break;
			case 2:
				initial = "k";
				dict = dicts.get(2);
				break;
			case 3:
				initial = "p";
				dict = dicts.get(3);
				break;
			case 4:
			case 5:
				initial = "u";
				dict = dicts.get(4);
				break;
			default:
				initial = "num";
				dict = dicts.get(5);
				break;
		}
		try {
			fileName = _options._indexPrefix + "/" + initial + ".idx";

			if (dict.isEmpty()) { // load the map
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String record;
				Vector<Integer> list = new Vector<Integer>();
				while ((record = br.readLine()) != null) {
					String[] results = record.split(";");
					String[] ids = results[1].split(" ");
					for (int i = 1; i < ids.length; ++i) { // ids[0] is empty
						list.add(Integer.parseInt(ids[i]));
					}
					dict.put(results[0], list);
				}
				br.close();
				System.out.println("Loaded " + fileName);
			}
		
			Vector<Integer> ids = dict.get(term);
			int length = ids.size();
			if (docid >= ids.get(length - 1)) return -1;
			if (docid < ids.get(0)) return ids.get(0);
			return ids.get(binarySearch(term, 0, length - 1, docid, ids));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Find the document whose id is just bigger than the docid 
	 * @param term
	 * @param low
	 * @param high
	 * @param docid
	 * @return the position
	 */
	public int binarySearch(String term, int low, int high, int docid, Vector<Integer> list) {
		int mid = 0;
		while (high - low > 1) {
			mid = (low + high) / 2;
			if (docid >= list.get(mid)) low = mid;
			else high = mid;
		}
		return high;
	}
	
	/**
	 * Check if all the elements in the vector are equal
	 * @param ids
	 * @return true if all the elements are equal, otherwise return false
	 */
	public boolean allEquals(Vector<Integer> ids) {
		for (int i = 0; i < ids.size() - 1; ++i) {
			if (ids.get(i).intValue() != ids.get(i + 1).intValue()) {
				//System.out.println(ids.get(i) + " != " + ids.get(i + 1));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find the max element in the vector
	 * @param ids
	 * @return the max element
	 */
	public int maxID(Vector<Integer> ids) {
		int max = 0;
		for (int i = 0; i < ids.size(); ++i) {
			if (ids.get(i) > max) max = ids.get(i);
		}
		return max;
	}
	
	@Override
	public int corpusDocFrequencyByTerm(String term) {
		if (termFrequency.containsKey(term) == false) return 0;
		
		String initial, fileName;
		HashMap<String, Vector<Integer>> dict;
		int idx = (term.charAt(0) - 'a') / 5;
		
		switch (idx) {
			case 0: 
				initial = "a";
				dict = dicts.get(0);
				break;
			case 1: 
				initial = "f";
				dict = dicts.get(1);
				break;
			case 2:
				initial = "k";
				dict = dicts.get(2);
				break;
			case 3:
				initial = "p";
				dict = dicts.get(3);
				break;
			case 4:
			case 5:
				initial = "u";
				dict = dicts.get(4);
				break;
			default:
				initial = "num";
				dict = dicts.get(5);
				break;
		}
		try {
		
			fileName = _options._indexPrefix + "/" + initial + ".idx";
			
			if (dict.isEmpty()) { // load the map
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String record;
				Vector<Integer> list = new Vector<Integer>();
				while ((record = br.readLine()) != null) {
					String[] results = record.split(";");
					String[] ids = results[1].split(" ");
					for (int i = 1; i < ids.length; ++i) { // ids[0] is empty
						list.add(Integer.parseInt(ids[i]));
					}
					dict.put(results[0], list);
				}
				br.close();
				System.out.println("Loaded " + fileName);
			}
			
			return dict.get(term).size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int corpusTermFrequency(String term) {
		if (termFrequency.containsKey(term)) return termFrequency.get(term);
		else return 0;
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		SearchEngine.Check(false, "Not implemented!");
		return 0;
	}
}
