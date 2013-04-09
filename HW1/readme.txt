First, cd the g01/src folder and compile all the .java file with the following command:
	javac edu/nyu/cs/cs2580 *.java

Second, run SearchEngine with the following command:
	java edu.nyu.cs.cs2580.SearchEngine 25801 /home/congyu/cs2580/hw1/g01/data/corpus.tsv

Ranker Part:
        run Ranker with the following command:
        curl "localhost:25801/search?query=<query>&ranker=<ranker-type>&format=text"
        results stored in /results/hw1.1-ranker_type.tsv
Note: <Ranker-Type> cosine is vsm(short for vector space model) in our homework. 
      <Ranker-Type> QL is ql in our homework.

Evaluator Part:
        run Evaluator with the following command:
	curl "localhost:25801/search?query=<QUERY>&ranker=<RANKER-TYPE>&format=text" | \
	java edu.nyu.cs.cs2580.Evaluator /home/congyu/cs2580/hw1/g01/data/qrels.tsv >>../results/hw1.3-<RANKER-TYPE>.tsv
   where <QUERY> is the query evaluating now, <RANKER-TYPE> is the ranker type using.


For Bonus Part:
     After each seach query there will be a unique session id in the stdout on the screen
Then type:
     curl "localhost:25801/click?sid = <sessionid>&did=<doc_id u want>&query&format=text"
Result will store at /results/log-4.tsv