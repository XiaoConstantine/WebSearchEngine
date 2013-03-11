package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {
    public RankerFavorite(Options options,
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
                String id = (new Integer(docid)).toString();
                double termlike = (double)_indexer.corpusTermFrequency(term) / (double)_indexer.totalTermFrequency();
                double doclike = (double)doc.getDocTermFrenquency().get(term) / (double)doc.getDocTotalTermFrequency();
                score += Math.log((1 - lambda)*doclike + lambda*termlike);
            }
            score = Math.pow(Math.E,score);
            ScoredDocument d = new ScoredDocument(doc,score);
            rankQueue.add(d);
            if(rankQueue.size() > numResults){
                rankQueue.poll();
            }
            docid = doc._docid;
        }
        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        ScoredDocument sd = null;
        while((sd = rankQueue.poll()) != null){
            results.add(sd);
        }
        Collections.sort(results, Collections.reverseOrder());
        return results;
    }
}
