package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
	// Store the start indices of a phrase in _tokens
	public ArrayList<Integer> startIndex = new ArrayList<Integer>();
	// Store the end indices of a phrase in _tokens
	public ArrayList<Integer> endIndex = new ArrayList<Integer>();
	
	public QueryPhrase(String query) {
		super(query);
	}

	@Override
	public void processQuery() {
		if (_query == null)
			return;

		Scanner s = new Scanner(_query);
		int i = 0;
		while (s.hasNext()) {
			String result = s.next();
			if (result.startsWith("\"")) {
				startIndex.add(i);
				result = result.substring(1); // remove the quote
			} 
			if (result.endsWith("\"")) {
				endIndex.add(i);
				result = result.substring(0, result.length() - 1); // remove the quote
			}
			_tokens.add(result);
			i++;
		}
		s.close();
	}

}
