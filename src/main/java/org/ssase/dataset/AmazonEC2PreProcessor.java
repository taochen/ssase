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
	String f = prefix + "temp.txt";
		
		
	
	String o = "";
		double r = 0.0;
		try {

			String line = null;
			// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while ((line = reader.readLine()) != null) {

				/**
				 * Time 9, 23
				 * CPU 5 or 6, 19 or 20
				 */
				
					 System.out.print(line  + "\n");
				
			}

			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		f = prefix + "C4L/CPU.rtf";
		File file = new File(f);
		if(!file.exists()) {
			//file.mkdirs();
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));

			
			//System.out.print(data.toString() + "\n");
			bw.write(o);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
