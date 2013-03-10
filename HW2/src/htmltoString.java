/*************************************************************************
    > File Name: htmltoString.java
    > Author: Xiao Cui
    > Mail: xc432@nyu.edu 
    > Created Time: Tue Mar  5 23:38:49 2013
 ************************************************************************/
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

class htmltoString{
   
 public static void main(String args[]) throws IOException{
   String filename = "/Users/Constantine/WSE_Git/HW2/data/wiki/Yahoo!";
   BufferedReader reader = new BufferedReader(new FileReader(filename));
   String noHtmlContent = null;
   String noScriptContent = null;
   boolean flag = false;
   try{
       int scriptFlag = 0; // 0 means no script, 1 means <script>, 2 means <script ...>
       String line = null;
       
       while ((line = reader.readLine()) != null) {
           // remove the content in <script>
           while (line.contains("<script") || line.contains("</script>")) {
               if (scriptFlag == 2) { // in <script ...
                   if (line.contains(">")) {
                       line = line.substring(line.indexOf(">") + 1);
                       scriptFlag = 1;
                   }
               }
               if (scriptFlag == 1) { // in <script> ...
                   if (line.contains("</script>")) {
                       line = line.substring(line.indexOf("</script>") + 9);
                       scriptFlag = 0;
                   }
               }
               if (scriptFlag == 0) {
                   if (line.contains("<script>")) {
                       // parse the no script content and check the remain string
                      // phraseFile(docid, line.substring(0, line.indexOf("<script>")), bodyTokens);
                       scriptFlag = 1;
                       line = line.substring(line.indexOf("<script>") + 8);
                   } else if (line.contains("<script")) {
                       // parse the no script content and check the remain string
                       //phraseFile(docid,line.substring(0, line.indexOf("<script")),bodyTokens);
                       scriptFlag = 2;
                       line = line.substring(line.indexOf("<script") + 7);
                       if (line.contains(">")) {
                           line = line.substring(line.indexOf(">") + 1);
                           scriptFlag = 1;
                       }
                   } 
               }
           }
           if (scriptFlag != 0) continue;
           // parse the content, add them into dictionary
           /* Below lines are also processing html, why add them
            * into parseFile function?
            * */
           
           line = line.replaceAll("<[^>]*>", " ");
           line = line.replaceAll("\\pP|\\pS|\\pC", " ");
           System.out.println(line);
        }
    }finally{
        reader.close();
    }
  }
}
