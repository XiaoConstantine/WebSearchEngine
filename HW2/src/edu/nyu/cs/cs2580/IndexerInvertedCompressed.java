package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.List;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable {
 private static final long serialVerisionUID = 1088111905740085031L;
  private HashMap<String, List<Tuple> > invertList = new HashMap<String, List<Tuple> >();
  private Stemmer stemmer = new Stemmer();
    
//  private ByteAlignCompress bac = new ByteAlignCompress();
    
  private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
 
  private Vector<String> _terms  = new Vector<String>();
  
  private Map<Integer, Integer> _termDocFrequency = new HashMap<Integer, Integer>();
  
  private Map<Integer, Integer> _termCopusFrequency = new HashMap<Integer,Integer>();
  
  private Vector<Document> _documents = new Vector<Document>();
    
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
      
      if(_options._corpusPrefix.equals("data/simple")){
          String corpusFile = _options._corpusPrefix + "/corpus.tsv"; //not certain
          System.out.println("Construct index from: " + corpusFile);
      
          BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
          try{
              String line = null;
              while((line = reader.readLine()) != null){
                processDocument(corpusFile,line);
              }
            }finally{
              reader.close();
         }
      }else if(_options._corpusPrefix.equals("data/wiki")){
           File folder = new File(_options._corpusPrefix);
           File[] files = folder.listFiles();
            for(File file: files){
              if(!file.isDirectory()){
                 System.out.println(file.getName());
                  //processWiki(file,content);
              }
          }
      }
      System.out.println(
                         "Indexed " + Integer.toString(_numDocs) + " docs with " +
                         Long.toString(_totalTermFrequency) + " terms.");
      
      String indexFile = _options._indexPrefix + "/corpus.idx";
      System.out.println("Store index to: " + indexFile);
      ObjectOutputStream writer =
      new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this);
      writer.close();
  }
  
  private void processDocument(String url, String content){
      Scanner s = new Scanner(content).useDelimiter("\t");
      String title = s.next();
      Vector<Integer> titleTokens = new Vector<Integer>();
      readTermVector(url, title, titleTokens);
      
      Vector<Integer> bodyTokens = new Vector<Integer>();
      readTermVector(url, s.next(), bodyTokens);
      
      int numViews = Integer.parseInt(s.next());
      s.close();
      DocumentIndexed doc = new DocumentIndexed(_documents.size(), this);
      doc.setUrl(url);
      doc.setTitle(title);
      doc.setNumViews(numViews);
      _documents.add(doc);
      ++_numDocs;
      
      Set<Integer> uniqueTerms = new HashSet<Integer>();
      updateStatistics(titleTokens, uniqueTerms);
      updateStatistics(bodyTokens, uniqueTerms);
      
      for(Integer idx: uniqueTerms){
          _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
      }
      
  }
    
  private void readTermVector(String url, String content, Vector<Integer> tokens){
      Scanner s = new Scanner(content);
      int docid = Integer.parseInt(url);
      while(s.hasNext()){
          String _token = s.next();
          String token = stem(_token);
          int idx = -1;
          if(_dictionary.containsKey(token) && invertList.get(token)!=null){
              idx = _dictionary.get(token);
              List<Tuple> Idx = invertList.get(token);
              Idx.add(new Tuple(docid, idx));
          }else{
              idx = _terms.size();
              _terms.add(token);
              _dictionary.put(token, idx);
              _termCopusFrequency.put(idx, 0);
              _termDocFrequency.put(idx,0);
              List<Tuple> Idx = invertList.get(token);
              Idx.add(new Tuple(docid, idx));
              invertList.put(token, Idx);
          }
          tokens.add(idx);
      }
      return;
  }
  
  private void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques){
      for(int idx: tokens){
          uniques.add(idx);
          _termCopusFrequency.put(idx, _termCopusFrequency.get(idx) + 1);
          ++_totalTermFrequency;
      }
  }
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
  }

  @Override
  public Document getDoc(int docid) {
    //SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
      return (docid < 0 || docid >= _documents.size())?null:_documents.get(docid);
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
      return _dictionary.containsKey(term)? _termCopusFrequency.get(_dictionary.get(term)):0;
  }
   
   /*
     Call Porter-stem to process tokens
    */
   public String stem(String token){
        token = token.toLowerCase();
        stemmer.add(token.toCharArray(),token.toCharArray().length);
        stemmer.stem();
        return stemmer.toString();
    }

   public Vector<String> getTermVector(Vector<Integer> tokens) {
        Vector<String> retval = new Vector<String>();
        for (int idx : tokens) {
            retval.add(_terms.get(idx));
        }
        return retval;
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
    
  private class Tuple{
      int docid;
      int pos;
      public Tuple(int docid, int pos){
          this.docid = docid;
          this.pos = pos;
      }
  }
}
