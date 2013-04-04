package edu.nyu.cs.cs2580;

import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Vector;
/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
   
/*    public Vector<String[]> phrases = new Vector<String[]>();
    public Vector<String> nonPhrase = new Vector<String>();

	public Vector<String> getNonPhrase(){
		return nonPhrase;
	}

    public Vector<String[]> getPhrases(){
		return phrases;
	}
*/
	public QueryPhrase(String query) {
		super(query);
	}

	/*public int getPhraseCount(){
		return phraseCount;
	}*/
    @Override
    public void processQuery() {
        
        //Find phrase first
        String phrase = "\"(.*?)\"";
        Pattern phrasePattern = Pattern.compile(phrase);
        Matcher phraseMatcher = phrasePattern.matcher(_query);
        
        
        while(phraseMatcher.find()){
            _tokens.add(phraseMatcher.group(1));
        }
        _query = _query.replaceAll(phrase, " ");
        
        //add the rest terms to the query vector
        Scanner s = new Scanner(_query);
        while (s.hasNext()) {
            _tokens.add(s.next());
        }
        s.close();
    }
    
    
    public static void main(String[] args){
        String query = "\"new york\" \"york time\" NYU";
        QueryPhrase test = new QueryPhrase(query);
        test.processQuery();
        
        for(String t : test._tokens){
            System.out.println(t);
        }
    }
}