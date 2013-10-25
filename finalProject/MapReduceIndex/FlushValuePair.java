import java.util.ArrayList;

public class FlushValuePair {
    ArrayList<ArrayList<Integer>> pos_List;
    
    public FlushValuePair(){
    	this.pos_List =  new ArrayList<ArrayList<Integer>>();
    }
    
    public void addValue(int docid, int pos){
    	ArrayList<Integer> newList = null;
    	if(this.getDocids().contains(docid) == true){
			for(ArrayList<Integer> list: pos_List){
				if(list.get(0) == docid){
					newList = list;
					break;
				}
			}
    		newList.add(pos);
    	}else{
    		newList = new ArrayList<Integer>();
    		newList.add(docid);
        	newList.add(pos);
        	this.pos_List.add(newList);
    	}
    }
    
    public ArrayList<Integer> getDocids(){
    	ArrayList<Integer> docidsList = new ArrayList<Integer>();
    	for(ArrayList<Integer> list : this.pos_List){
    		docidsList.add(list.get(0));
    	}
    	return docidsList;
    }
    
    public int getDocidNumber(){
    	return getDocids().size();
    }
    
    public ArrayList<ArrayList<Integer>> getList(){
    	return this.pos_List;
    }
    
    public ArrayList<Integer> getSingleList(int index){
    	if(index < 0 || index > getDocids().size()){
    		return null;
    	}else{
    		return this.pos_List.get(index);
    	}
    }
    
    public int listSize(){
    	int size = 0;
    	for(ArrayList<Integer> list: pos_List){
    		size += list.size();
    	}
    	size += getDocids().size();
    	return size;
    }
    
}
