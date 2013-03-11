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
<<<<<<< HEAD
    
    //private Map<Integer, LinkedList<Tuple> > invertIndex = new HashMap<Integer,LinkedList<Tuple>>();
    //DS: <term,<docid1,pos1,pos2,...> <docid2,pos1,pos2,...>>
    private Map<String, ArrayList<ArrayList<Integer>>> invertedIndex = new HashMap<String,ArrayList<ArrayList<Integer>>>();
    
=======
   
	/*The first one is very clear but very inefficient
	 *Impove Tupe into:
	 *docid->list(occur1, occur2,.....)
	 *Meanwhile, we dont want to store the whole string in the Map 
	 *Just use int_representation
	 * */
//    private Map<String, List<Tuple>> invertList = new HashMap<String, List<Tuple>>();
    private Map<Integer, LinkedList<Tuple> > invertIndex = new HashMap<Integer,LinkedList<Tuple>>();

>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    //Maps each term to their integer representation
    //private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
    
  /*  private class Tuple implements Serializable{
        private static final long serialVersionUID = 1074551805740585098L;
        int docid;
		List<Integer> pos = new LinkedList<Integer>();
        
		public Tuple(int docid, int pos){
            this.docid = docid;
            this.pos.add(pos);
        }
<<<<<<< HEAD
        
		public int getTupleID(){
			return this.docid;
		}
        
		public List<Integer> getTupleList(){
			return this.pos;
		}
        
=======

		public int getTupleID(){
			return this.docid;
		}

		public List<Integer> getTupleList(){
			return this.pos;
		}

>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
		public String toString(){
			String content = "";
			for(Integer i: this.pos){
				content += Integer.toString(i) + " ";
			}
			return Integer.toString(docid)+ " " + content + ";";
		}
<<<<<<< HEAD
        
    }*/
    
    
=======

    }
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    //All unique terms appeared in corpus. Offsets are integer representation.
    private Vector<String> _terms = new Vector<String>();
    
    //Term document frequency, key is the integer representation of the term and 
    //value is the number of documents the term appears in.
    private Map<String, Integer> _termDocFrequency = new HashMap<String, Integer>();
    
    //Term frequency, key is the integer representation of the term and value is 
    //the number of the times the term appears in the corpus.
    private Map<String, Integer> _termCorpusFrequency = new HashMap<String, Integer>();
    
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
			int round = 0;
            
			if(files.length % 500 == 0) round = files.length/500;
			else round = files.length/500 + 1;
			for(int i = 0; i < round; i++){
				for(int j = 0; j < 500 && (i*500 + j < files.length); j++){
					processWikiDocument(files[docid], docid);
				    docid++;
				}
				if(i == 0){
					processWriting(i);
				}else{
					processMerging(i, round);
				}
                System.out.println("round" + i + "\n");
			}
        }
<<<<<<< HEAD
=======
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
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
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
<<<<<<< HEAD
=======
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
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    }
   
    public void refresh(){
		invertIndex.clear();
	}

	public void processWriting(int round) throws IOException{
		String indexFile = _options._indexPrefix + "/" + round+"tmp.idx";
		FileWriter fWriter = new FileWriter(indexFile);
		BufferedWriter writer = new BufferedWriter(fWriter);
		StringBuilder sb;
		//sb.append(Integer.toString(_numDocs) + ";" + Long.toString(_totalTermFrequency));
        //sb.append("\n");

		//writer.write(Integer.toString(_numDocs) + " " + Long.toString(_totalTermFrequency));
	//	for(Document doc: _documents){
			//writer.write(((DocumentIndexed)doc).toString());
	//		sb.append(((DocumentIndexed)doc).toString()+" ");
  //		}
        for(Integer idx: invertIndex.keySet()){
			sb = new StringBuilder();
			System.out.println(invertIndex.get(idx).size());
			int i = 0;
			sb.append(Integer.toString(idx) + ";");
			for(Tuple tup: invertIndex.get(idx)){
			    i++;
				System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
				sb.append(tup.toString());
				//writer.write(Integer.toString(idx) + tup.toString());
			   // sb.append("\n");
			}
			
			System.out.println("Finished" + i+"\n");

			//sb.append("\n");
			writer.write(sb.toString() + "\n");	
		}
		writer.close();
		refresh();

	}
    
<<<<<<< HEAD
    public void refresh(){
		invertIndex.clear();
	}
    
	public void processWriting(int round) throws IOException{
		String indexFile = _options._indexPrefix + "/" + round+"tmp.idx";
		FileWriter fWriter = new FileWriter(indexFile);
		BufferedWriter writer = new BufferedWriter(fWriter);
		StringBuilder sb;
		//sb.append(Integer.toString(_numDocs) + ";" + Long.toString(_totalTermFrequency));
        //sb.append("\n");
        
		//writer.write(Integer.toString(_numDocs) + " " + Long.toString(_totalTermFrequency));
        //	for(Document doc: _documents){
        //writer.write(((DocumentIndexed)doc).toString());
        //		sb.append(((DocumentIndexed)doc).toString()+" ");
        //		}
        for(Integer idx: invertIndex.keySet()){
			sb = new StringBuilder();
			System.out.println(invertIndex.get(idx).size());
			int i = 0;
			sb.append(Integer.toString(idx) + ";");
			for(Tuple tup: invertIndex.get(idx)){
			    i++;
				System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
				sb.append(tup.toString());
				//writer.write(Integer.toString(idx) + tup.toString());
                // sb.append("\n");
			}
			
			System.out.println("Finished" + i+"\n");
            
			//sb.append("\n");
			writer.write(sb.toString() + "\n");	
		}
		writer.close();
		refresh();
        
	}
    
=======
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    public void processMerging(int curr, int round) throws IOException{
		System.out.println("Process merging\n");
		File oldfile = new File(_options._indexPrefix + "/" + (curr - 1) + "tmp.idx");
		BufferedReader br = new BufferedReader(new FileReader(oldfile));
	    File newfile;
		if(curr == round - 1){
<<<<<<< HEAD
            newfile = new File(_options._indexPrefix+"/"+"final.idx");
		}
		else{
            newfile = new File(_options._indexPrefix + "/" + curr + "tmp.idx");
=======
	         newfile = new File(_options._indexPrefix+"/"+"final.idx");
		}
		else{
	          newfile = new File(_options._indexPrefix + "/" + curr + "tmp.idx");
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(newfile));
		String preRecord = br.readLine();
		ArrayList<Integer> keys = new ArrayList<Integer>(invertIndex.keySet());
        int i = 0;
		while(preRecord!=null&& i < keys.size() ){
			StringBuilder sb = new StringBuilder();
			String pre = preRecord.split(";")[0];
            int idx = Integer.parseInt(pre);
<<<<<<< HEAD
            
=======
                 
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
			//Integer idx = _dictionary.get(pre);
			if(idx < keys.get(i)){
				sb.append(preRecord);
				bw.write(sb.toString() + "\n");
				preRecord = br.readLine();
			}else if(idx > keys.get(i)){
                sb.append(Integer.toString(keys.get(i))+ ";"); 
				for(Tuple tup: invertIndex.get(keys.get(i))){
<<<<<<< HEAD
                    System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
                    sb.append(tup.toString());
                    bw.write(sb.toString() + "\n");
                    i++;
                }
			}else{
				sb.append(preRecord);
                for(Tuple tup: invertIndex.get(idx)){
                    System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
                    sb.append(tup.toString());
                }				
=======
				System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
				sb.append(tup.toString());
				bw.write(sb.toString() + "\n");
				i++;
			  }
			}else{
				sb.append(preRecord);
                for(Tuple tup: invertIndex.get(idx)){
				System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
				sb.append(tup.toString());
			 }				
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
				bw.write(sb.toString() + "\n");
				preRecord = br.readLine();
                i++;
			}
		}
		while(preRecord != null ){
         	StringBuilder sb = new StringBuilder();
			String pre = preRecord.split(";")[0];
            int idx = Integer.parseInt(pre);
            sb.append(preRecord);
			bw.write(sb.toString() + "\n");
<<<<<<< HEAD
            preRecord = br.readLine();
=======
           preRecord = br.readLine();
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
		}
		while( i  < keys.size()){
            StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(keys.get(i))+ ";"); 
			for(Tuple tup: invertIndex.get(keys.get(i))){
<<<<<<< HEAD
                System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
                sb.append(tup.toString());
=======
			System.out.println("Tuple size:" + tup.getTupleList().size()+"\n");
			sb.append(tup.toString());
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
			}
			bw.write(sb.toString() + "\n");
			i++;
		}
		br.close();
		bw.close();
		oldfile.delete();
		refresh();
	}
<<<<<<< HEAD
    
=======

>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    /**
     * For parsing simple document
     * each line is a document, format as follow:
     * title<TAB>body
     * @param line, docid
     * 
     */
    private void processSimpleDocument(String line, int docid){
        Scanner s = new Scanner(line).useDelimiter("\t");
        String title = s.next();
		int pos = 0;
        //phrase title
<<<<<<< HEAD
        ArrayList<String> titleTokens = new ArrayList<String>();
        phraseLine(docid, title, titleTokens," ");
        //phrase body
        ArrayList<String> bodyTokens = new ArrayList<String>();
=======
        ArrayList<Integer> titleTokens = new ArrayList<Integer>();
        phraseLine(docid, title, titleTokens," ");
        //phrase body
        ArrayList<Integer> bodyTokens = new ArrayList<Integer>();
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
        phraseLine(docid, s.next(), bodyTokens," ");
        
        int numViews = Integer.parseInt(s.next());
        s.close();
        DocumentIndexed doc = new DocumentIndexed(docid, this);
        doc.setUrl(Integer.toString(docid));
        doc.setTitle(title);
        doc.setNumViews(numViews);
        _documents.add(doc);
        ++_numDocs;
<<<<<<< HEAD
        Set<String> uniqueTerms = new HashSet<String>();
=======
        Set<Integer> uniqueTerms = new HashSet<Integer>();
        //updateStatistics(titleTokens,uniqueTerms);
        //updateStatistics(bodyTokens,uniqueTerms);
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
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
<<<<<<< HEAD
                            // phraseFile(docid, line.substring(0, line.indexOf("<script>")), bodyTokens);
=======
                           // phraseFile(docid, line.substring(0, line.indexOf("<script>")), bodyTokens);
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
							scriptFlag = 1;
							line = line.substring(line.indexOf("<script>") + 8);
						} else if (line.contains("<script")) {
							// parse the no script content and check the remain string
<<<<<<< HEAD
                            //    phraseFile(docid,line.substring(0, line.indexOf("<script")),bodyTokens);
=======
                        //    phraseFile(docid,line.substring(0, line.indexOf("<script")),bodyTokens);
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
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
    
<<<<<<< HEAD
    private void updateInvertedIndex(ArrayList<String> tokens, Set<String> uniqueTerms, int docid, int pos){
        Integer token;  
        for(int i = 0; i <tokens.size();i++){
            token =  tokens.get(i);
            uniqueTerms.add(token);
            LinkedList<Tuple> Idx = invertIndex.get(token);
            if(Idx.size() ==0  || Idx.get(Idx.size()-1).getTupleID()!=docid){	
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
    
    private void phraseLine(int docid,  String content, ArrayList<String> tokens, String delimiter){
        Scanner s = new Scanner(content).useDelimiter(delimiter);
        while(s.hasNext()){
            String token = stem(s.next());
			//int idx = -1;
            //if(_dictionary.containsKey(token)){
               /* idx  = _dictionary.get(token);
				if(invertIndex.get(idx) == null){
					invertIndex.put(idx, new LinkedList<Tuple>());
				}*/
                if(invertedIndex.get(token) == null){
                    ArrayList<Integer> l = new ArrayList<Integer>();
                    l.add(docid);
                    ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
                    list.add(l);
                    invertedIndex.put(token, list);
                }else{
                    _terms.add(token);
                    _termCorpusFrequency.put(token,0);
                    _termDocFrequency.put(token,0);
                }
                
           // }else{
                //idx = _terms.size();
                //_terms.add(token);
               // _dictionary.put(token,idx);
               // _termCorpusFrequency.put(idx,0);
               // _termDocFrequency.put(idx,0);
               // invertIndex.put(idx,new LinkedList<Tuple>());
		//	}
            tokens.add(token);
        }
	}
    
=======
    private void updateInvertedIndex(ArrayList<Integer> tokens, Set<Integer> uniqueTerms, int docid, int pos){
         Integer token;  
		 for(int i = 0; i <tokens.size();i++){
			  token =  tokens.get(i);
			  uniqueTerms.add(token);
			  LinkedList<Tuple> Idx = invertIndex.get(token);
			  if(Idx.size() ==0  || Idx.get(Idx.size()-1).getTupleID()!=docid){	
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
				if(invertIndex.get(idx) == null){
					invertIndex.put(idx, new LinkedList<Tuple>());
				}
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
   
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
    private void phraseFile(int docid, String content, ArrayList<Integer> tokens){
		Scanner s = new Scanner(content);
        while(s.hasNext()){
            String token = stem(s.next());
            int idx = -1;
            if(_dictionary.containsKey(token)){
                idx  = _dictionary.get(token);
            	if(invertIndex.get(idx) == null){
					invertIndex.put(idx, new LinkedList<Tuple>());
				}   
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
    
<<<<<<< HEAD
=======
   /* private void updateStatistics(ArrayList<Integer> tokens, Set<Integer> uniques){
        for(int idx: tokens){
            uniques.add(idx);
            _termCorpusFrequency.put(idx,_termCorpusFrequency.get(idx)+1);
            ++_totalTermFrequency;
        }
    }*/
    
>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
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
<<<<<<< HEAD
        
=======

>>>>>>> cbbec08a33f4adcded05c93e01108d8bd3a2dfdc
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
        //SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
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
