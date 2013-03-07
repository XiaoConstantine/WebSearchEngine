package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer implements Serializable {
	private static final long serialVersionUID = 1077112905740585098L;
	/*
	 * Invert-Index DS: LinkedList with Term & Doc_id which appeared.Since we
	 * have method: corpusDocFrequencyByTerm(Term) & corpusTermFrequency(Term).
	 * We should have a HashMap<String, Integer> to return like hw1?
	 */

	// Maps each term to the doc-ids that it appears in
	private Map<String, Vector<Integer>> dictionary = new HashMap<String, Vector<Integer>>();
	// Maps each term appears in corpus
	private Map<String, Integer> termFrequency = new HashMap<String, Integer>();
	// Stores all Document in memory.
	private Vector<Document> documents = new Vector<Document>();

	/**
	 * Constructor
	 * 
	 * @param options
	 */
	public IndexerInvertedDoconly(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
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
			  
			System.out.println("Indexing wiki files: " + files.length);
			
			for (int i = 0; i < files.length; ++i) {
				processDocument(files[_numDocs], _numDocs);
				++_numDocs;
			}
		}
		
		// remove the stop words
		for (String stopword: termFrequency.keySet()) {
			if ((float) termFrequency.get(stopword) / _totalTermFrequency > 0.06) {
				termFrequency.remove(stopword);
				dictionary.remove(stopword);
			}
		}
		
		
		System.out.println("Finish indexing " + _numDocs + " files");
		System.out.println("Terms: " + _totalTermFrequency);
		// write the idx to disk
		String indexFile = _options._indexPrefix + "/corpus.idx";
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
		writer.writeObject(this);
		writer.close();
		System.out.println("Finish writing to disk");
	}

	/**
	 * For parsing simple documents
	 * @param content
	 * @param docid
	 */
	public void parseDocument(String content, int docid) {
	    Scanner s = new Scanner(content).useDelimiter("\t");
		
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
			Vector<Integer> tmp;
			if (dictionary.containsKey(token)) {
				tmp = dictionary.get(token);
				if (tmp.contains(docid) == false){
					tmp.add(docid); // add the doc to the term's appearance list
					dictionary.put(token, tmp);
				}
			} else { // new term
				tmp = new Vector<Integer>();
				tmp.add(docid); // add the doc to the term's appearance list
				dictionary.put(token, tmp);
			}			
			
			if (termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token) + 1);
			} else {
				termFrequency.put(token, 1);
			}
			
			++_totalTermFrequency;
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

		DocumentIndexed doc = new DocumentIndexed(docid,this);
		doc.setTitle(file.getName());
		doc.setUrl(file.getAbsolutePath());

		// parse the title
		Scanner s = new Scanner(file.getName()).useDelimiter("_");
		while (s.hasNext()) {
			String token = stem(s.next());
			// check the stemmed token
			Vector<Integer> tmp;
			if (dictionary.containsKey(token)) {
				tmp = dictionary.get(token);
				if (tmp.contains(docid) == false){
					tmp.add(docid); // add the doc to the term's appearance list
					dictionary.put(token, tmp);
				}
			} else { // new term
				tmp = new Vector<Integer>();
				tmp.add(docid); // add the doc to the term's appearance list
				dictionary.put(token, tmp);
			}	
			
			if (termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token) + 1);
			} else {
				termFrequency.put(token, 1);
			}
			
			++_totalTermFrequency;
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
						} 
					}
				}
				
				if (scriptFlag != 0) continue;
				
				// parse the content, add them into dictionary
				processWikiDoc(line, docid);
			}
			documents.add(docid, doc);
		} finally {
			reader.close();
		}

	}

	/**
	 * process the wiki doc
	 */
	public void processWikiDoc(String content, int docid) {
		String pureText;
		pureText = content.replaceAll("<[^>]*>", " "); // remove <...>
		pureText = pureText.replaceAll("\\pP|\\pS|\\pC", " "); // replace sign with whitespace
		Scanner s = new Scanner(pureText).useDelimiter(" ");
		
		while (s.hasNext()) {
			String token = stem(s.next());
			// 
			if (token.matches("[0-9a-z]*") == false) continue; // not number or english
			// check the stemmed token
			Vector<Integer> tmp;
			if (dictionary.containsKey(token)) {
				tmp = dictionary.get(token);
				if (tmp.contains(docid) == false){
					tmp.add(docid); // add the doc to the term's appearance list
					dictionary.put(token, tmp);
				}
			} else { // new term
				tmp = new Vector<Integer>();
				tmp.add(docid); // add the doc to the term's appearance list
				dictionary.put(token, tmp);
			}	
			
			if (termFrequency.containsKey(token)) {
				termFrequency.put(token, termFrequency.get(token) + 1);
			} else {
				termFrequency.put(token, 1);
			}
			
			++_totalTermFrequency;
		}
		s.close();
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

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
	    String indexFile = _options._indexPrefix + "/corpus.idx";
	    System.out.println("Load index from: " + indexFile);

	    ObjectInputStream reader =
	        new ObjectInputStream(new FileInputStream(indexFile));
	    IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

	    this.documents = loaded.documents;
	    this._numDocs = documents.size();
	    this.dictionary = loaded.dictionary;
	    this.termFrequency = loaded.termFrequency;
	    
	    for (String term : loaded.dictionary.keySet()) {
	      this._totalTermFrequency += this.corpusTermFrequency(term);
	    }
	    
	    reader.close();

	    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
	        "with " + Long.toString(_totalTermFrequency) + " terms!");
	    
	    Iterator<String> iter = this.dictionary.keySet().iterator();
    	
	    while(iter.hasNext()){
	    	String term = iter.next();
            if(term.equals("bing")){
	    	System.out.println(term + " " + this.dictionary.get(term).size()+this.dictionary.get(term).toString());
            }
	    	
	    }
	}

	@Override
	public Document getDoc(int docid) {
		return null;
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		Vector<String> words = query._tokens;
		Vector<Integer> indices = new Vector<Integer>();
		
		for (String word : words) {
			int id = next(word, docid);
			if (id == -1) return null;
			else indices.add(id);
		}
		
		if (allEquals(indices) == true) return documents.get(docid);
		else return nextDoc(query, maxID(indices) - 1);
	}

	/**
	 * Find the next document that term appears in
	 * @param term
	 * @param docid
	 * @return the id of the doc
	 */
	public int next(String term, int docid) {
		if (dictionary.containsKey(term) == false) return -1;
		else {
			Vector<Integer> list = dictionary.get(term);
			int length = list.size();
			if (docid > list.get(length - 1)) return -1;
			if (docid < list.get(0)) return list.get(0);
			return list.get(binarySearch(term, 0, length - 1, docid));
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
	public int binarySearch(String term, int low, int high, int docid) {
		int mid = 0;
		while (high - low > 1) {
			mid = (low + high) / 2;
			if (docid >= dictionary.get(term).get(mid)) low = mid;
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
			if (ids.get(i) != ids.get(i + 1)) return false;
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
		for (int i = 0; i < ids.size() - 1; ++i) {
			if (ids.get(i) > max) max = ids.get(i);
		}
		return max;
	}
	
	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return dictionary.get(term).size();
	}

	@Override
	public int corpusTermFrequency(String term) {
		return termFrequency.get(term);
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		SearchEngine.Check(false, "Not implemented!");
		return 0;
	}
	
//	 public static void main(String args[]) throws IOException{
//		   String fileName = "/home/zz477/git/WebSearchEngine/HW2/data/3D_film";
////		   BufferedReader reader = new BufferedReader(new FileReader(filename));
////		    String noHtmlContent = null;
////		   try{
////		        String line = null;
////		        while((line = reader.readLine()) != null){
////		            noHtmlContent = line.replaceAll("<[^>]*>", " ");
////		            noHtmlContent = noHtmlContent.replaceAll("\\pP|\\pS|\\pC", " ");
////		            
////		             System.out.println(noHtmlContent);
////		        }
////		    }finally{
////		        reader.close();
////		    }
//		// read the file, set the doc
//			BufferedReader reader = new BufferedReader(new FileReader(fileName));
//			try {
//				int scriptFlag = 0; // 0 means no script, 1 means <script>, 2 means <script ...>
//				String line = null;
//				String pureText = null;
//				while ((line = reader.readLine()) != null) {
//					// remove the content in <script>
//					while (line.contains("<script") || line.contains("</script>")) {
//						if (scriptFlag == 2) { // in <script ...
//							if (line.contains(">")) {
//								line = line.substring(line.indexOf(">") + 1);
//								scriptFlag = 1;
//							}
//						} 
//						if (scriptFlag == 1) { // in <script> ... 
//							if (line.contains("</script>")) {
//								line = line.substring(line.indexOf("</script>") + 9);
//								scriptFlag = 0;
//							}
//						}
//						if (scriptFlag == 0) {
//							if (line.contains("<script>")) {
//								scriptFlag = 1;
//								System.out.println(line.substring(0, line.indexOf("<script>")));
//								line = line.substring(line.indexOf("<script>") + 8);
//							} else if (line.contains("<script")) {
//								scriptFlag = 2;
//								System.out.println(line.substring(0, line.indexOf("<script")));
//								line = line.substring(line.indexOf("<script") + 7);
//							} 
//						}
//					}
//					
//					if (scriptFlag != 0) continue;
//					
//					System.out.println(line);
//				}
//			} finally {
//				reader.close();
//			}
//		   
//	 }
}
