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

public class ByteAlignCompress{
  private List<Byte> byteList;
  private List<Integer> intList;

  public ByteAlignCompress(){
	byteList = new List<Byte>();
	intList = new List<Integer>();
  }
  
  public void compress(List<Integer> postList){
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
