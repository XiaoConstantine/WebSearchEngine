Final Project

1. Run Java files in Preprocess folder by java Rename(Compile first). Preprocess does the following job:
   a. Remove wiki files html tag.
   b. Remove Stop Words
   c. Stem words
   d. Merge small files to fit hadoop chunk size (64mb)

Upload wiki_new to cluster for test

2. Run Java files in Mapreduce folder by javac -cp hadoop-core-1.0.4:lib/common-cli-1.0.4 *.java. 
   Compress the class files into jar
   Upload to Hadoop cluster(AWS EC2 whatever)
   Upload corpus after preprocess

3. You can just upload the Index.jar to hadoop cluster to skip step 2.

 hadoop jar Index.jar IndexFlushVersion input(wiki_use) output

4. Check output
   Should has part-r-00000 to part-r-00005 represents a.idx, f.idx, k.idx, p.idx, u.idx, num.idx
   rename output to a-num.idx and copy to local index folder

5. Run like HW3