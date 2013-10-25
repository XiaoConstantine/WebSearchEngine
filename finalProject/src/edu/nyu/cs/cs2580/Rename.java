
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
import java.io.IOException;
public class Rename {
	public static void rename() {
		String dirPath = "/Users/Work_Git/wse/HW3/data/wiki_original";
		File dir = new File(dirPath);
		
		File[] files = dir.listFiles();
		System.out.println(files.length);
        int tmpcount = 0;
		
		// rename
		for (int i = 0; i < files.length; ++i) {
			if (files[i].getName().contains("DS_Store")) {
				continue;
			}
			String fileName = files[i].getName().replaceAll("[\\W]", "_");
			String resultName;
			
			if (!fileName.matches("[_]")) {
				resultName = files[i].getParent() + "/" + fileName;
			} else {
                resultName = files[i].getParent() + "/" + fileName + tmpcount;
                tmpcount++;
            }
            files[i].renameTo(new File(resultName));
            //			System.out.print(files[i].getName());
            //			if (!fileName.matches("[_]")) System.out.println(", " + fileName);
            //			else System.out.println();
            
		}
		System.out.println(files.length);
		
	}
	
    
    /*public static void loadFilePath() throws IOException{
        final File folder = new File("/Users/Constantine/hadoop/input");
        //final File folder = new File("./data");
        File dir = new File("/Users/Constantine/hadoop/input_use");
        dir.mkdirs();
        Vector<String> tokens;
        int count = 0;
        int count_file = 0;
        for(final File fileEntry: folder.listFiles()){
            if(!fileEntry.isDirectory()){
                if(fileEntry.getName().equals(".DS_Store")){
                    continue;
                }
	    		//  System.out.println(fileEntry.getAbsolutePath());
                tokens = HTML.parse(fileEntry.getAbsolutePath());
                File tempfile = new File(dir + "/" +  count_file);
                
                BufferedWriter out = new BufferedWriter(new FileWriter(tempfile,true));
                out.write(fileEntry.getName() + " " + count);
                for(String token : tokens){
                    out.write(" " + token);
                }
                out.write("\n");
                out.close();
                count++;
                if(tempfile.length()/(1024*1024) >=60){
                    count_file++;
                }
            }
            
        }

    }*.
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		rename();
        //loadFilePath();
	}

}
