package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class AmazonEC2PreProcessor {

	private static String prefix = "/Users/tao/research/projects/ssase-core/ssase/experiments-data/on-off/amazon-ec2/dataset/";
	
	public  static void main (String[] a) {
		read();
	}
	
	private static void read(){
		
		
		File file = new File(prefix);
		for (File f1 : file.listFiles()) {
			
			if(f1.getName().equals(".DS_Store")) {
				continue;
			}
			
			String o = "";
			try {

				String line = null;
				// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
				BufferedReader reader = new BufferedReader(new FileReader(f1+"/Time.txt"));
				while ((line = reader.readLine()) != null) {

					int n = Integer.parseInt(line.split(":")[0]);
					n++;
					o += n + "\n";
					
				}

				reader.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			File wfile = new File(prefix + f1.getName() + "/Time-parsed.txt");
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(wfile, false));

				
				//System.out.print(data.toString() + "\n");
				bw.write(o);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
	
		
	}
}
