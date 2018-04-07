package org.soa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Analyzer {

	public static void main(String[] a) {
		//new File("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd/*").deleteOnExit();
		
		
		//read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd/Throughput.rtf");
		//read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed-base/Throughput.rtf");
		read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed/Throughput.rtf");
		read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed1/Throughput.rtf");
		//read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd/Cost.rtf");
		//read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed-base/Cost.rtf");
		read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed/Cost.rtf");
		read("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed1/Cost.rtf");
		
		deleteDirectory(new File("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed"));
		deleteDirectory(new File("/Users/tao/research/monitor/sas-soa/sas/NSGAIIkd-seed1"));
	}
	
	
	public static void read(String path) {

		File file = new File(path);
		double v = 0.0;
		int no = 0;
		try {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int i = 0;
		while((line = reader.readLine()) != null) {
//			if(i <= 200) {
//				i++;
//				continue;
//			}
			v += Double.parseDouble(line);
			no++;
//			if(path.endsWith("Throughput.rtf")) {
//				System.out.print((1.0/Double.parseDouble(line)) + "\n");
//			} else {
//				System.out.print(line + "\n");
//			}
			
		}
		}catch (Exception e) {
			
		}
		
		System.out.print(path + "=" + (v/no) + "\n");
	}
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
}
