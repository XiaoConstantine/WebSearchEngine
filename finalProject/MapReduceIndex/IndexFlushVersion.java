import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class IndexFlushVersion {
	public static class invertedMapper extends Mapper<LongWritable, Text, Text, IntArrayWritable>{
		private HashMap<String, FlushValuePair> cache;
		public void map(LongWritable key, Text value, Context context)throws IOException, InterruptedException{
			String line = value.toString();
			String[] temp = line.split(" ");
			String docid = temp[1];
			Vector<String> tokens = new Vector<String>();
			for(int i = 2; i < temp.length; i++){
				tokens.add(temp[i]);
			}
			posInfo(tokens, docid, context);
		}
        public void posInfo(Vector<String> tokens,String docid, Context context) throws IOException, InterruptedException{
            for(int i = 0; i < tokens.size(); i++){
                FlushValuePair pair;
                if(!cache.containsKey(tokens.get(i))){
                    if(cache.size() >= 40000){
                        flush(context);
                    }
                    pair = new FlushValuePair();
                    pair.addValue(Integer.parseInt(docid), i);
                    cache.put(tokens.get(i), pair);  // term->docid, poslist 
                }else{
                    pair = cache.get(tokens.get(i));
                    pair.addValue(Integer.parseInt(docid), i);   			
                }	
            }
        }
        private void flush(Context context) throws IOException, InterruptedException{
			for(Entry<String, FlushValuePair> entry: cache.entrySet()){
				FlushValuePair temp = entry.getValue();
				Vector<IntWritable> data = new Vector<IntWritable>();
				for(ArrayList<Integer> pos: temp.getList()){
					for(Integer p : pos){
						data.add(new IntWritable(p));
					}
					data.add(new IntWritable(-1));
				}
				IntWritable[] arr = new IntWritable[data.size()];
				for(int i = 0; i < data.size(); i++){
					arr[i] = data.get(i);
				}
			
				context.write(new Text(entry.getKey()), new IntArrayWritable(arr));
			}
			cache.clear();
		}
	 
	    protected void setup(Context context)throws IOException, InterruptedException{
	    	cache = new HashMap<String, FlushValuePair>();
	    }
	    
	    protected void cleanup(Context context)throws IOException, InterruptedException{
	    	flush(context);
	    }
	 	public void run(Context context)throws IOException, InterruptedException{
	 		setup(context);
	 		while(context.nextKeyValue()){
	 			map(context.getCurrentKey(), context.getCurrentValue(), context);
	 		}
	 		cleanup(context);
	 	}
	}
	
	public static class invertedCombiner extends Reducer<Text, IntArrayWritable, Text, IntArrayWritable>{
		public void combine(Text key, Iterable<IntArrayWritable>values, Context context)throws IOException, InterruptedException{
			ArrayList<Integer> list = new ArrayList<Integer>();
			ArrayList<IntWritable[]> v = new ArrayList<IntWritable[]>();
			for(IntArrayWritable arr: values){
				IntWritable[] arr_t = (IntWritable[])arr.toArray();
			   if(list.contains(arr_t[0].get()) == false){
			    	list.add(arr_t[0].get());
			    }
			    v.add(arr_t);
			}
			Collections.sort(list);
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for(int i = 0; i < list.size();){
				for(IntWritable[] arr: v){
					if(list.get(i) == arr[0].get()){
						for(int j = 0; j < arr.length; j++){
							temp.add(arr[j].get());
						}
						i++;
						break;
					}
				}
			}
			v.clear();
			list.clear();
			IntWritable[] arr_result = new IntWritable[temp.size()];
			for(int i = 0; i < temp.size(); i++){
				arr_result[i] = new IntWritable(temp.get(i));
			}
			temp.clear();
			context.write(key, new IntArrayWritable(arr_result));
		}
	}
	
	public static class invertedReducer extends Reducer<Text, IntArrayWritable, Text, Text>{
		public void reduce(Text key, Iterable<IntArrayWritable>values, Context context)throws IOException, InterruptedException{
			ArrayList<Integer> list = new ArrayList<Integer>();
			ArrayList<IntWritable[]> v = new ArrayList<IntWritable[]>();
			for(IntArrayWritable arr: values){
				IntWritable[] arr_t = (IntWritable[])arr.toArray();
			   if(list.contains(arr_t[0].get()) == false){
			    	list.add(arr_t[0].get());
			    }
			    v.add(arr_t);
			}
			Collections.sort(list);
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for(int i = 0; i < list.size();){
				for(IntWritable[] arr: v){
					if(list.get(i) == arr[0].get()){
						for(int j = 0; j < arr.length; j++){
							temp.add(arr[j].get());
						}
						i++;
						break;
					}
				}
			}
			v.clear();
			list.clear();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < temp.size(); i++){
                if(temp.get(i) != -1){
                    sb.append(temp.get(i).toString());
                    sb.append(",");
                }else{
                    sb.append("#");
                }
            }
		   context.write(key, new Text(sb.toString()));	
		}
	}
	
	public static class invertedPartitioner extends Partitioner<Text , IntArrayWritable>{
		@Override
		public int getPartition(Text key, IntArrayWritable value, int numPartitions) {
			String keyStr = key.toString();
			int idx = keyStr.charAt(0) - 'a';
			int result = 0;
			if(idx >= 0){
				if(idx/5 == 0){
					result = Math.abs(idx/5)%numPartitions; 
				}else if(idx/5 == 1){
					result = Math.abs(idx/5)%numPartitions; 
				}else if(idx/5 == 2){
					result = Math.abs(idx/5)%numPartitions; 
				}else if(idx/5 == 3){
					result = Math.abs(idx/5)%numPartitions; 
				}else{
					result = Math.abs(idx/5)%numPartitions; 
			    }
		  }else{
			  result =  Math.abs(numPartitions) - 1;
		  }
			return result;
		}	
	}
	
	public static void main(String[] args)throws Exception{
		Job job = new Job();
        // Set job name to locate it in the distributed environment
        job.setJarByClass(IndexFlushVersion.class);
        job.setJobName("Index");
        
        job.setNumReduceTasks(6);
        // Set input and output Path, note that we use the default input format
        // which is TextInputFormat (each record is a line of input)
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Set Mapper Combiner Partitioner Reducer class
        job.setMapperClass(invertedMapper.class);
        job.setPartitionerClass(invertedPartitioner.class);
        job.setCombinerClass(invertedCombiner.class);
        job.setReducerClass(invertedReducer.class);
        
        // Set MapperOutput key and value
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntArrayWritable.class);
        // Set Output key and value;
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
