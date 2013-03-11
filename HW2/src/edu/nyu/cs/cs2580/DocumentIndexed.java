package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.io.Serializable;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
  private static final long serialVersionUID = 9184892508124423115L;
  private Indexer _indexer = null;
  private HashMap<String, Integer> docTermFrequecy = null;
 
  public DocumentIndexed(int docid, Indexer indexer) {
    super(docid);
      _indexer = indexer;
      
  }

}
