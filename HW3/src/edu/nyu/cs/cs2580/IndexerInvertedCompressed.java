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
public class IndexerInvertedCompressed extends Indexer implements Serializable{

  private static final long serialVersionUID = 1077222905740085030L;
  private HashMap<String, ArrayList<ArrayList<Byte>>> invertedList = new HashMap<String, ArrayList<ArrayList<Byte>>>();

  private List<HashMap<String, ArrayList<ArrayList<Byte>>>> invertList_wiki = new ArrayList<HashMap<String, ArrayList<ArrayList<Byte>>>>();

  private HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();

//  private HashMap<String, Integer> docTermFrequency =  new HashMap<String, Integer>();

  private int pos = 0;
  private long docTotalTermFrequency = 0;
  private Vector<String> _terms = new Vector<String>();
  private Vector<Document> _documents = new Vector<Document>();
  private ByteAlignCompress compresser = new ByteAlignCompress();
  private Stemmer s = new Stemmer();

 
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
    for(int i = 0; i < 6; i++)
		invertList_wiki.add(new HashMap<String, ArrayList<ArrayList<Byte>>>());
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
            }finally{
                reader.close();
            }
        }
      else if(_options._corpusPrefix.equals("data/wiki")){
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
        weakRefgc();		
		String docFile = _options._indexPrefix + "/documents.idx";
		System.out.println("Documents: writing to " + docFile);
		bw = new BufferedWriter(new FileWriter(docFile));
		writeDocuments(bw);
		bw.close();
		
     
/*	String qury = "google";
	ArrayList<ArrayList<Byte>> infoindex = invertedList.get(qury);
	ArrayList<Integer> docID = new ArrayList<Integer>();
	for(ArrayList<Byte> tmp : infoindex){
		int did;
		did = compresser.decompressID(tmp);
		docID.add(did);
	}

	System.out.println(Integer.toString(docID.size()));
*/
	/*
	String indexFile = _options._indexPrefix + "/corpus.idx";
    System.out.println("Store index to: " + indexFile);
    ObjectOutputStream writer =
        new ObjectOutputStream(new FileOutputStream(indexFile));
    writer.writeObject(this);
    writer.close();
    */
  }

  private void processSimpleDocument(String line, int docid){
     Scanner s = new Scanner(line).useDelimiter("\t");
	 String title = s.next();
	 
	 phraseLine(docid, title, " ");

	 String body = s.next();
	 phraseLine(docid, body, " ");

	 int numViews = Integer.parseInt(s.next());
	 s.close();
	 DocumentIndexed doc = new DocumentIndexed(docid, this);
	 doc.setTitle(title);
	 doc.setNumViews(numViews);
	 _documents.add(doc);
	 ++_numDocs;

  }
 
  private void phraseLine(int docid, String content, String delimiter){
       Scanner s = new Scanner(content).useDelimiter(delimiter);
	   int pos = 1;
	   while(s.hasNext()){
	       ++_totalTermFrequency;
		   String token = s.next();
		   if(!_terms.contains(token)){
			   _terms.add(token);
		   ArrayList<ArrayList<Byte>> posList = new ArrayList<ArrayList<Byte>>();           
           ArrayList<Byte> indexinfo = new ArrayList<Byte>();
		   indexinfo.addAll(compresser.compressSingle(docid));
		   indexinfo.addAll(compresser.compressSingle(pos));
		   posList.add(indexinfo);
		   invertedList.put(token,posList);
		   }else{
			   ArrayList<ArrayList<Byte>> posList = invertedList.get(token);
			   if(docid != compresser.decompressID(posList.get(posList.size()- 1))){
				   ArrayList<Byte> indexinfo = new ArrayList<Byte>();
				   indexinfo.addAll(compresser.compressSingle(docid));
				   posList.add(indexinfo);
			   }
			   ArrayList<Byte> indexinfo = posList.get(posList.size() - 1);
			   indexinfo.addAll(compresser.compressSingle(pos));
		   }
		   pos++;
	       
	   }
  }


  public void processWikiDocument(File file, int docid) throws IOException{
       DocumentIndexed doc = new DocumentIndexed(docid, this);
	   pos = 0;
       docTotalTermFrequency = 0; 
	   doc.setTitle(file.getName());
	   //doc.setUrl(file.getAbsolutePath());
	   String title = file.getName();
	   title = title.replaceAll("\\pP|\\pS|\\pC", " ");
		//System.out.println(title);
	   Scanner s = new Scanner(title).useDelimiter(" ");
	   while (s.hasNext()) {
			String token = stem(s.next());
			if (token.matches("[0-9a-z]*") == false || token.isEmpty()) continue;
	        processWord(token, docid); 
	   }

	   s.close();
 
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
			 //doc.getDocTermFrequency.put(); 
		     //doc.setDocTermFrequency(docTermFrequency);
			_documents.add(docid, doc);
	     
		} finally {
			reader.close();
		} 
  }


  public void processWikiDoc(String content, int docid){
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

  public void processWord(String token, int docid){
	  HashMap<String,ArrayList<ArrayList<Byte>>> dict = null;
      int dictIdx = (token.charAt(0) - 'a') / 5;
		if (dictIdx >= 0 && dictIdx < 5) dict = invertList_wiki.get(dictIdx);
		else if (dictIdx == 5) dict = invertList_wiki.get(4);
		else dict = invertList_wiki.get(5);

		ArrayList<Byte> indexinfo;
		if(dict.containsKey(token)){
		   ArrayList<ArrayList<Byte>> posList = dict.get(token);
		   if(docid != compresser.decompressID(posList.get(posList.size()- 1))){
			  indexinfo = new ArrayList<Byte>();
			  indexinfo.addAll(compresser.compressSingle(docid));
			  posList.add(indexinfo);
		}
			   indexinfo = posList.get(posList.size() - 1);
			   indexinfo.addAll(compresser.compressSingle(pos));
		}else{
           ArrayList<ArrayList<Byte>> posList = new ArrayList<ArrayList<Byte>>();           
           indexinfo = new ArrayList<Byte>();
		   indexinfo.addAll(compresser.compressSingle(docid));
		   indexinfo.addAll(compresser.compressSingle(pos));
		   posList.add(indexinfo);
		   dict.put(token,posList);
		}
		pos++;
		if(termFrequency.containsKey(token) == false){
			termFrequency.put(token,1);
		}else{
			termFrequency.put(token, termFrequency.get(token) + 1);
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

  public void writeIndex() throws IOException{
     	String fileName;
		List<String> orderedTerms;
		HashMap<String, ArrayList<ArrayList<Byte>>> dict;
		BufferedWriter bw;
		
		// num terms
		fileName = _options._indexPrefix + "/numTmp0.idx";
		bw = new BufferedWriter(new FileWriter(fileName));
		dict = invertList_wiki.get(5);
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
			dict = invertList_wiki.get(i);
			orderedTerms = new ArrayList<String>(dict.keySet());
			
			Collections.sort(orderedTerms);
			writeIndexHelper(bw, orderedTerms, dict);
			bw.close();
			//System.out.println("Write to " + fileName);
		}
		
		refresh();
		weakRefgc();
  }

   public void writeIndexHelper(BufferedWriter bw, List<String> orderedTerms, 
				HashMap<String, ArrayList<ArrayList<Byte> > > dict) throws IOException {
		for (String term : orderedTerms) { // write the terms alphabetically
			//StringBuilder sb = new StringBuilder();
			// separate term and its doc ids by semicolon
			//sb.append(term+";");
			bw.write(term + ";");
			// separate the doc ids by white space
			for (ArrayList<Byte> infoindex : dict.get(term)) {
					for(Byte res: infoindex){
				          //sb.append(" " + res);
					       bw.write(" " + res);
					}
			}
			//sb.append("\n");
			bw.write("\n");
			//bw.write(sb.toString());
		}
	}

  public void mergeAndWriteIndex(int currentRound, int roundTime) throws IOException{
    	String oldFileName, newFileName;
		File oldFile, newFile;
		BufferedReader br;
		BufferedWriter bw;
		HashMap<String, ArrayList<ArrayList<Byte>>> dict;
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
		
		dict = invertList_wiki.get(5);
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
			
			dict = invertList_wiki.get(i);
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
				List<String> orderedTerms, HashMap<String, ArrayList<ArrayList<Byte>>> dict) throws IOException {
		String prevRecord;
		//StringBuilder sb;
		int termIndex = 0;
		prevRecord = br.readLine();
		while ((prevRecord != null) && termIndex < orderedTerms.size()) {
			//sb = new StringBuilder();
			String prevTerm = prevRecord.split(";")[0], newTerm = orderedTerms.get(termIndex);
			
			if (prevTerm.equals(newTerm)) { // merge the doc ids
			//	sb.append(prevRecord);
                bw.write(prevRecord);
		      for (ArrayList<Byte> indexinfo: dict.get(newTerm)) {
                for(Byte b: indexinfo){
					    //sb.append(" " + b);
						bw.write(" " + b);
				 }
	        	}
				//sb.append("\n");
				//bw.write(sb.toString());
			    bw.write("\n");	
			  prevRecord = br.readLine();
				termIndex++;
			} else if (prevTerm.compareTo(newTerm) < 0) { // prevTerm is alphabetically smaller than newTerm, write prevRecord
				//sb.append(prevRecord + "\n");
				bw.write(prevRecord + "\n");
				//bw.write(sb.toString());
				prevRecord = br.readLine();
			} else if (prevTerm.compareTo(newTerm) > 0) { // prevTerm is alphabetically larger than newTerm, write newTerm
				//sb.append(newTerm + ";");
				bw.write(newTerm + ";");
				for (ArrayList<Byte> infoindex: dict.get(newTerm)) {
					  for(Byte b: infoindex){
						//sb.append(" " + b);
						bw.write(" " + b);
				  }

				}
				//sb.append("\n");
				//bw.write(sb.toString());
				bw.write("\n");
				termIndex++;
			}
			
		}

		// write the remaining records in previous index to new files
		while (prevRecord != null) {
			//sb = new StringBuilder();
			bw.write(prevRecord + "\n");
		   // bw.write(sb.toString());
			prevRecord = br.readLine();
		}
		// write the remaining current data to new files
		while (termIndex < orderedTerms.size()) {
			String term = orderedTerms.get(termIndex);
			//sb = new StringBuilder();
			//sb.append(term + ";");
			bw.write(term + ";");
			for (ArrayList<Byte> infoindex: dict.get(term)) {
					  for(Byte b: infoindex){
						//sb.append(" " + b);
			             bw.write(" " + b);
					  }

			}
		//	sb.append("\n");
	    //		bw.write(sb.toString());
		    bw.write("\n");
			termIndex++;
		}
  }

  public void refresh(){
	for(HashMap<String, ArrayList<ArrayList<Byte>>> dict: invertList_wiki ){
        for(String term: dict.keySet()){
			for(ArrayList<Byte> list: dict.get(term)){
				list.clear();
			}
		}
		dict.clear();
	}
  }

  	public void writeTermFrequency(BufferedWriter bw) throws IOException {
		StringBuilder sb;
		// termFrequency format: term; frequency
		for (String term : termFrequency.keySet()) {
			bw.write(term + ";");
			bw.write(termFrequency.get(term) + "\n");;
			//bw.write(sb.toString());
		}
	} 


  public void writeDocuments(BufferedWriter bw) throws IOException{
	  for(Document doc: _documents){
			bw.write(doc._docid + ";");
			bw.write(doc.getTitle() + ";");
			bw.write(doc.getUrl() + ";");
			bw.write(doc.getPageRank() + ";");
			bw.write(doc.getNumViews() + ";");
		 	bw.write(((DocumentIndexed)doc).getDocTotalTermFrequency() + ";");
	  }

  }

  public void loadIndex() throws IOException, ClassNotFoundException {
    
        /*if (_options._corpusPrefix.contains("simple")) {
		    String indexFile = _options._indexPrefix + "/corpus.idx";
		    System.out.println("Load index from: " + indexFile);

		    ObjectInputStream reader =
		        new ObjectInputStream(new FileInputStream(indexFile));
		    IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

		    this.documents = loaded.documents;
		    // Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		    this._numDocs = documents.size();
		    for (Integer freq : loaded.termFrequency.values()) {
		      this._totalTermFrequency += freq;
		    }
		    for(int i = 0; i < this.dicts.size(); ++i) {
		    	this.dicts.clear();
		    	this.dicts.add(i,loaded.dicts.get(i));
		    }
		    
		    reader.close();

		    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
		        "with " + Long.toString(_totalTermFrequency) + " terms!");
		} */
	  //else {
			loadTermFrequency();
			loadDocuments();

			System.out.println(_numDocs + " files loaded " +
	    		"with " + Long.toString(_totalTermFrequency) + " terms!");
	//	}
  }
    
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
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
  public Document nextDoc(Query query, int docid) {
	  Vector<String> query_list = query._tokens;
      ArrayList<Integer> indices = new ArrayList<Integer>();
	  for(String term: query_list){
		  String[] term_array = term.split(" ");
		  if(term_array.length > 1){
              int id = nextPhrase(term, docid);
			  if(id == -1) return null;
			  else indices.add(id);
		  }else{
			  term = stem(term);
			  int id = next(term, docid);
              if(id == -1) return null;
			  else indices.add(id);
		  }
	  }

	  if(allEquals(indices) == true){
		  return _documents.get(indices.get(0));
	  }else return nextDoc(query, maxID(indices) - 1);
  }
  
  public int nextPhrase(Query query, int docid){
         

  }
  
  
  
  
  
  /*public int nextPhrase(Query query, int docid, int pos){
	  Document idVerify = nextDoc(query, docid - 1);
	  if(docid != idVerify._docid){
		  return -1;
	  }
	  ArrayList<Integer> posl = new ArrayList<Integer>();
	  int id = 0;
	  for(int i = 0; i < query._tokens.size(); i++){
		  id = next_pos(query._tokens.get(i), docid, pos);
		  if(id == -1)
			  return -1;
		  posl.add(id);
	  }
	  if(isPhrase(posl))
		  return posl.get(0);
	  else
		  return nextPhrase(query, docid, posl.get(posl.size()-1));
  }*/

  public boolean isPhrase(ArrayList<Integer> list){
	  if(list == null)
		  return false;
	  for(int i = 1; i < list.size(); i++){
		  if(list.get(i) != list.get(i-1) +1)
			  return false;
	  }
	  return true;
  }

 /* public int next_pos(String term, int docid, int pos){
      ArrayList<ArrayList<Byte>> list = dict.get(term);
      int next = -1;
	  for(ArrayList<Byte> index: list){
		  if(docid == compresser.decompressID(index)){
             ArrayList<Integer> res = compresser.decompressList(index);
			 for(int i = 1; i < res.size(); i++){
				 if(pos == res.get(i)){
			        next = res.get(i+1);
				 }
			 }
		  }
	  }
	  return next;
  }*/

  public int next(String term, int docid){
    if (termFrequency.containsKey(term) == false) return -1;
	String initial, fileName;
	HashMap<String, ArrayList<ArrayList<Byte>>> dict;
	int idx = (term.charAt(0) - 'a') / 5;
		
	switch (idx) {
		case 0: 
			initial = "a";
			dict = invertList_wiki.get(0);
			break;
		case 1: 
			initial = "f";
			dict = invertList_wiki.get(1);
			break;
		case 2:
			initial = "k";
			dict = invertList_wiki.get(2);
			break;
		case 3:
			initial = "p";
			dict = invertList_wiki.get(3);
			break;
		case 4:
		case 5:
			initial = "u";
			dict = invertList_wiki.get(4);
			break;
		default:
			initial = "num";
			dict = invertList_wiki.get(5);
			break;
		}
		try {
			fileName = _options._indexPrefix + "/" + initial + ".idx";
			if (dict.isEmpty()) { // load the map
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String record;
				ArrayList<ArrayList<Byte>> list = new ArrayList<ArrayList<Byte>>();
		    	while ((record = br.readLine()) != null) {
					String[] results = record.split(";");
					for(int j = 1; j < results.length; j++){
					String[] indexinfo = results[j].split(" ");
				    ArrayList<Byte> pos = new ArrayList<Byte>();
					for (int i = 1; i < indexinfo.length; ++i) { // ids[0] is empty
						pos.add((byte)Integer.parseInt(indexinfo[i]));
						list.add(pos);
					 }
					}
				    System.out.println(results[0]);	
					dict.put(results[0], list);
				}
				br.close();
				System.out.println("dict size:"+  dict.size());
				System.out.println("Loaded " + fileName);
			}
	        ArrayList<ArrayList<Byte>> tmp = dict.get(term);
			ArrayList<Integer> ids = compresser.decompressTermIDs(tmp);
			System.out.println("Start bs!!!");
			int length = ids.size();
			if (docid >= ids.get(length - 1)) return -1;
			if (docid < ids.get(0)) return ids.get(0);
			return ids.get(binarySearch(term, 1, length - 1, docid, ids));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
  }
 

  private int binarySearch(String term, int low, int high, int docid, ArrayList<Integer> list){
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
	  for(int i = 0; i < ids.size() - 1; i++){
		  if(ids.get(i).intValue() != ids.get(i + 1).intValue()){
			  return false;
		  }
	  }
	  return true;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    if (termFrequency.containsKey(term) == false) return -1;
	String initial, fileName;
	HashMap<String, ArrayList<ArrayList<Byte>>> dict;
	int idx = (term.charAt(0) - 'a') / 5;
		
	switch (idx) {
		case 0: 
			initial = "a";
			dict = invertList_wiki.get(0);
			break;
		case 1: 
			initial = "f";
			dict = invertList_wiki.get(1);
			break;
		case 2:
			initial = "k";
			dict = invertList_wiki.get(2);
			break;
		case 3:
			initial = "p";
			dict = invertList_wiki.get(3);
			break;
		case 4:
		case 5:
			initial = "u";
			dict = invertList_wiki.get(4);
			break;
		default:
			initial = "num";
			dict = invertList_wiki.get(5);
			break;
		}
		try {
			fileName = _options._indexPrefix + "/" + initial + ".idx";
			if (dict.isEmpty()) { // load the map
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String record;
				ArrayList<ArrayList<Byte>> list = new ArrayList<ArrayList<Byte>>();
		    	while ((record = br.readLine()) != null) {
					String[] results = record.split(";");
					for(int j = 1; j < results.length; j++){
					String[] indexinfo = results[j].split(" ");
				    ArrayList<Byte> pos = new ArrayList<Byte>();
					for (int i = 1; i < indexinfo.length; ++i) { // ids[0] is empty
						pos.add((byte)Integer.parseInt(indexinfo[i]));
						list.add(pos);
					 }
					}
				    System.out.println(results[0]);	
					dict.put(results[0], list);
				}
				br.close();
				System.out.println("dict size:"+  dict.size());
				System.out.println("Loaded " + fileName);
			}
	        ArrayList<ArrayList<Byte>> tmp = dict.get(term);
			ArrayList<Integer> ids = compresser.decompressTermIDs(tmp);
		/*	System.out.println("Start bs!!!");
			int length = ids.size();
			if (docid >= ids.get(length - 1)) return -1;
			if (docid < ids.get(0)) return ids.get(0);
			return ids.get(binarySearch(term, 1, length - 1, docid, ids));
*/        
             return ids.size();
		 } catch (Exception e) {
			e.printStackTrace();
			return -1;
		}  
  }

  @Override
  public int corpusTermFrequency(String term) {
    if(termFrequency.containsKey(term)) return termFrequency.get(term);
	else  return 0;
  }

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
	  int docid  = -1;
	  for(int  i = 0; i < _documents.size(); i++){
		  if(url.equals(_documents.get(i).getUrl()))
	          docid = _documents.get(i)._docid;
			  //return documentTermFrequency(term, _documents.get(i)._docid);
	  }
	  return docid;
  }
  
  public int documentTermFrequency(String term, int docid){
  if (termFrequency.containsKey(term) == false) return -1;
	String initial, fileName;
	HashMap<String, ArrayList<ArrayList<Byte>>> dict;
	int idx = (term.charAt(0) - 'a') / 5;
		
	switch (idx) {
		case 0: 
			initial = "a";
			dict = invertList_wiki.get(0);
			break;
		case 1: 
			initial = "f";
			dict = invertList_wiki.get(1);
			break;
		case 2:
			initial = "k";
			dict = invertList_wiki.get(2);
			break;
		case 3:
			initial = "p";
			dict = invertList_wiki.get(3);
			break;
		case 4:
		case 5:
			initial = "u";
			dict = invertList_wiki.get(4);
			break;
		default:
			initial = "num";
			dict = invertList_wiki.get(5);
			break;
		}
		try {
			fileName = _options._indexPrefix + "/" + initial + ".idx";
			if (dict.isEmpty()) { // load the map
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String record;
				ArrayList<ArrayList<Byte>> list = new ArrayList<ArrayList<Byte>>();
		    	while ((record = br.readLine()) != null) {
					String[] results = record.split(";");
					for(int j = 1; j < results.length; j++){
					String[] indexinfo = results[j].split(" ");
				    ArrayList<Byte> pos = new ArrayList<Byte>();
					for (int i = 1; i < indexinfo.length; ++i) { // ids[0] is empty
						pos.add((byte)Integer.parseInt(indexinfo[i]));
						list.add(pos);
					 }
					}
				    System.out.println(results[0]);	
					dict.put(results[0], list);
				}
				br.close();
				System.out.println("dict size:"+  dict.size());
				System.out.println("Loaded " + fileName);
			}
	        ArrayList<ArrayList<Byte>> tmp = dict.get(term);
			ArrayList<Integer> ids = compresser.decompressTermIDs(tmp);
		    int size = -1;
			for(ArrayList<Byte> list: tmp){
				if(docid == compresser.decompressID(list))
					size = list.size() - 1;
			}
			return size;
            
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}  
  }
  public void weakRefgc(){
	  Object obj =new Object();
	  WeakReference ref =  new WeakReference<Object>(obj);
	  obj = null;
	  while(ref.get() != null){
		  System.gc();
	  }
  }
 /* public static void main(String args[]) throws IOException{
     Options option = new Options("conf/engine.conf");
	 IndexerInvertedCompressed index = new IndexerInvertedCompressed(option);
	 index.constructIndex();
     Query query = new Query("Yahoo");
	 query.processQuery();
	 Document doc  = index.nextDoc(query, 5000);
	 if(doc != null){
	   System.out.println(doc._docid);
	 }else{
		 System.out.println("null");
	 }
  }*/
}
