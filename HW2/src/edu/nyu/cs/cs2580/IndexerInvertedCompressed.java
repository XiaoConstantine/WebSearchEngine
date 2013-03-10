package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.Iterator;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable {
    private static final long serialVerisionUID = 1078111905740087931L;
    private Stemmer stemmer = new Stemmer();
    private ByteAlignCompress compresser = new ByteAlignCompress();
    
    private Map<Byte, List<Tuple> > invertIndex = new HashMap<Byte, List<Tuple>>();
    
    //Maps each term to their integer representation
    private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
    
    private class Tuple implements Serializable{
        private static final long serialVersionUID = 1073541805740585098L;
        byte docid;
		List<Byte> pos;
        public Tuple(byte docid){
            this.docid = docid;
            this.pos = new ArrayList<Byte>();
        }
        
		public byte getTupleID(){
			return this.docid;
		}
        
		public List<Byte> getTupleList(){
			return this.pos;
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
    
    public IndexerInvertedCompressed(Options options) {
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
                System.out.println(docid);
            }
        }
        for(Integer swIntPresentation: _termCorpusFrequency.keySet()){
            String term = _terms.get(swIntPresentation);
            if((float)_termCorpusFrequency.get(swIntPresentation) / _totalTermFrequency > 0.06){
                System.out.println("test stop word");
                invertIndex.remove(swIntPresentation.byteValue());
                _totalTermFrequency--;
            }
        }
        System.out.println(
                           "Indexed " + Integer.toString(_numDocs) + " docs with " +
                           Long.toString(_totalTermFrequency) + " terms.");
        String indexFile = _options._indexPrefix + "/corpus.idx";
        System.out.println("Store index to: " + indexFile);
        ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
        writer.writeObject(this);
        writer.close();
    }
    
    
    /**
     * For parsing simple ducument
     *
     */
    private void processSimpleDocument(String line, int docid){
        Scanner s = new Scanner(line).useDelimiter("\t");
        String title = s.next();
        //phrase title
        Vector<Integer> titleTokens = new Vector<Integer>();
        phraseLine(docid, title, titleTokens," ");
        //phrase body
        Vector<Integer> bodyTokens = new Vector<Integer>();
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
        updateStatistics(titleTokens,uniqueTerms);
        updateStatistics(bodyTokens,uniqueTerms);
        
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
        Vector<Integer> titleTokens = new Vector<Integer>();
        phraseLine(docid,file.getName(),titleTokens,"_");
        
        //phrase file content
        Vector<Integer> bodyTokens = new Vector<Integer>();
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
                            phraseFile(docid, line.substring(0, line.indexOf("<script>")), bodyTokens);
							scriptFlag = 1;
							line = line.substring(line.indexOf("<script>") + 8);
						} else if (line.contains("<script")) {
							// parse the no script content and check the remain string
                            phraseFile(docid,line.substring(0, line.indexOf("<script")),bodyTokens);
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
        updateStatistics(titleTokens,uniqueTerms);
        updateStatistics(bodyTokens,uniqueTerms);
        
        for(Integer idx: uniqueTerms){
            _termDocFrequency.put(idx,_termDocFrequency.get(idx)+1);
        }
        
        
    }
    
    private void phraseLine(int docid, String content, Vector<Integer> tokens, String delimiter){
        Scanner s = new Scanner(content).useDelimiter(delimiter);
        int pos = 0;
        while(s.hasNext()){
            String token = stem(s.next());
            int idx = -1;
            if(_dictionary.containsKey(token)){
                idx  = _dictionary.get(token);
                byte idx_compressed = compresser.compressSingle(idx);
                byte docid_compressed = compresser.compressSingle(docid);

				if(invertIndex.get(idx_compressed)!=null){
                    List<Tuple> Idx = invertIndex.get(idx_compressed);
					for(Tuple tup: Idx){
						if(tup.getTupleID() == docid_compressed){
							tup.getTupleList().add(compresser.compressSingle(pos));
						}
					}
				}
            }else{
                idx = _terms.size();
				byte idx_compressed = compresser.compressSingle(idx);
				byte docid_compressed = compresser.compressSingle(docid);
                _terms.add(token);
                _dictionary.put(token,idx);
                _termCorpusFrequency.put(idx,0);
                _termDocFrequency.put(idx,0);
                List<Tuple> Idx = new ArrayList<Tuple>();
                Idx.add(new Tuple(docid_compressed));
				for(Tuple tup: Idx){
					if(tup.getTupleID() == docid_compressed){   
						tup.getTupleList().add(compresser.compressSingle(pos));
					}
				}
                invertIndex.put(idx_compressed,Idx);
            }
            pos++;
            tokens.add(idx);
        }
    }
    
    
    private void phraseFile(int docid, String content, Vector<Integer> tokens){
		Scanner s = new Scanner(content);
        int pos = 0;
        while(s.hasNext()){
            String token = stem(s.next());
            int idx = -1;
            if(_dictionary.containsKey(token)){
                idx  = _dictionary.get(token);
				byte idx_compressed = compresser.compressSingle(idx);
				byte docid_compressed = compresser.compressSingle(docid);
				if(invertIndex.get(idx)!=null){
                    List<Tuple> Idx = invertIndex.get(idx_compressed);
					for(Tuple tup: Idx){
						if(tup.getTupleID() == docid_compressed){
							tup.getTupleList().add(compresser.compressSingle(pos));
						}
					}
				}
            }else{
                idx = _terms.size();
				byte idx_compressed = compresser.compressSingle(idx);
				byte docid_compressed = compresser.compressSingle(docid);
                _terms.add(token);
                _dictionary.put(token,idx);
                _termCorpusFrequency.put(idx,0);
                _termDocFrequency.put(idx,0);
                List<Tuple> Idx = new ArrayList<Tuple>();
                Idx.add(new Tuple(docid_compressed));
				for(Tuple tup: Idx){
					if(tup.getTupleID() == docid_compressed){
						tup.getTupleList().add(compresser.compressSingle(pos));
					}
				}
                invertIndex.put(idx_compressed,Idx);
            }
            pos++;
            tokens.add(idx);
        }
        
    }
    
    private void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques){
        for(int idx: tokens){
            uniques.add(idx);
            _termCorpusFrequency.put(idx,_termCorpusFrequency.get(idx)+1);
            ++_totalTermFrequency;
        }
    }
    
    public String stem(String token) {
		token = token.toLowerCase();
		stemmer.add(token.toCharArray(), token.toCharArray().length);
		stemmer.stem();
		return stemmer.toString();
	}
    
    @Override
    public void loadIndex() throws IOException, ClassNotFoundException {
        String indexFile = _options._indexPrefix + "/corpus.idx";
        System.out.println("Load index from: " + indexFile);
        
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
        IndexerInvertedCompressed loaded = (IndexerInvertedCompressed) reader.readObject();
        
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
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
  public Document nextDoc(Query query, int docid) {
    return null;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
      return _dictionary.containsKey(term)? _termDocFrequency.get(_dictionary.get(term)):0;
  }

  @Override
  public int corpusTermFrequency(String term) {
      return 0;
  }
   
  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
    /*  if(!_dictionary.containsKey(term)){
          return 0;
      }else{
          Integer idx = _dictionary.get(term);
      }*/
      return 0;
  }
 
}
