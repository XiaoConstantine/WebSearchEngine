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
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends Indexer implements Serializable {
    
    private static final long serialVerisionUID = 1088111905740087931L;
   
	/*The first one is very clear but very inefficient
	 *Impove Tupe into:
	 *docid->list(occur1, occur2,.....)
	 *Meanwhile, we dont want to store the whole string in the Map 
	 *Just use int_representation
	 * */
//    private Map<String, List<Tuple>> invertList = new HashMap<String, List<Tuple>>();
    private Map<Integer, LinkedList<Tuple> > invertIndex = new HashMap<Integer,LinkedList<Tuple>>();

    //Maps each term to their integer representation
    private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
    
    private class Tuple implements Serializable{
        private static final long serialVersionUID = 1074551805740585098L;
        int docid;
        //int pos;
		List<Integer> pos = new LinkedList<Integer>();
        public Tuple(int docidi, int pos){
            this.docid = docid;
            this.pos.add(pos);
        }

		public int getTupleID(){
			return this.docid;
		}

		public List<Integer> getTupleList(){
			return this.pos;
		}

		public String toString(){
			String content = "";
			for(Integer i: this.pos){
				content += Integer.toString(i) + " ";
			}
			return Integer.toString(docid)+" " + content;
		}

    }
    //All unique terms appeared in corpus. Offsets are integer representation.
    private Vector<String> _terms = new Vector<String>();
    
    //Term document frequency, key is the integer representation of the term and 
    //value is the number of documents the term appears in.
    private Map<Integer, Integer> _termDocFrequency = new HashMap<Integer, Integer>();
    
    //Term frequency, key is the integer representation of the term and value is 
    //the number of the times the term appears in the corpus.
    private Map<Integer, Integer> _termCorpusFrequency = new HashMap<Integer, Integer>();
    
    //Store all Document in memory.
    private Vector<Document> _documents = new Vector<Document>();
    
    public IndexerInvertedOccurrence(Options options) {
        super(options);
        System.out.println("Using Indexer: " + this.getClass().getSimpleName());
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
        }else if(_options._corpusPrefix.equals("data/wiki")){
            File folder = new File(_options._corpusPrefix);
            File[] files = folder.listFiles();
            int docid = 0;
            for (File file: files){
                processWikiDocument(file,docid);
                docid++;
				if(docid % 1000 == 0 ){
                  System.out.println(docid);
				  String indexFile = _options._indexPrefix +"/" +"wiki_corpus.idx" ;
				  processWriting(Integer.toString(docid)+indexFile);
				}
            }
        }
       /*In hashmap better to use iterator to remove element(In case of thread safe)*/
       /* Iterator itr = invertList.keySet().iterator();
	    while(itr.hasNext()){
            for(Integer swIntPresentation: _termCorpusFrequency.keySet()){
            String term = _terms.get(swIntPresentation);
            if((float)_termCorpusFrequency.get(swIntPresentation) / _totalTermFrequency > 0.06){
                System.out.println("test stop word");
                itr.remove(term);
                _totalTermFrequency--;
            }
			itr.next();
         }
		}*/
        for(Integer swIntPresentation: _termCorpusFrequency.keySet()){
            String term = _terms.get(swIntPresentation);
            if((float)_termCorpusFrequency.get(swIntPresentation) / _totalTermFrequency > 0.06){
                System.out.println("test stop word");
                invertIndex.remove(swIntPresentation);
                _totalTermFrequency--;
            }
        }
        System.out.println(
                           "Indexed " + Integer.toString(_numDocs) + " docs with " +
                           Long.toString(_totalTermFrequency) + " terms.");
        String indexFile = _options._indexPrefix + "/corpus.idx";
        System.out.println("Store index to: " + indexFile);
       /* FileWriter fWriter = new FileWriter(indexFile);
		BufferedWriter writer = new BufferedWriter(fWriter);
		writer.write(Integer.toString(_numDocs) + " " + Integer.toString(_totalTermFrequency));
		for(Document doc: _documents){
			writer.write((DocumentIndexed)doc.toString());
		}
		writer.flush();
        for(Integer idx: invertIndex.keySet()){
			for(Tuple tup: idx.get(tup)){
				writer.write(Integer.toString(idx) + tup.toString());
			}
			writer.newLine();
		}
		writer.flush();
		writer.close();
		*/
       //processWriting(indexFile);
    }
   
	public void processWriting(String indexFile){
      try{
		FileWriter fWriter = new FileWriter(indexFile);
		BufferedWriter writer = new BufferedWriter(fWriter);
		writer.write(Integer.toString(_numDocs) + " " + Long.toString(_totalTermFrequency));
		for(Document doc: _documents){
			writer.write(((DocumentIndexed)doc).toString());
		}
		writer.flush();
        for(Integer idx: invertIndex.keySet()){
			for(Tuple tup: invertIndex.get(idx)){
				writer.write(Integer.toString(idx) + tup.toString());
			}
			writer.newLine();
		}
		writer.flush();
	}catch(IOException e){

	}
	}
    
    /**
     * For parsing simple ducument
     * 
     */
    private void processSimpleDocument(String line, int docid){
        Scanner s = new Scanner(line).useDelimiter("\t");
        String title = s.next();
		int pos = 0;
        //phrase title
        ArrayList<Integer> titleTokens = new ArrayList<Integer>();
        phraseLine(docid, title, titleTokens," ");
        //phrase body
        ArrayList<Integer> bodyTokens = new ArrayList<Integer>();
        phraseLine(docid, s.next(), bodyTokens," ");
        
        int numViews = Integer.parseInt(s.next());
        s.close();
        DocumentIndexed doc = new DocumentIndexed(docid, this);
        doc.setUrl(Integer.toString(docid));
        doc.setTitle(title);
        doc.setNumViews(numViews);
        _documents.add(doc);
        ++_numDocs;
        Set<Integer> uniqueTerms = new HashSet<Integer>();
        //updateStatistics(titleTokens,uniqueTerms);
        //updateStatistics(bodyTokens,uniqueTerms);
        updateInvertedIndex(titleTokens, uniqueTerms, docid, 0);
		updateInvertedIndex(bodyTokens, uniqueTerms, docid, titleTokens.size());
        for(Integer idx: uniqueTerms){
            _termDocFrequency.put(idx,_termDocFrequency.get(idx)+1);
        }
    }
    
    /**
     * For parsing wiki ducuments
     * 
     */
    private void processWikiDocument(File file,int docid) throws IOException{
        DocumentIndexed doc = new DocumentIndexed(docid,this);
        doc.setUrl(file.getAbsolutePath());
        doc.setTitle(file.getName());
        //phrase title
        ArrayList<Integer> titleTokens = new ArrayList<Integer>();
        phraseLine(docid,file.getName(),titleTokens,"_");
        
        //phrase file content
        ArrayList<Integer> bodyTokens = new ArrayList<Integer>();
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
                           // phraseFile(docid, line.substring(0, line.indexOf("<script>")), bodyTokens);
							scriptFlag = 1;
							line = line.substring(line.indexOf("<script>") + 8);
						} else if (line.contains("<script")) {
							// parse the no script content and check the remain string
                        //    phraseFile(docid,line.substring(0, line.indexOf("<script")),bodyTokens);
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
				// parse the content, add them into dictionary
                /* Below lines are also processing html, why add them
				 * into parseFile function?
				 * */
				
				line = line.replaceAll("<[^>]*>", " ");
				line = line.replaceAll("\\pP|\\pS|\\pC", " ");
				
				phraseFile(docid,line,bodyTokens);
			}
		} finally {
			reader.close();
		}
        
        _documents.add(doc);
        ++_numDocs;
        Set<Integer> uniqueTerms = new HashSet<Integer>();
        updateInvertedIndex(titleTokens, uniqueTerms, docid, 0);
		updateInvertedIndex(bodyTokens, uniqueTerms, docid, titleTokens.size());
        for(Integer idx: uniqueTerms){
            _termDocFrequency.put(idx,_termDocFrequency.get(idx)+1);
        }
        
        
    }
    
    private void updateInvertedIndex(ArrayList<Integer> tokens, Set<Integer> uniqueTerms, int docid, int pos){
         Integer token;  
		 for(int i = 0; i <tokens.size();i++){
			  token =  tokens.get(i);
			  uniqueTerms.add(token);
			  LinkedList<Tuple> Idx = invertIndex.get(token);
			  if(Idx.size() == 0 || Idx.get(Idx.size()-1).getTupleID()!=docid){
					 Idx.add(new Tuple(docid, pos+i));
					 invertIndex.put(token, Idx);
			  }else{
				  boolean flag = false;
                  for(Tuple tup: Idx){
					  if(tup.getTupleID() == docid){
						  tup.getTupleList().add(pos+i);
						  flag = true;
						  break;
					  }
				  }
				  if(flag == false){
					 Idx.add(new Tuple(docid, pos+i));
				 }
                  
			  }
			  ++_totalTermFrequency;
		  }
	}

    private void phraseLine(int docid,  String content, ArrayList<Integer> tokens, String delimiter){
        Scanner s = new Scanner(content).useDelimiter(delimiter);
        while(s.hasNext()){
            String token = stem(s.next());
			int idx = -1;
            if(_dictionary.containsKey(token)){
                idx  = _dictionary.get(token);
            }else{
                idx = _terms.size();
                _terms.add(token);
                _dictionary.put(token,idx);
                _termCorpusFrequency.put(idx,0);
                _termDocFrequency.put(idx,0);
                invertIndex.put(idx,new LinkedList<Tuple>());
              
			}
            tokens.add(idx);
        }
    }
   

    private void phraseFile(int docid, String content, ArrayList<Integer> tokens){
		Scanner s = new Scanner(content);
        while(s.hasNext()){
            String token = stem(s.next());
            int idx = -1;
            if(_dictionary.containsKey(token)){
                idx  = _dictionary.get(token);
                }else{
                idx = _terms.size();
                _terms.add(token);
                _dictionary.put(token,idx);
                _termCorpusFrequency.put(idx,0);
                _termDocFrequency.put(idx,0);
               // LinkedList<Tuple> Idx = new LinkedList<Tuple>();
                invertIndex.put(idx,new LinkedList<Tuple>());
            }
            tokens.add(idx);
        }

    }
    
   /* private void updateStatistics(ArrayList<Integer> tokens, Set<Integer> uniques){
        for(int idx: tokens){
            uniques.add(idx);
            _termCorpusFrequency.put(idx,_termCorpusFrequency.get(idx)+1);
            ++_totalTermFrequency;
        }
    }*/
    
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
        
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
        IndexerInvertedOccurrence loaded = (IndexerInvertedOccurrence) reader.readObject();
        
        this.invertIndex = loaded.invertIndex;
        this._documents = loaded._documents;

        this._numDocs = _documents.size();
        for (Integer freq : loaded._termCorpusFrequency.values()) {
            this._totalTermFrequency += freq;
        }
        this._dictionary = loaded._dictionary;
        this._terms = loaded._terms;
        this._termCorpusFrequency = loaded._termCorpusFrequency;
        this._termDocFrequency = loaded._termDocFrequency;
        reader.close();
        
        System.out.println(Integer.toString(_numDocs) + " documents loaded " +
                           "with " + Long.toString(_totalTermFrequency) + " terms!");
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
    public Document nextDoc(Query query, int docid) {
        return null;
    }
    
    @Override
    public int corpusDocFrequencyByTerm(String term) {
        return 0;
    }
    
    @Override
    public int corpusTermFrequency(String term) {
        return 0;
    }
    
    @Override
    public int documentTermFrequency(String term, String url) {
        SearchEngine.Check(false, "Not implemented!");
        return 0;
    }
}
