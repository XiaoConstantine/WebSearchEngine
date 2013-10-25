import java.io.*;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;


public class HTML {
	public static String stemm(String b) {
        String a=b;
        Stemmer s = new Stemmer();
        for (int i = 0;i<b.length();i++)
            if (b.charAt(i)<'0'||b.charAt(i)>'9') {
                s.add(b.charAt(i));
            }
            else return b;
        s.stem();
        a = s.toString();
        return a;
    }

    public static Vector<String> parse(String fileName) {
    	//Stopword sw = new Stopword();
        Vector<String> ret = new Vector<String>();
        try {
        
       // fileName = "/Users/Constantine/hadoop/wiki/" + fileName;
       // System.out.println(fileName + "  HTML");
        BufferedReader reader = new BufferedReader(new FileReader (fileName));
		int scriptFlag = 0; // 0 means no script, 1 means <script>, 2 means <script ...>
		String line = null;
            
		while ((line = reader.readLine()) != null) {
				// remove the content in <script>
				//System.out.println(line);
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
                            processWikiDoc(line.substring(0, line.indexOf("<script>")), ret);
							scriptFlag = 1;
							line = line.substring(line.indexOf("<script>") + 8);
						} else if (line.contains("<script")) {
							// parse the no script content and check the remain string
                            processWikiDoc(line.substring(0, line.indexOf("<script")), ret);
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
				processWikiDoc(line, ret);
		}
        
        
        
        /*int r;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            if (bracketCount>0) {
                if (current.length()<=10)
                    current+=c;
                if (c=='>')
                    bracketCount --;
                if (c=='<')
                    bracketCount ++;
                if (bracketCount ==0&&current.length()>0) {
                    if (current.charAt(0)=='<') {
                        if (current.length()>=7 && current.substring(0,7).equals("<script")){
                            script ++;
                        }
                        else if (current.equals("</script>")) {
                            script --;
                        }
                    current = "";
                    }
                }
            }
            else if ((c<='z'&&c>='a')||(c<='Z'&&c>='A')||(c<='9'&&c>='0')) {
                if (script==0) {
                    current = current+c;
                }
            }
            else {
                if (script==0 && current.length()>0 && bracketCount == 0) {
                	String tmp = stemm(current);
                	//if (!sw.contain(tmp))
                		ret.add(tmp);
                }
                current = "";
                if (c=='<') {
                    bracketCount++;
                    current="<";
                }
            }
        }*/
        System.out.println("Size:" + ret.size());
        reader.close();
        } catch (IOException e){
            System.out.println("readfile error!");
        }
        return ret;
    }
        
    private static void processWikiDoc(String content, Vector<String> ret){
        String pureText;
		pureText = content.replaceAll("<[^>]*>", " ");
		pureText = pureText.replaceAll("\\pP|\\pS|\\pC", " ");
		Scanner s = new Scanner(pureText).useDelimiter(" ");
		
		while (s.hasNext()) {
			String token = stemm(s.next());
			// only consider numbers and english
			if (token.matches("[0-9a-z]*") == false || token.isEmpty()) continue;
			// check the stemmed token
			ret.add(token);
		}
		s.close();
    }

      
}
