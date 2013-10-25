import java.util.*;

public class Compress {
        /*
    VB_Compress(n)
    1 bytes = <>
    2 while true
    3 do PREPEND(bytes, n mod 128)
    4   if n < 128
    5   then BREAK
    6   n = n div 128
    7 bytes[LENGTH(bytes)] += 128
    8 return bytes
*/    
    
    public static List<Byte> VB_Compress(int n) {
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
        VBENCODE(numbers)
        1 bytestream = <>
        2 for each n in numbers
        3 do bytes = VB_Compress(n)
        4       bytestream = EXTEND(bytestream, bytes)
        5 return bytestream   
  */
    
    /*  public static byte[] VBENCODE(List<Integer> numbers) {
        List<Byte> bytestream_l = new ArrayList<Byte>();
        
        for (Integer n : numbers) {
                List<Byte> bytes = VB_Compress(n);
                bytestream_l.addAll(bytes);
        }
        
        //Convert result to byte[], then return. 
        byte[] bytestream = new byte[bytestream_l.size()];
        for (int i=0; i<bytestream_l.size(); i++) bytestream[i] = bytestream_l.get(i); 
        return bytestream;
	}*/
     public static List<Byte> VBENCODE(List<Integer> numbers) {
        List<Byte> bytestream_l = new ArrayList<Byte>();
        
        for (Integer n : numbers) {
                List<Byte> bytes = VB_Compress(n);
                bytestream_l.addAll(bytes);
        }
        
        //Convert result to byte[], then return. 
	// byte[] bytestream = new byte[bytestream_l.size()];
        //for (int i=0; i<bytestream_l.size(); i++) bytestream[i] = bytestream_l.get(i); 
        return bytestream_l;
    }
    public static byte[] VBENCODE(int[] numbers) {
        List<Byte> bytestream_l = new ArrayList<Byte>();
        
        for (Integer n : numbers) {
                List<Byte> bytes = VB_Compress(n);
                bytestream_l.addAll(bytes);
        }
        
        //Convert result to byte[], then return. 
        byte[] bytestream = new byte[bytestream_l.size()];
        for (int i=0; i<bytestream_l.size(); i++) bytestream[i] = bytestream_l.get(i); 
        return bytestream;
    }
    
/*    
    VBDECODE(bytestream)
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
    
    public static List<Integer> VBDECODE(List<Byte> bytestream) {
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
    
    public static List<Integer> VBDECODE(byte[] bytestream, int off, int len) {
        List<Integer> numbers = new ArrayList<Integer>();
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
    
    
    public static int firstDocIdOfChunk(List<Byte> bytestream, int chunkNum, int chunkID) {//return the first value of the chunk
        int n = 0;
        int offset = chunkNum;//offset of docId is equal to  chunk number of this inverted list
        for(int i=0; i< chunkID; i++) {
	    offset += (int)bytestream.get(i) & 0xff;
        }
        for (int i=offset; ; i++) {
            if ( (bytestream.get(i) & (byte)(0x80)) == 0 ){
		n = 128*n + bytestream.get(i);
                    return n;
            }
            else {
		byte b = (byte)(bytestream.get(i) & 0x7F); //Achieves the effect of -= 128. 
                    n = 128*n + b;
            }
        }
    }
    
    
   public static void main(String[] args) throws Exception {
        //Testing
       /* List<Integer> numbers = new ArrayList<Integer>();
        numbers.add(2); 
        numbers.add(120); 
        numbers.add(65536);
	numbers.add(3);
	numbers.add(999);
	numbers.add(199);
	//	int numbers[] = {2,120,65536,65547,8913248};
	//   byte[] tmp = Compress.VBENCODE(numbers);
	List<Byte> temp = Compress.VBENCODE(numbers);
	  List<Integer> umcompress = Compress.VBDECODE(temp);
	//  byte[] result = Compress.VBENCODE(numbers);
        int id = Compress.firstDocIdOfChunk(temp, 0,0);
	System.out.println("Finish"); 
		System.out.println(id);
	//System.out.println(tmp);
        System.out.println(umcompress);
    }*/

	   String s = "[1, 8]";
	   String temp = s.replaceAll("\\[]", "");
	//	temp = s.replaceAll("]", "");

	   System.out.println(temp);
  }
}
