/*************************************************************************
    > File Name: Writer.java
    > Author: Xiao Cui
    > Mail: xc432@nyu.edu 
    > Created Time: Sun Feb 24 16:44:00 2013
 ************************************************************************/
package edu.nyu.cs.cs2580;

import java.io.*;

class Writer{
    private static String folder = "../results/";
    private static Writer writer = new Writer();
    private Writer(){
        File file = new File(folder);
        if(!file.exists()){
            file.mkdirs();
        }
    }
    public static Writer getInstance(){
        return writer;
    }
    // how to distinguish rank and evaluate?
    // hw-1? hw-2?
    // Add a mark?
    public void writeToFile(String ranker_type, String results, String mark){
        String filename = folder + mark + ranker_type + ".tsv";
        try{
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(filename,true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(results);
            bufferWriter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    
    }
    
    public String readFromFile(String ranker_type, String mark){
        String filename = folder + mark + ranker_type + ".tsv";
        StringBuilder content = new StringBuilder();
        try{
            FileReader file = new FileReader(filename);
            BufferedReader input = new BufferedReader(file);
            String line = null;
            try{
                while((line = input.readLine())!=null){
                    content.append(line);
                    content.append("\n");
                }
            }finally{
                input.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return content.toString();
    }
}
