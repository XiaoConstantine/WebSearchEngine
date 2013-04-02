/*************************************************************************
    > File Name: ByteAlignCompress.java
    > Author: Xiao Cui
    > Mail: xc432@nyu.edu 
    > Created Time: Wed Mar  6 14:46:25 2013
 ************************************************************************/
package edu.nyu.cs.cs2580;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;
import java.lang.Math;
import java.io.IOException;
public class ByteAlignCompress implements Serializable{
  private static final long serialVerisionUID = 1099111905740087931L;  
  //private ArrayList<Byte> byteList;
  //private List<Integer> intList;

   public ByteAlignCompress(){
//	byteList = new ArrayList<Byte>();
//	intList = new ArrayList<Integer>();
   }
  
  public ArrayList<Byte> compressSingle(int para){
      ArrayList<Byte> byteList = new ArrayList<Byte>();   
	  /*if(para >= 1<<21){
          byteList.add((byte)(para>>21));
	  }
	  para&=(1<<21)-1;

	  if(para>=1<<14){
		  byteList.add((byte)(para>>14));
	  }
      para&=(1<<14)-1;

	  if(para>=1<<7){
		  byteList.add((byte)(para>>7));
	  }
	  para&=(1<<7)-1;

	  para|=128;
	  byte parab = (byte)(para);
	  byteList.add(parab);
	  return byteList;
    */
	  boolean first = true;
	  if(para == 0){
		  byteList.add((byte)(1<<7));
	  }
	  while(para > 0 ){
		  byte parab = (byte)(para % 128);
          para = para >> 7;
		  if(first){
			  parab |= 1 << 7;
			  first = false;
		  }
		  byteList.add(parab);
	  }
	  Collections.reverse(byteList);
	  return byteList;
  
  }

 /* public void compressList(List<Integer> postList){
      for(Integer para: postList){
      if(para >= 1<<21){
          byteList.add((byte)(para>>21));
	  }
	  para&=(1<<21)-1;

	  if(para>=1<<14){
		  byteList.add((byte)(para>>14));
	  }
      para&=(1<<14)-1;

	  if(para>=1<<7){
		  byteList.add((byte)(para>>7));
	  }
	  para&=(1<<7)-1;

	  para|=128;
	  byteList.add(para.byteValue());
  
     }
  }*/
  
   public int decompress(ArrayList<Byte> byteList){
       if(byteList == null)
		   return -1;
	   
	   int temp = 0;
	   /*for(int i = 0; i < byteList.size(); i++){
           //int temp = 0;
           while(byteList.get(i) > 0){
               temp = temp << 7;
               temp += byteList.get(i);
               i++;
           }
           temp = temp << 7;
           temp += byteList.get(i)&127;
          // intList.add(temp);
       }*/
		temp += (int)(byteList.get(0)&((1<<7)-1));
		for(int i = 1; i<byteList.size();i++){
			temp += byteList.get(i)*((int)Math.pow(128,i));
		}
	   return temp;
   }
 
   public ArrayList<Integer> decompressList(ArrayList<Byte> list){
	   if(list == null )
		   return null;
	   ArrayList<Integer> result = new ArrayList<Integer>();
	   ArrayList<Byte> current = new ArrayList<Byte>();

	   for(int i = 0; i < list.size(); i++){
		   current.add(list.get(i));
		   if((list.get(i)&(1<<7)) > 0){
			   int temp = decompress(current);
			   result.add(temp);
			   current.clear();
		   }
	   }
	   return result;
   }
   public int decompressID(ArrayList<Byte> byteList){
	   if(byteList.size() == 0)
		   System.out.println("No doc id inside");
	   ArrayList<Byte> did = new ArrayList<Byte>();
	   did.add(byteList.get(0));
	   for(int i = 1; i < byteList.size(); i++){
		   if((byteList.get(i)&(1<<7)) > 0)
			   break;
		   did.add(byteList.get(i));
	   }
	   return decompress(did);
   }

   public ArrayList<Integer> decompressTermIDs(ArrayList<ArrayList<Byte>> infoindex) throws IOException{
	   ArrayList<Integer> ids = new ArrayList<Integer>();
	   for(ArrayList<Byte> list: infoindex){
		   int docid;
		   docid = decompressID(list);
		  ids.add(docid);
	   }
	   return ids;
	   
   }

 /*   public static void main(String args[]){
        ByteAlignCompress b = new ByteAlignCompress();
        List<Integer> postList = new LinkedList<Integer>();
        postList.add(1);
        postList.add(6);
        ArrayList<Byte> by =  b.compressSingle(1);
        for(Byte i:  by){
            System.out.println(Integer.toHexString(i));
        }
        /*b.decompress(b.byteList);
        for(Integer i: b.intList){
            System.out.println(Integer.toString(i));
        }
    }*/
}
