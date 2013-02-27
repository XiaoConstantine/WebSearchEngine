First, cd the g01/src folder and compile all the .java file with the following command:
	javac edu/nyu/cs/cs2580 *.java

Second, run SearchEngine with the following command:
	java edu.nyu.cs.cs2580.SearchEngine 25801 /home/congyu/cs2580/hw1/g01/data/corpus.tsv

Third, run Evaluator with the following command:
	curl "http://localhost:25801/search?query=<QUERY>&ranker=<RANKER-TYPE>&format=text" | \
	java edu.nyu.cs.cs2580.Evaluator /home/congyu/cs2580/hw1/g01/data/qrels.tsv >>../results/hw1.3-<RANKER-TYPE>.tsv
   where <QUERY> is the query evaluating now, <RANKER-TYPE> is the ranker type using.