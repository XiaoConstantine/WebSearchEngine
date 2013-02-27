package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.lang.Math;

class Evaluator {
    public static void main(String[] args) throws IOException {
        HashMap <String, HashMap<Integer,Double>> relevance_judgments = 
           new HashMap <String, HashMap<Integer,Double>>();
        if(args.length < 1){
            System.out.println("need to provide relevance_judgments");
            return;
        }
        String p = args[0];
        String results = null;
        // first read the relevance judgments into the HashMap
        readRelevanceJudgments(p,relevance_judgments);
        // now evaluate the results from stdin
        evaluateStdin(relevance_judgments);
    }

    public static void readRelevanceJudgments(String p,
                                              HashMap<String,HashMap<Integer,Double>> 
                                              relevance_judgments){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(p));
            try {
                String line = null;
                while ((line = reader.readLine()) != null){
                    // parse the query,did,relevance line
                    Scanner s = new Scanner(line).useDelimiter("\t");
                    String query = s.next();
                    int did = Integer.parseInt(s.next());
                    String grade = s.next();
                    double rel = 0.0;
                    // five grades.
                    if(grade.equals("Perfect")){  
                        rel = 10.0;
                    }else if(grade.equals("Excellent")){  
                        rel = 7.0; 
                    }else if(grade.equals("Good")){ 
                        rel = 5.0;
                    }else if(grade.equals("Fair")){ 
                        rel = 3.0;
                    }else if(grade.equals("Bad")){
                        rel = 0.0;
                    }
                    if(relevance_judgments.containsKey(query) == false){
                        HashMap<Integer,Double> qr = new HashMap<Integer,Double>();
                        relevance_judgments.put(query,qr);
                    }
                    HashMap<Integer,Double> qr = relevance_judgments.get(query);
                    qr.put(did,rel);
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe){
            System.err.println("Oops " + ioe.getMessage());
        }
    }
    
    public static void evaluateStdin(HashMap<String,HashMap<Integer,Double>>
                                     relevance_judgments){
        // only consider one query per call 
        StringBuffer results = new StringBuffer();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String in= null;
            StringBuffer input = new StringBuffer();
            boolean q = false;
            String query = null;
            while((in = reader.readLine()) != null){
                if(!q){
                    Scanner s = new Scanner(in).useDelimiter("\t");
                    query = s.next();
                    q = true;
                }
                input.append(in);
                input.append("\n");
            }
            Writer.getInstance().writeToFile("middleresult",input.toString(),"hw1.3-");
            try {
                results.append(query+"\t");
                DecimalFormat df = new DecimalFormat("0.00");
                results.append(df.format(evaluate_Precision(1,relevance_judgments))+"\t");
                results.append(df.format(evaluate_Precision(5,relevance_judgments))+"\t");
                results.append(df.format(evaluate_Precision(10,relevance_judgments))+"\t");
                results.append(df.format(evaluate_Recall(1,relevance_judgments))+"\t");
                results.append(df.format(evaluate_Recall(5,relevance_judgments))+"\t");
                results.append(df.format(evaluate_Recall(10,relevance_judgments))+"\t");
                results.append(df.format(evaluate_FMeasure(1,0.5,relevance_judgments))+"\t");
                results.append(df.format(evaluate_FMeasure(5,0.5,relevance_judgments))+"\t");
                results.append(df.format(evaluate_FMeasure(10,0.5,relevance_judgments))+"\t");
                HashMap<Double,Double> ss = evaluate_PrecisionRecallGraph(relevance_judgments);
                for(double i = 0.0; i <= 1.0; i = i + 0.1){
                    double t = ss.get(i);
                    results.append(df.format(t)+"\t");
                }
                results.append(df.format(evaluate_AveragePrecision(relevance_judgments))+"\t");
                results.append(df.format(evaluate_NDCG(1,relevance_judgments))+"\t");
                results.append(df.format(evaluate_NDCG(5,relevance_judgments))+"\t");
                results.append(df.format(evaluate_NDCG(10,relevance_judgments))+"\t");
                results.append(df.format(evaluate_ReciprocalRank(relevance_judgments))+"\t");
                System.out.println(results);
            }catch (Exception e){ 
                System.err.println("Error:" + e.getMessage());
            }finally{
                reader.close();
                File file = new File("../results/hw1.3-middleresult.tsv");
                if(file.isFile()){
                    file.delete();
                }
            }
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public static double evaluate_Precision(Integer K,HashMap<String,HashMap<Integer,Double>> 
                                            relevance_judgments){
        double result = 0.0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
            String line = null;
            int RR = 0;
            for (int i = 0; i < K ; i++){
                line = reader.readLine();
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) != false){
                    RR++;
                }
            }
            if(K != 0){
                result = (double)RR/K;
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    public static double evaluate_Recall(Integer K,HashMap<String,HashMap<Integer,Double>> 
                                         relevance_judgments){
        double result = 0.0;
        try{
            BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
            double RR = 0.0;
            int R = 0; 
            String line = null;
            for(int i = 0; i < K; i++){
                line = reader.readLine();
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                Set <Integer> keys = qr.keySet();
                R = 0;
                for(Integer key: keys){
                    if (qr.get(key)>0.0){
                        R++;
                    }
                }
                if(qr.containsKey(did) != false){
                    RR++;
                }
            }
            if(R != 0){
                result = (double)RR/R;
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    public static double evaluate_FMeasure(Integer K,double lambda,
                                           HashMap<String,HashMap<Integer,Double>> 
                                           relevance_judgments){
        double result = 0.0;
        try{
            double P =  evaluate_Precision(K,relevance_judgments);
            double R = evaluate_Recall(K,relevance_judgments);
            if((P != 0.0) && (R != 0.0)){
                result = 1/(lambda*(1/P)+(1-lambda)*(1/R));
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    public static HashMap<Double,Double> evaluate_PrecisionRecallGraph(HashMap<String,HashMap<Integer,Double>>
                                                       relevance_judgments){
        HashMap<Double,Double> PR=new HashMap<Double,Double>();
        HashMap<Double,Double> result =new HashMap<Double,Double>();
        result.put(0.0, 1.0);
            try {
                String line = null;
                int RR = 0;
                int R=0;
                int K=1;
                double recall;
                double precision;
                BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
                
                while((line=reader.readLine())!=null){
                    Scanner s = new Scanner(line).useDelimiter("\t");
                    String query = s.next();
                    int did = Integer.parseInt(s.next());
                    String title = s.next();
                    double rel = Double.parseDouble(s.next());
                    if (relevance_judgments.containsKey(query) == false){
                        throw new IOException("query not found");
                    }
                    HashMap < Integer , Double > qr = relevance_judgments.get(query);
                    Set<Integer> keys=qr.keySet();
                    R=0;
                    for(int key:keys){
                        if(qr.get(key)>0)
                            R++;
                    }
                    if (qr.containsKey(did) != false){
                        RR++;
                    }
                    recall=(double)RR/R;
                    precision=(double)RR/K;
                    if(!PR.containsKey(recall)){
                        PR.put(recall, precision);
                        if(recall==1.0){
                            break;
                        }
                    } 	
                    K++;
                }
                Set<Double> keys=PR.keySet();
                for(double j=0.1;j<=1.0;j+=0.1){
                    double max=0.0;
                    for(double key:keys){
                        if((key>=j)&&(PR.get(key)>max)){
                            max=PR.get(key);
                        }	
                    }
                    result.put(j,max);
                }
            }catch(Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        return result;
    }
    
    public static double evaluate_AveragePrecision(HashMap<String,HashMap<Integer,Double>>
                                                   relevance_judgments){
        double result = 0.0;
        try{
            double AP = 0.0;
            double RR = 0.0;
            String line = null;
            int i = 0;
            BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
            while((line = reader.readLine()) != null){
                i++;
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) != false){
                    RR++;
                    AP = AP + RR/i;
                }
            }
            if(RR != 0.0){
                result = (double)AP/RR;
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    public static double evaluate_NDCG(int K, HashMap<String,HashMap<Integer,Double>> 
                                       relevance_judgments){
        double result = 0.0;
        try{
            String line = null;
            double DCG = 0.0;
            double IDCG = 0.0;
            double []ss = new double[K];
            int i = 0;
            BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
            while((i < K)&&((line=reader.readLine())!=null)){
                i++;
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) != false){
                    ss[i-1] = qr.get(did);
                    DCG += ss[i-1]/((Math.log(i+1))/(Math.log(2)));
                }
            }
            Arrays.sort(ss);
            for(int m = ss.length-1; m >= 0; m--){
                IDCG += ss[m]/((Math.log(ss.length-m+1))/(Math.log(2)));
            }
            if(IDCG != 0.0){
                result = DCG/IDCG;
            }
            
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    public static double evaluate_ReciprocalRank(HashMap<String,HashMap<Integer,Double>> 
                                                 relevance_judgments){
        double result = 0.0;
        try{
            String line = null;
            int i = 0;
            BufferedReader reader = new BufferedReader(new FileReader("../results/hw1.3-middleresult.tsv"));
            while((line = reader.readLine()) != null){
                i++;
                Scanner s = new Scanner(line).useDelimiter("\t");
                String query = s.next();
                int did = Integer.parseInt(s.next());
                String title = s.next();
                double rel = Double.parseDouble(s.next());
                if(relevance_judgments.containsKey(query) == false){
                    throw new IOException("query not found");
                }
                HashMap<Integer,Double> qr = relevance_judgments.get(query);
                if(qr.containsKey(did) != false){
                    return 1/i;
                }
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return 0;
    }
                  
                  
        
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
