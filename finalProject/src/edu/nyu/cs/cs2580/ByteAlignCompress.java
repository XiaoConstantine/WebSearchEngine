package edu.nyu.cs.cs2580;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ByteAlignCompress {
    /*
     ByteAlignCompress(n)
     1 bytes = <>
     2 while true
     3 do PREPEND(bytes, n mod 128)
     4   if n < 128
     5   then BREAK
     6   n = n div 128
     7 bytes[LENGTH(bytes)] += 128
     8 return bytes
     */    
    
    public static List<Byte> ByteAlignCompress(int n) {
        List<Byte> bytes = new ArrayList<Byte>();
        bytes.add(0,(byte)(n%128));
        n = n / 128;
        while (n>0) 
        {
        	byte tmp = (byte)(n%128);
            bytes.add(0,(byte)(tmp | 0x80));//This is done like that because ,Java doesn't have unsigned byte.
            n = n / 128;
        }                                                 
        return bytes;
    }
    
    /*
     ENCODE(numbers)
     1 bytestream = <>
     2 for each n in numbers
     3 do bytes = ByteAlignCompress(n)
     4       bytestream = EXTEND(bytestream, bytes)
     5 return bytestream   
     */
        
    public static byte[] ENCODE(int number){
        List<Byte> bytes = ByteAlignCompress(number);           
        //Convert result to byte[], then return. 
        byte[] bytestream = new byte[bytes.size()];
        for (int i=0; i<bytes.size(); i++){
           bytestream[i] = bytes.get(i);  
        }
        return bytestream;
    }
    

    public static byte[] ENCODE(int[] numbers) {
        List<Byte> bytestream_l = new ArrayList<Byte>();
        
        for (Integer n : numbers) {
            List<Byte> bytes = ByteAlignCompress(n);
            bytestream_l.addAll(bytes);
        }
        
        //Convert result to byte[], then return. 
        byte[] bytestream = new byte[bytestream_l.size()];
        for (int i=0; i<bytestream_l.size(); i++) {
            bytestream[i] = bytestream_l.get(i); 
        }
        return bytestream;
    }
    
    /*    
     DECODE(bytestream)
     1 numbers = <>
     2 n = 0
     3 for i = 1 to LENGTH(bytestream)
     4 do if bytestream[i] < 128
     5       then n = 128*n + bytestream[i]
     6       else n = 128*n + (bytestream[i] - 128)
     7               APPEND(numbers, n)
     8               n = 0
     9 return numbers  
     */    
    
    public static List<Integer> DECODE(List<Byte> bytestream) {
        List<Integer> numbers = new ArrayList<Integer>();
        int n = 0;
        for (int i=0; i<bytestream.size(); i++) {
            if ( (bytestream.get(i) & (byte)(0x80)) == 0 ){
                n = 128*n + bytestream.get(i);
                numbers.add(n);
                n = 0;
            }
            else {
                byte b = (byte)(bytestream.get(i) & 0x7F); //Achieves the effect of -= 128. 
                n = 128*n + b;
            }
        }
        
        return numbers;
    }
    
    public static List<Integer> DECODE(byte[] bytestream) {
        List<Integer> numbers = new ArrayList<Integer>();
        int off = 0;
        int len = bytestream.length;
        int n = 0;
        for (int i=off; i<(off+len); i++) {
            if ( (bytestream[i] & (byte)(0x80)) == 0 ){
                n = 128*n + bytestream[i];
                numbers.add(n);
                n = 0;
            }
            else {
                byte b = (byte)(bytestream[i] & 0x7F); //Achieves the effect of -= 128. 
                n = 128*n + b;
            }
        }
        
        return numbers;
    }
    
    
    public static int firstDocIdOfChunk(byte[] bytestream, int chunkNum, int chunkID) {//return the first value of the chunk
        int n = 0;
        int offset = chunkNum;//offset of docId is equal to  chunk number of this inverted list
        for(int i=0; i< chunkID; i++) {
            offset += (int)bytestream[i] & 0xff;
        }
        for (int i=offset; ; i++) {
            if ( (bytestream[i] & (byte)(0x80)) == 0 ){
                n = 128*n + bytestream[i];
                return n;
            }
            else {
                byte b = (byte)(bytestream[i] & 0x7F); //Achieves the effect of -= 128. 
                n = 128*n + b;
            }
        }
    }
    
    public static byte[] getNextChunk(FileInputStream fis){
        Vector<Byte> currentChunk = new Vector<Byte>();
        int current;
        try {
			while((current = fis.read())!=-1){
                currentChunk.add((byte)current);
                if((current & (byte)(0x80)) == 0){
                    //the current byte is the ending byte
                    break;
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
        byte[] bytestream = new byte[currentChunk.size()];
        for (int i = 0; i < currentChunk.size(); i++) {
            bytestream[i] = currentChunk.get(i); 
        }
      //  System.out.println(currentChunk.size() + "**" + currentChunk);
		return bytestream;
    }
    
    
    public static void main(String[] args) throws Exception {
        //Testing
        
        //**************For Integer******************
        //ENCODE
//        int number[] = {99999999,6,127,128,130,20000};
//        byte[] res = ByteAlignCompress.ENCODE(number);
//        for(byte b: res){
//            System.out.print(b + " ");
//        }
//        System.out.println();
//        
//        //DECODE
//        List<Integer> resR = ByteAlignCompress.DECODE(res);
//        System.out.println(resR);
//        
//        //WRITE TO FILE
//        FileOutputStream fos = null;
//        fos = new FileOutputStream("data/index/term.idx");
//        fos.write(res);
//        
//        //READ FROM FILE
//        FileInputStream fis = null;
//        fis = new FileInputStream("data/index/test.idx");
//        List<Integer> test = ByteAlignCompress.DECODE(getNextChunk(fis));
//        test.addAll(ByteAlignCompress.DECODE(getNextChunk(fis)));
//        System.out.println(test);
        
        
        //**************For Term**********************
//        String str = "4cdbc040657a4847b2667e31d9e2c3d9";
        //System.out.println(str.length());
//        byte[] byBuffer = new byte[str.length()];
        //Vector<byte> terms = new Vector<byte>();
//        byBuffer = str.getBytes();
//        for(byte b: byBuffer){
//            System.out.print(b + " ");
//        }
//        System.out.println();
//        fos.write(byBuffer);
//        
//        FileInputStream fis = null;
//        fis = new FileInputStream("data/index/term.idx");
//        byte[] term = getNextChunk(fis);
//        for(int i = 0; i < 31 ; i++){
//            term = getNextChunk(fis);
//        }
//        fis.read();
//        
//        
//        String str2 = new String(term);
//        str2 = String.copyValueOf(str2.toCharArray(),0,term.length);
//        System.out.println("str2 " + str2);

        
        
    }
       

    
}