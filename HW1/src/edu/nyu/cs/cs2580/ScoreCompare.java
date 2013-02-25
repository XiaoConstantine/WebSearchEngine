/*************************************************************************
    > File Name: ScoreCompare.java
    > Author: Xiao Cui
    > Mail: xc432@nyu.edu 
    > Created Time: Sun Feb 24 17:10:40 2013
    > Implement comparator between ScoredDocuments
 ************************************************************************/
package edu.nyu.cs.cs2580;

import java.util.Comparator;

class ScoreCompare implements Comparator < ScoredDocument >{
    public int compare(ScoredDocument s1, ScoredDocument s2){
        return (s1._score < s2._score)?1:-1;
    }
}
