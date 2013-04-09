package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews. 
 */
public class RankerComprehensive extends Ranker {

  public RankerComprehensive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
	  Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
      DocumentIndexed doc = null;
      int docid = -1;
      double lambda = 0.5;
      Vector<String> qv = query._tokens;
      while ((doc = (DocumentIndexed)_indexer.nextDoc(query, docid)) != null) {
          //page rank
          double score = 0.0;
          for(int i = 0; i < qv.size(); i++){
              String term = qv.get(i);
              String[] terms = term.split(" ");
              if(terms.length > 1){
                  term = terms[0];
              }
              String id = (new Integer(docid)).toString();
              double termlike = (double)_indexer.corpusTermFrequency(term) / (double)_indexer.totalTermFrequency();
              double doclike = (double)_indexer.documentTermFrequency(term,id) / (double)doc.getDocTotalTermFrequency();
              score += Math.log((1 - lambda)*doclike + lambda*termlike);
          }
          score = Math.pow(Math.E,score);
          
          // combine the score with pagerank value and numview
          score = 0.5 * score + 0.25 * doc.getPageRank() + 0.25 * doc.getNumViews();
          
          ScoredDocument d = new ScoredDocument(doc,score);
          rankQueue.add(d);
          
          docid = doc._docid;
      }
      Vector<ScoredDocument> results = new Vector<ScoredDocument>();
      ScoredDocument sd = null;
      while((sd = rankQueue.poll()) != null){
          results.add(sd);
      }
      Collections.sort(results, Collections.reverseOrder());
      if (results.size() > numResults) results = (Vector<ScoredDocument>) results.subList(0, numResults - 1);
      return results;
  }
}
