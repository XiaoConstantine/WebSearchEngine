package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import java.lang.ref.WeakReference;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends Indexer implements Serializable {
    
    private static final long serialVerisionUID = 1088111905740087931L;
    private HashMap<String, ArrayList<ArrayList<Integer>>> invertedIndex = new HashMap<String,ArrayList<ArrayList<Integer>>>();
    
    private List<HashMap<String, ArrayList<ArrayList<Integer>>>> invertedIndex_wiki = new ArrayList<HashMap<String, ArrayList<ArrayList<Integer>>>>();

    
    //All unique terms appeared in corpus. Offsets are integer representation.
    private Vector<String> _terms = new Vector<String>();
    
    //Term frequency, key is term and value is the number of times the term appears in the corpus.
    private HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
    
    //Store all Document in memory.
    private Vector<DocumentIndexed> _documents = new Vector<DocumentIndexed>();
    
    private long docTotalTermFrequency = 0;
    private int pos = 0;
    
    public IndexerInvertedOccurrence(Options options) {
        super(options);
        System.out.println("Using Indexer: " + this.getClass().getSimpleName());
        for(int i = 0; i < 6; i++)
            invertedIndex_wiki.add(new HashMap<String, ArrayList<ArrayList<Integer>>>());
    }
    
    @Override
    public void constructIndex() throws IOException {
        if(_options._corpusPrefix.equals("data/simple")){
            String corpusFile = _options._corpusPrefix + "/corpus.tsv";
            System.out.println("Construct index from: " + corpusFile);
            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
            try{
                String line = null;
                int docid = 0;
                while((line = reader.readLine()) != null){
                    processSimpleDocument(line,docid);
                    docid ++;
                }
                System.out.println("Indexed " + Integer.toString(_numDocs)
                                   + " docs with " + Long.toString(_totalTermFrequency)
                                   + " terms.");
                String indexFile = _options._indexPrefix + "/corpus.idx";
                System.out.println("Store index to: " + indexFile);
                ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
                writer.writeObject(this);
                writer.close();
            }finally{
                reader.close();
            }
        }else if(_options._corpusPrefix.equals("data/wiki")){
            File folder = new File(_options._corpusPrefix);
            File[] files = folder.listFiles();
            int docid = 0;
			int round = 0;
            
			if(files.length % 500 == 0) round = files.length/500;
			else round = files.length/500 + 1;
			for(int i = 0; i < round; i++){
				for(int j = 0; j < 500 && (i*500 + j < files.length); j++){
					processWikiDocument(files[docid], docid);
				    docid++;
                    ++_numDocs;
				}
				if(i == 0){
					writeIndex();
				}else{
					mergeAndWriteIndex(i, round);
				}
                System.out.println("round" + i + "\n");
			}
        }
        
        System.out.println("Finish indexing " + _numDocs + " files");
        System.out.println("Terms: " + _totalTermFrequency);
        System.out.println("Unique terms: " + termFrequency.keySet().size());
        
        //remove stopwords
        Vector<String> stopwords = new Vector<String>();
        for(String stopword : termFrequency.keySet()){
            if((float)termFrequency.get(stopword) / _totalTermFrequency > 0.06){
                stopwords.add(stopword);
            }
        }
        for(String stopword : stopwords){
            removeStopword(stopword);
        }
        
        System.out.println("Removed " + stopwords.size() + " stopwords");
        System.out.println("Terms without stopwords: " + _totalTermFrequency);
        
        /*String test = "google";
        ArrayList<ArrayList<Integer>> info = invertedIndex.get(test);
        ArrayList<Integer> docID = new ArrayList<Integer>();
        for(ArrayList<Integer> tmp : info){
            int did = tmp.get(0);
            docID.add(did);
        }
        System.out.println(Integer.toString(docID.size()));*/
        
        //write termFrequency and documents to disk
        BufferedWriter bw;
        String termFreqFile = _options._indexPrefix + "/termFrequency.idx";
        System.out.println("TermFrequency: writing to " + termFreqFile);
        bw = new BufferedWriter(new FileWriter(termFreqFile));
        writeTermFrequency(bw);
        bw.close();
        weakRefgc();
        
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
    public void writeTermFrequency(BufferedWriter bw) throws IOException{
        StringBuilder sb;
		// termFrequency format: term; frequency
		for (String term : termFrequency.keySet()) {
			bw.write(term + ";");
			bw.write(termFrequency.get(term) + ";");
			//bw.write(sb.toString());
		}
    }
    
    /**
      * Write the documents to file
      * @param bw
      * @throws IOException
      */
    public void writeDocuments(BufferedWriter bw) throws IOException{
       // StringBuilder sb;
        //docid,title,url,pageRank,numViews
        for(Document doc: _documents){
            //DocumentIndexed doc = (DocumentIndexed)documents.get(i);
            //sb = new StringBuilder();
            //sb.append(doc._docid + ";");
            //sb.append(doc.getTitle() + ";");
            //sb.append(doc.getUrl() + ";");
            //sb.append(doc.getPageRank() + ";");
            //sb.append(doc.getNumViews() + ";");
            //sb.append(doc.getDocTotalTermFrequency() + "\n");
            //bw.write(sb.toString());
            bw.write(doc._docid + ";");
            bw.write(doc.getTitle() + ";");
			bw.write(doc.getUrl() + ";");
			bw.write(doc.getPageRank() + ";");
			bw.write(doc.getNumViews() + ";");
		 	bw.write(((DocumentIndexed)doc).getDocTotalTermFrequency() + ";");
        }
    }
    
    public void writeIndex() throws IOException{
     	String fileName;
		List<String> orderedTerms;
		HashMap<String, ArrayList<ArrayList<Integer>>> dict;
		BufferedWriter bw;
		
		// num terms
		fileName = _options._indexPrefix + "/numTmp0.idx";
		bw = new BufferedWriter(new FileWriter(fileName));
		dict = invertedIndex_wiki.get(5);
		orderedTerms = new ArrayList<String>(dict.keySet());
		
		Collections.sort(orderedTerms);
		writeIndexHelper(bw, orderedTerms, dict);
		bw.close();
		
		// english terms
		for (int i = 0; i < 5; ++i) {
			char initial = (char) (i * 5 + 'a');
			fileName = _options._indexPrefix + "/" + initial + "Tmp0.idx";
			bw = new BufferedWriter(new FileWriter(fileName));
			dict = invertedIndex_wiki.get(i);
			orderedTerms = new ArrayList<String>(dict.keySet());
			
			Collections.sort(orderedTerms);
			writeIndexHelper(bw, orderedTerms, dict);
			bw.close();
		}
		
		refresh();
        weakRefgc();
    }
    
    public void writeIndexHelper(BufferedWriter bw, List<String> orderedTerms, 
                                 HashMap<String, ArrayList<ArrayList<Integer> > > dict) throws IOException {
		StringBuilder sb;
		for (String term : orderedTerms) { // write the terms alphabetically
			//sb = new StringBuilder();
			// separate term and its doc ids by semicolon
			bw.write(term + ";");
			// separate the doc ids by white space
			for (ArrayList<Integer> infoindex : dict.get(term)) {
                for(Integer res: infoindex){
                    bw.write(" " + res);
                    //bw.write(res);	
                }
                bw.write(";");
			}
			bw.write("\n");
			//bw.write(sb.toString());
		}
	}
    
    public void mergeAndWriteIndex(int currentRound, int roundTime) throws IOException{
    	String oldFileName, newFileName;
		File oldFile, newFile;
		BufferedReader br;
		BufferedWriter bw;
		HashMap<String, ArrayList<ArrayList<Integer>>> dict;
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
		
		dict = invertedIndex_wiki.get(5);
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
			
			dict = invertedIndex_wiki.get(i);
			orderedTerms = new ArrayList<String>(dict.keySet());
			
			Collections.sort(orderedTerms);
			mergeAndWriteIndexHelper(br, bw, orderedTerms, dict);
			br.close();
			bw.close();
			oldFile.delete();
			//System.out.println("Write to " + newFileName);
		}
		
		refresh();
        weakRefgc();
    }
    
    
	public void mergeAndWriteIndexHelper(BufferedReader br, BufferedWriter bw, 
                                         List<String> orderedTerms, HashMap<String, ArrayList<ArrayList<Integer>>> dict) throws IOException {
		String prevRecord;
		StringBuilder sb;
		int termIndex = 0;
		prevRecord = br.readLine();
		while ((prevRecord != null) && termIndex < orderedTerms.size()) {
			//sb = new StringBuilder();
			String prevTerm = prevRecord.split(";")[0], newTerm = orderedTerms.get(termIndex);
			
			if (prevTerm.equals(newTerm)) { // merge the doc ids
				bw.write(prevRecord);
                
                for (ArrayList<Integer> indexinfo: dict.get(newTerm)) {
                    for(Integer b: indexinfo){
                        //sb.append(" " + b);
                        bw.write(" " + b);
                        //bw.write(b);		
                    }
                    bw.write(";");
                }
		        bw.write("\n");
				//sb.append("\n");
				//bw.write(sb.toString());
				prevRecord = br.readLine();
				termIndex++;
			} else if (prevTerm.compareTo(newTerm) < 0) { // prevTerm is alphabetically smaller than newTerm, write prevRecord
				//sb.append(prevRecord + "\n");
				//bw.write(sb.toString());
                
				bw.write(prevRecord + "\n");
				prevRecord = br.readLine();
			} else if (prevTerm.compareTo(newTerm) > 0) { // prevTerm is alphabetically larger than newTerm, write newTerm
				//sb.append(newTerm + ";");
				bw.write(newTerm + ";");
				for (ArrayList<Integer> infoindex: dict.get(newTerm)) {
                    for(Integer b: infoindex){
						//sb.append(" " + b);
                        bw.write(" "+ b);
                       // bw.write(b);
                    }
                    bw.write(";");
				}
				bw.write("\n");
				//sb.append("\n");
				//bw.write(sb.toString());
				termIndex++;
			}
			
		}
        
		// write the remaining records in previous index to new files
		while (prevRecord != null) {
			//sb = new StringBuilder();
			//sb.append(prevRecord + "\n");
			//bw.write(sb.toString());
		    bw.write(prevRecord + "\n");
			prevRecord = br.readLine();
		}
		// write the remaining current data to new files
		while (termIndex < orderedTerms.size()) {
			String term = orderedTerms.get(termIndex);
			//sb = new StringBuilder();
			//sb.append(term + ";");
			bw.write(term + ";");
			for (ArrayList<Integer> infoindex: dict.get(term)) {
                for(Integer b: infoindex){
                    //sb.append(" " + b);
                    bw.write(" "+ b);
                   // bw.write(b);
                }
                bw.write(";");
			}
			//sb.append("\n");
			//bw.write(sb.toString());
			bw.write("\n");
			termIndex++;
		}
    }
    
    public void refresh(){
        for(HashMap<String, ArrayList<ArrayList<Integer>>> dict: invertedIndex_wiki ){
            for(String term: dict.keySet()){
                for(ArrayList<Integer> list: dict.get(term)){
                    list.clear();
                }
            }
            dict.clear();
        }
    }

    
    /**
     * For parsing simple ducument
     * 
     */
    private void processSimpleDocument(String line, int docid){
        Scanner s = new Scanner(line).useDelimiter("\t");
        
        String title = s.next();
        phraseLine(docid, title, " ");
        
        String body = s.next();
        phraseLine(docid, body, " ");
        
        int numViews = Integer.parseInt(s.next());
        s.close();
        DocumentIndexed doc = new DocumentIndexed(docid, this);
        doc.setUrl(Integer.toString(docid));
        doc.setTitle(title);
        doc.setNumViews(numViews);
        _documents.add(doc);
        ++_numDocs;
    }
    
    private void phraseLine(int docid,  String content, String delimiter){
        Scanner s = new Scanner(content).useDelimiter(delimiter);
        int pos = 0;
        while(s.hasNext()){
            ++_totalTermFrequency;
            String token = stem(s.next());
            if(!_terms.contains(token)){
                _terms.add(token);
                ArrayList<ArrayList<Integer>> posList = new ArrayList<ArrayList<Integer>>();
                ArrayList<Integer> indexInfo = new ArrayList<Integer>();
                indexInfo.add(docid);
                indexInfo.add(pos);
                posList.add(indexInfo);
                invertedIndex.put(token,posList);
            }else{
                ArrayList<ArrayList<Integer>> posList = invertedIndex.get(token);
                if(docid != posList.get(posList.size() - 1).get(0)){
                    ArrayList<Integer> indexInfo = new ArrayList<Integer>();
                    indexInfo.add(docid);
                    posList.add(indexInfo);
                }
                ArrayList<Integer> indexInfo = posList.get(posList.size() - 1);
                indexInfo.add(pos);
            }
            pos++;
            if(termFrequency.containsKey(token) == false){
                termFrequency.put(token,1);
            }else{
                termFrequency.put(token,termFrequency.get(token) + 1);
            }
        }
    }
    
    /**
     * For parsing wiki ducuments
     * 
     */
    private void processWikiDocument(File file,int docid) throws IOException{
        docTotalTermFrequency = 0;
        pos = 0;
        DocumentIndexed doc = new DocumentIndexed(docid,this);
        doc.setUrl(file.getAbsolutePath());
        doc.setTitle(file.getName());
        
        //phrase title
        String title = file.getName();
        title = title.replaceAll("\\pP|\\pS|\\pC", " ");
        Scanner s = new Scanner(title).useDelimiter(" ");
        while(s.hasNext()){
            String token = stem(s.next());
            if (token.matches("[0-9a-z]*") == false || token.isEmpty()) continue;
	        processWord(token, docid); 
        }
        s.close();
        
        //phrase file content
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
                            processWikiDoc(line.substring(0, line.indexOf("<script")),docid);
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
				processWikiDoc(line,docid);
			}
            _documents.add(docid,doc);
		} finally {
			reader.close();
		}
    }
    
    private void processWikiDoc(String content,int docid){
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
    
    private void processWord(String token, int docid){
        HashMap<String,ArrayList<ArrayList<Integer>>> dict = null;
        int dictIdx = (token.charAt(0) - 'a') / 5;
		if (dictIdx >= 0 && dictIdx < 5) 
            dict = invertedIndex_wiki.get(dictIdx);
		else if (dictIdx == 5) 
            dict = invertedIndex_wiki.get(4);
		else dict = invertedIndex_wiki.get(5);
        
        ArrayList<Integer> indexInfo;
        if(dict.containsKey(token)){
            ArrayList<ArrayList<Integer>> posList = dict.get(token);
            if(docid != posList.get(posList.size() - 1).get(0)){
                indexInfo = new ArrayList<Integer>();
                indexInfo.add(docid);
                posList.add(indexInfo);
            }
            indexInfo = posList.get(posList.size() - 1);
            indexInfo.add(pos);
        }else{
            ArrayList<ArrayList<Integer>> posList = new ArrayList<ArrayList<Integer>>();
            indexInfo = new ArrayList<Integer>();
            indexInfo.add(docid);
            indexInfo.add(pos);
            posList.add(indexInfo);
            dict.put(token,posList);
        }
        pos++;
        if(termFrequency.containsKey(token) == false){
            termFrequency.put(token,1);
        }else{
            termFrequency.put(token,termFrequency.get(token) + 1);
        }
        ++docTotalTermFrequency;
        ++_totalTermFrequency;
    }
    
    public String stem(String token) {
		token = token.toLowerCase();
		Stemmer stemmer = new Stemmer();
		stemmer.add(token.toCharArray(), token.toCharArray().length);
		stemmer.stem();
		return stemmer.toString();
	}
    
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

    
    @Override
    public void loadIndex() throws IOException, ClassNotFoundException {
        
        System.out.println("**********Begin Load Index******************");
        loadTermFrequency();
        loadDocuments();
                        
        System.out.println(Integer.toString(_numDocs) + " documents loaded " +
                           "with " + Long.toString(_totalTermFrequency) + " terms!");
    }
    
    private void loadTermFrequency() throws IOException, ClassNotFoundException {
	    String termFreqFile = _options._indexPrefix + "/termFrequency.idx";
	    System.out.println("Load termFrequency from: " + termFreqFile);
	    
	    BufferedReader br = new BufferedReader(new FileReader(termFreqFile));
	    String record;
	    while ((record = br.readLine()) != null) {
            //System.out.println(record);
	    	String[] results = record.split(";");
	    	termFrequency.put(results[0], Integer.parseInt(results[1]));
	    	_totalTermFrequency += Integer.parseInt(results[1]);
	    }
    }
	private void loadDocuments() throws IOException {
	    String docFile = _options._indexPrefix + "/documents.idx";
	    System.out.println("Load documents from: " + docFile);
	    
	    BufferedReader br = new BufferedReader(new FileReader(docFile));
	    String record;
	    while ((record = br.readLine()) != null) {
            //System.out.println(record);
	    	String[] results = record.split(";");
	    	DocumentIndexed doc = new DocumentIndexed(Integer.parseInt(results[0]), this);
	    	doc.setTitle(results[1]);
	    	doc.setUrl(results[2]);
	    	//System.out.println(documents.size() + " " + results[3]);
	    	doc.setPageRank(Float.parseFloat(results[3]));
	    	doc.setNumViews(Integer.parseInt(results[4]));
	    	doc.setDocTotalTermFrequency(Long.parseLong(results[5]));
	    	_documents.add(doc);
	    }
	    _numDocs = _documents.size();
	}

    
    @Override
    public Document getDoc(int docid) {
        SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
        return null;
    }
    
    /**
     * In HW2, you should be using {@link DocumentIndexed}.
     */
    @Override
    public DocumentIndexed nextDoc(Query query, int docid) {
        Vector<String> query_list = query._tokens;
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for(String term : query_list){
            term = stem(term);
            int id = next(term,docid);
            if(id == -1)return null;
            else indices.add(id);
        }
        System.out.println("finish for loop");
        if(allEquals(indices) == true){
            return _documents.get(indices.get(0));
        }else return nextDoc(query, maxID(indices) -1);
    }
    
    private int next(String term, int docid){
        if(!termFrequency.containsKey(term)) return -1;
        String initial, fileName;
        HashMap<String, ArrayList<ArrayList<Integer>>> dict;
        int idx = (term.charAt(0) - 'a')/5;
        
        switch(idx){
            case 0:
                initial = "a";
                dict = invertedIndex_wiki.get(0);
                break;
            case 1:
                initial = "f";
                dict = invertedIndex_wiki.get(1);
                break;
            case 2:
                initial = "k";
                dict = invertedIndex_wiki.get(2);
                break;
            case 3:
                initial = "p";
                dict = invertedIndex_wiki.get(3);
                break;
            case 4:
            case 5:
                initial = "u";
                dict = invertedIndex_wiki.get(4);
                break;
            default: 
                initial = "num";
                dict = invertedIndex_wiki.get(5);
                break;
        }
        try{
            fileName = _options._indexPrefix + "/" + initial + ".idx";
            //fileName = _options._indexPrefix + "/corpus.idx";
            //dict = invertedIndex;
            System.out.println("FIND IN NEXT()");
            if(dict.isEmpty()){
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                //System.out.println(fileName);
                String record;
                ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
                ArrayList<Integer> pos = new ArrayList<Integer>();
                while((record = br.readLine()) != null){
                    String []results = record.split(";");
                    //results[0] is term
                    //results[1] is posList of this term, split with white space " "
                    //System.out.println("test results");
                    //System.out.println(results[0]);
                    //System.out.println(results[1]);
                    //System.out.println("test finish");
                    String []indexInfo = results[1].split(" ");
                    //System.out.println(indexInfo.length);
                    //System.out.println(indexInfo[0]);
                    //System.out.println(indexInfo[1]);

                    for(int i = 1; i < indexInfo.length; i++){
                       // System.out.println("+++++++++");
                        pos.add(Integer.parseInt(indexInfo[i]));
                        if(term.equals("zzzz")){
                            System.out.println(indexInfo[i]);
                        }
                        
                        list.add(pos);
                    }
                    dict.put(results[0],list);
                }
                br.close();
               // weakRefgc();
            }
            //weakRefgc();
            System.out.println("term:"+term);
            System.out.println(dict.get(term).get(0).get(0));
            System.out.println(dict.get(term).get(0).get(1));
            ArrayList<ArrayList<Integer>> idList = dict.get(term);
            ArrayList<Integer> ids = new ArrayList<Integer>();
            for(ArrayList<Integer> i : idList){
                //System.out.println(i.get(0));
                ids.add(i.get(1));
            }
            System.out.println("ids.size(): " + ids.size());
            //System.out.println(ids.get(1111));
            //System.out.println(ids.get(ids.size()-1));
            int length = ids.size();
            System.out.println("docid: "+docid);
            System.out.println("first in ids: "+ ids.get(0));
            System.out.println("lase in ids: "+ ids.get(length-1));
            if(docid >= ids.get(length-1)) return -1;
            if(docid < ids.get(0)) return ids.get(0);
            return ids.get(binarySearch(term,1,length-1,docid,ids));
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }
    
    private int binarySearch(String term, int low, int high, int docid, ArrayList<Integer> list){
        System.out.println("binararysearch");
        while(high - low > 1){
            int mid = (high - low ) /2;
            if(list.get(mid) <= docid) low = mid;
            else high = mid;
        }
        return high;
    }
    
    private int maxID(ArrayList<Integer> ids){
        int max = 0;
        for(int i = 0; i < ids.size(); i++){
            if(ids.get(i) > max) max = ids.get(i);
        }
        return max;
    }
    
    private boolean allEquals(ArrayList<Integer> ids){
        System.out.println(ids.get(0));
        for(int i = 0; i < ids.size() - 1; i++){
            if(ids.get(i).intValue() != ids.get(i + 1).intValue()){
                return false;
            }
        }
        return true;
    }
    
    public void weakRefgc(){
        Object obj =new Object();
        WeakReference ref =  new WeakReference<Object>(obj);
        obj = null;
        while(ref.get() != null){
            System.gc();
        }
    }
    
    @Override
    public int corpusDocFrequencyByTerm(String term) {
        int result = 0;
        String initial;
        int idx = (term.charAt(0) - 'a')/5;
        switch(idx){
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
                break;
            case 4:
            case 5:
                initial = "u";
                break;
            default: 
                initial = "num";
                break;
        }
        String indexFileName = _options._indexPrefix + "/" + initial + ".idx";
        try{
            BufferedReader br = new BufferedReader(new FileReader(indexFileName));
            String line;
            while((line = br.readLine()) != null){
                String []results = line.split(";");
                String []indexInfo = results[1].split(" ");
                if(term.equals(results[0])){
                    result = indexInfo.length-1; 
                }
            }
            br = new BufferedReader(new FileReader(_options._indexPrefix + "/termFrequency.idx"));
            String l;
            String freq = null;
            while((l = br.readLine()) != null){
                Scanner s = new Scanner(l).useDelimiter(";");
                while(s.hasNext()){
                    if(term.equals(s.next())){
                        freq = s.next();
                    }
                    break;
                }
                break;
            }
            result = result - Integer.parseInt(freq);
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
                
       
        return result;
    }
    
    @Override
    public int corpusTermFrequency(String term) {
        if(termFrequency.containsKey(term)) 
            return termFrequency.get(term);
        return 0;
    }
    
    @Override
    public int documentTermFrequency(String term, String url) {
        //SearchEngine.Check(false, "Not implemented!");
        
        return 0;
    }
    
    public static void main(String args[]) throws IOException{
        Options option = new Options("conf/engine.conf");
        IndexerInvertedOccurrence index = new IndexerInvertedOccurrence(option);
        index.constructIndex();
        try{
            index.loadIndex();
        }catch(ClassNotFoundException e){
            System.out.println(e.getMessage());
        }
        
        Query query = new Query("zzzz");
        query.processQuery();
        DocumentIndexed doc = index.nextDoc(query,1);
        if(doc != null){
            System.out.println(doc._docid);
        }else{
            System.out.println("no such document with this query");
        }
    }
}
