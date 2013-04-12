Homework 3

1. For 2.1.1, we choose lambda = 0.9 and two iterations as the setting for computing PageRank values.
Because lambda is the chance that an user click the link in current page, thus 0.9 is more reasonable than 0.1; Since the result of PageRank algorithm will converge with the increase of iteration times, two iterations would be able to produce better results for search queries.

2. For 3.1, you should type: "java -cp src edu.nyu.cs.cs2580.Spearman data/index/pagerank data/index/numView".
The Spearman correlation coefficient is 0.48231150757490404.

3. For 4.1, the expanded queries will just show on the screen.
To remove the stop words, I set the percentage to be 0.005.

4. For 4.2, our project supports the script mentioned in HW3.pdf with a little bit modification shown below: 
rm -f prf*.tsv
i=0
while read q;do i=$((i+1)); 
prfout=prf-$i.tsv;
Q=`echo $q| sed "s/ /%20/g"`; 
t="http://localhost:25801/prf?query=$Q&ranker=comprehensive&numdocs=10&numterms=5";
curl $t > $prfout; 
echo $q:$prfout >> prf.tsv; 
done < queries.tsv
java -cp src edu.nyu.cs.cs2580.Bhattacharyya prf.tsv qsim.tsv

you can run in the HW3/ directory.

