/*************************************************************************
 > File Name: QueryHandler.java
 > Author: Xiao Cui
 > Mail: xc432@nyu.edu
 > Modified Time: Mon Feb 25 16:30:00 2013
 ************************************************************************/
package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;
import java.lang.Math;

class QueryHandler implements HttpHandler {
  private static String plainResponse =
      "Request received, but I am not smart enough to echo yet!\n";

  private Ranker _ranker;

  private double sid = 0;
  public QueryHandler(Ranker ranker){
    _ranker = ranker;
  }

  public static Map<String, String> getQueryMap(String query){  
    String[] params = query.split("&");  
    Map<String, String> map = new HashMap<String, String>();  
    for (String param : params){  
      String name = param.split("=")[0];  
      String value = param.split("=")[1];
      map.put(name, value);  
    }
    return map;  
  }
    
   /*public String toHtml(String in){
       StringBuffer out = new StringBuffer();
       for(int i = 0; in!=null&&i<in.length(); i++){
           char c = in.charAt(i);
           if(c == '\t'){
              out.append("&nbsp&nbsp&nbsp&nbsp;");
           }else if(c == '\n'){
               out.append("<br/>;");
           }else{
               out.append(c);
           }
       }
       return out.toString();
   }*/
  
  public void handle(HttpExchange exchange) throws IOException {
    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")){  // GET requests only.
      return;
    }

    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()){
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();
      
    boolean isHtml = false; // check if user want output as html format
    String queryResponse = "";
    int doc_size = _ranker.getDocSize();
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();
    // add a mark for output specification
    String mark = null;
    if ((uriPath != null) && (uriQuery != null)){
      if (uriPath.equals("/search")){
        Map<String,String> query_map = getQueryMap(uriQuery);
        Set<String> keys = query_map.keySet();
        sid = (int)(Math.random()*1000);
        Vector < ScoredDocument > sds = new Vector < ScoredDocument >();
        if (keys.contains("query")){
          if (keys.contains("ranker")){
            String ranker_type = query_map.get("ranker");
            //Vector < ScoredDocument > sds = new Vector < ScoredDocument >();
            // @CS2580: Invoke different ranking functions inside your
            // implementation of the Ranker class.
            
              if (ranker_type.equals("vsm")){
                //call cosine func(vector space model)
                mark = "hw1.1-";
				sds = _ranker.runquery(query_map.get("query"), "vsm");
                  
            } else if (ranker_type.equals("ql")){
               // call QL func
                mark = "hw1.1-";
                sds = _ranker.runquery(query_map.get("query"), "ql");
            } else if (ranker_type.equals("phrase")){
              //call phrase func
               mark = "hw1.1-";
			   sds = _ranker.runquery(query_map.get("query"), "phrase");
            }else if (ranker_type.equals("numviews")){
               mark = "hw1.1-";
                sds = _ranker.runquery(query_map.get("query"), "numviews");
            }else if (ranker_type.equals("linear")){
              // call linear
                mark = "hw1.2-";
			    sds = _ranker.runquery(query_map.get("query"), "linear");
            } else {
              queryResponse = (ranker_type+" not implemented.");
            }
              
              if(keys.contains("format")){
                  if(query_map.get("format").equals("html")){
                      isHtml = true;
                  }
              }
              Iterator < ScoredDocument > itr = sds.iterator();
              while(itr.hasNext()){
                  ScoredDocument sd = itr.next();
                  if(queryResponse.length() > 0){
                      queryResponse = queryResponse + "\n";
                  }
                  queryResponse = queryResponse + query_map.get("query") + "\t" + sd.asString()+"\t" + "render" +"\t" + sid;
                  String result = query_map.get("query") + "\t" + sd.asString()+"\n";
                  /*if(isHtml){
                      queryResponse += toHtml(query_map.get("query")) + toHtml(sd.asString());
                  }*/
                  Writer.getInstance().writeToFile(ranker_type, result, mark);
              }
              if(queryResponse.length() > 0 ){
                  queryResponse = queryResponse + "\n";
              }

          } else {
            // @CS2580: The following is instructor's simple ranker that does not
            // use the Ranker class.
            //Vector < ScoredDocument > sds = _ranker.runquery(query_map.get("query"), "vsm");
            Iterator < ScoredDocument > itr = sds.iterator();
            while (itr.hasNext()){
              ScoredDocument sd = itr.next();
              if (queryResponse.length() > 0){
                queryResponse = queryResponse + "\n";
              }
              queryResponse = queryResponse + query_map.get("query") + "\t" + sd.asString();
            }
            if (queryResponse.length() > 0){
              queryResponse = queryResponse + "\n";
            }
          }
        }
      }else if(uriPath.equals("/click")){
          Map<String, String> query_map = getQueryMap(uriQuery);
          Set<String> keys = query_map.keySet();
          mark = "hw1.4-";
          String sid = null;
          String did = null;
          String action = "render";
          String query = null;
          if(keys.contains("sid")){
              sid = query_map.get("sid");
          }
          if(keys.contains("did")){
              did = query_map.get("did");
          }
          if(keys.contains("action")){
              action = query_map.get("action");
          }
          if(keys.contains("query")){
              query = query_map.get("query");
          }
          Date time = new Date();
          String result = sid + "\t" + query + "\t" + did + "\t" + action + "\t" + time + "\n";
          Writer.getInstance().writeToFile("log",result, mark);
          for(int i = 0; i <doc_size; i++){
              int id = Integer.parseInt(did);
              if(i!=id){
                  String result_norm = sid + "\t" + query + "\t" + i + "\t" + "render" + "\t" + time + "\n";
                  Writer.getInstance().writeToFile("log",result_norm,mark);
              }
          }

      }
    }
    
      // Construct a simple response.
      Headers responseHeaders = exchange.getResponseHeaders();
      if(isHtml){
          responseHeaders.set("Content-Type", "text/html");
      }else{
          responseHeaders.set("Content-Type", "text/plain");
      }
      exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
      OutputStream responseBody = exchange.getResponseBody();
      responseBody.write(queryResponse.getBytes());
      responseBody.close();
  }
}
