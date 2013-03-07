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
   String filename = "data/wiki/Yahoo!";
   BufferedReader reader = new BufferedReader(new FileReader(filename));
    String noHtmlContent = null;
   try{
        String line = null;
        while((line = reader.readLine()) != null){
            noHtmlContent = line.replaceAll("<[^>]*>", "");
            System.out.println(noHtmlContent);
        }
    }finally{
        reader.close();
    }
  }
}
