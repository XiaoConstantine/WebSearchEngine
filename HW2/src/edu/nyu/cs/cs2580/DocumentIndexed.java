package edu.nyu.cs.cs2580;

import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
  private static final long serialVersionUID = 9184892508124423115L;
  
  private Indexer _indexer = null;
 
  public DocumentIndexed(int docid, Indexer indexer) {
    super(docid);
      _indexer = indexer;
  }

  public String asString(){
	  return Integer.toString(_docid) + "\t" + getTitle() + "\t" + getUrl() + "\n";
  }
}
