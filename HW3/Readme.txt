Homework 3

1. For 2.1.1, we choose lambda = 0.9 and two iterations as the setting for computing PageRank values.
Because lambda is the chance that an user click the link in current page, thus 0.9 is more reasonable than 0.1; Since the result of PageRank algorithm will converge with the increase of iteration times, two iterations would be able to produce better results for search queries.

2. For 3.1, you should type: "java -cp src edu.nyu.cs.cs2580.Spearman data/index/pagerank data/index/numView".
The Spearman correlation coefficient is 0.48231150757490404.

3. For 4.1, the expanded queries are stored in "data/index/prf/" directory, named "xxx.tsv" where xxx is each query.

4. For 4.2, you should type: "java -cp src edu.nyu.cs.cs2580.Bhattacharyya data/index/prf data/index/qsim.tsv". 
The first argument is the path to the prf results directory, the program will read all the prf result files in the dirctory and compute the result then output to "data/index/qsim.tsv".
