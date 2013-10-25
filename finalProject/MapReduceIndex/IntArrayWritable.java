
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;


public class IntArrayWritable extends ArrayWritable implements WritableComparable<IntArrayWritable> {
	   
	   public IntArrayWritable(){
		   super(IntWritable.class);
	   }
	   
	   public IntArrayWritable(IntWritable[] values){
		   super(IntWritable.class, values);
	   }

	@Override
	public int compareTo(IntArrayWritable arr) {
		IntWritable[] arr_1 = (IntWritable[])this.toArray();
		IntWritable[] arr_2 = (IntWritable[])arr.toArray();
		
		if(arr_1[0].get() == arr_2[0].get()){
			return 0;
		}else if(arr_1[0].get() < arr_2[0].get()){
			return -1;
		}else{
			return 1;
		}
	}
}
