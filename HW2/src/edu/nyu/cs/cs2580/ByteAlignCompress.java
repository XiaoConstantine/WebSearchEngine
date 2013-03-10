/*************************************************************************
    > File Name: ByteAlignCompress.java
    > Author: Xiao Cui
    > Mail: xc432@nyu.edu 
    > Created Time: Wed Mar  6 14:46:25 2013
 ************************************************************************/
package edu.nyu.cs.cs2580;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

public class ByteAlignCompress implements Serializable{
  private static final long serialVerisionUID = 1099111905740087931L;  
  private List<Byte> byteList;
  private List<Integer> intList;

  public ByteAlignCompress(){
	byteList = new ArrayList<Byte>();
	intList = new ArrayList<Integer>();
  }
  
  public Byte compressSingle(Integer para){
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
	  //byteList.add(para.byteValue());
	  return para.byteValue();
  }

  public void compressList(List<Integer> postList){
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
  }
  
   public void decompress(List<Byte> byteList){
       for(int i = 0; i < byteList.size(); i++){
           int temp = 0;
           while(byteList.get(i) > 0){
               temp = temp << 7;
               temp += byteList.get(i);
               i++;
           }
           temp = temp << 7;
           temp += byteList.get(i)&127;
           intList.add(temp);
       }
   }
    
    /*public static void main(String args[]){
        ByteAlignCompress b = new ByteAlignCompress();
        List<Integer> postList = new LinkedList<Integer>();
        postList.add(1);
        postList.add(6);
        b.compress(postList);
        for(Byte i: b.byteList){
            System.out.println(Integer.toHexString(i));
        }
        b.decompress(b.byteList);
        for(Integer i: b.intList){
            System.out.println(Integer.toString(i));
        }
    }*/
}
