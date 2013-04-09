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
<<<<<<< HEAD
    
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
=======
   
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
>>>>>>> ceb115cf40c5f56a7c1f9b0259f5dbec57b6d97a
	@Override
	public void processQuery() {
		if (_query == null)
			return;
<<<<<<< HEAD
        
	    Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher match = p.matcher(_query);
		
        while(match.find()){
            _tokens.add(match.group(1));
            //phraseCount++;
        }
        
        _query = _query.replace("\"(.*?)\"", " ");
        
        Scanner s = new Scanner(_query);
        
        while(s.hasNext()){
            _tokens.add(s.next());
        }
        s.close();
	}
    
	/*
     public void processQuery(){
     HashSet<String> set = new HashSet<String>();
     
     if (_query == null)
     return;
     System.out.println("Query is:" + _query);
     _query = _query.replaceAll("%20", " ");
     _query = _query.replaceAll("%22", "\"");
     String[] tokens = _query.split("[ \"]");
     for(String token: tokens){
     if(!token.equals("")){
     _tokens.add(token);
     }
     }
     tokens = _query.split("[\"]");
     for(int i = 1; i< tokens.length; i=i+2){
     String[] phrase = tokens[i].split("[ ]");
     phrases.add(phrase);
     }
     
     for(int i = 0; i < phrases.size(); i++){
     System.out.println("phrase:");
     for(int j = 0; j < phrases.get(i).length; j++){
     System.out.println(phrases.get(i)[j] + " ");
     set.add(phrases.get(i)[j]);
     }
     }
     
     for(int i = 0; i < _tokens.size(); i++){
     System.out.println(_tokens.get(i) + " ");
     if(!set.contains(_tokens.get(i))){
     nonPhrase.add(_tokens.get(i));
     }
     }
     System.out.println();
     }*/
    
    /* public static void main(String args[]){
     String query = "[\"new%20york%20times\"][\"micro%20soft\"]%20film";
     QueryPhrase test = new QueryPhrase(query);
     test.processQuery();
     
     for(int i = 0; i < test._tokens.size(); i++){
     System.out.println(test._tokens.get(i));
     }
     //System.out.println(test.getPhraseCount());
     /*	for(String t: test._tokens){
     System.out.println(t);
     }
     
     */
    /*
     for(int i = 0; i < test.startIndex.size(); i++){
     System.out.println(test._tokens.get(i));
     }
     for(int i = 0; i < test.endIndex.size(); i++){
     System.out.println(test._tokens.get(i));
     }
     
     System.out.println(test.endIndex.size());*/

=======

	    Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher match = p.matcher(_query);
		
       while(match.find()){
		   _tokens.add(match.group(1));
		   //phraseCount++;
	   }

	   _query = _query.replace("\"(.*?)\"", " ");

       Scanner s = new Scanner(_query);

	   while(s.hasNext()){
		   _tokens.add(s.next());
	   }
	   s.close();
	}

	/*
	public void processQuery(){
    	HashSet<String> set = new HashSet<String>();

		if (_query == null)
			return;
		System.out.println("Query is:" + _query);
		_query = _query.replaceAll("%20", " ");
		_query = _query.replaceAll("%22", "\"");
		String[] tokens = _query.split("[ \"]");
		for(String token: tokens){
			if(!token.equals("")){
				_tokens.add(token);
			}
		}
		tokens = _query.split("[\"]");
		for(int i = 1; i< tokens.length; i=i+2){
			String[] phrase = tokens[i].split("[ ]");
			phrases.add(phrase);
	    }

		for(int i = 0; i < phrases.size(); i++){
			System.out.println("phrase:");
			for(int j = 0; j < phrases.get(i).length; j++){
				System.out.println(phrases.get(i)[j] + " ");
			    set.add(phrases.get(i)[j]);
			}
		}

		for(int i = 0; i < _tokens.size(); i++){
			System.out.println(_tokens.get(i) + " ");
			if(!set.contains(_tokens.get(i))){
				nonPhrase.add(_tokens.get(i));
			}
		}
		System.out.println();
	}*/

   /* public static void main(String args[]){
		String query = "[\"new%20york%20times\"][\"micro%20soft\"]%20film";
		QueryPhrase test = new QueryPhrase(query);
		test.processQuery();

		for(int i = 0; i < test._tokens.size(); i++){
			System.out.println(test._tokens.get(i));
		}
		//System.out.println(test.getPhraseCount());
    /*	for(String t: test._tokens){
			System.out.println(t);
		}

		*/
/*
		for(int i = 0; i < test.startIndex.size(); i++){
			System.out.println(test._tokens.get(i));
		}
     	for(int i = 0; i < test.endIndex.size(); i++){
			System.out.println(test._tokens.get(i));
		}
		
		System.out.println(test.endIndex.size());*/
//	}
>>>>>>> ceb115cf40c5f56a7c1f9b0259f5dbec57b6d97a

}
