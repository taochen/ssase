package org.soa.wsdream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataFileParser {

	private static List<Double> tp = new ArrayList<Double>();
	private static List<Double> rt = new ArrayList<Double>();
	private static int counter = 0;

	public static void main(String[] args) {
		String path = "/Users/tao/research/experiments-data/seed/services-bk1/";
		String path1 = "/Users/tao/research/experiments-data/seed/services/";
		File file = new File(path);
		//System.out.print(file.listFiles().length + "***\n");
		for(File f : file.listFiles()) {
			if(f.getName().equals(".DS_Store")) {
				continue;
			}
			try {
				//fix(f);
				write(path1+f.getName(),fix(f));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.print(file.listFiles().length + "***\n");
			;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main1(String[] args) {
		String path = "/Users/tao/research/experiments-data/on-off/wsdream/processed/";
		String path1 = "/Users/tao/research/experiments-data/seed/services/";
		Random r = new Random();
		List<Integer> selected = new ArrayList<Integer>();
		for (int k = 0; k < 50; k++) {
			tp.clear();
			rt.clear();
			int n = -1;
			do {
				n = r.nextInt(4500);
			} while (selected.contains(n));

			selected.add(n);
			read(path + "rtdata/service" + n, rt);
			read(path + "tpdata/service" + n, tp);

			List<Double> sort = new ArrayList<Double>();
			sort.addAll(rt);
			Collections.sort(sort);

			List<Double> cost = new ArrayList<Double>();
			cost.addAll(rt);
			// List<Double> ct = new ArrayList<Double>();

			for (int i = 0; i < sort.size(); i++) {
				// cost.add(r.nextDouble() + i);
				// System.out.print(rt.indexOf(sort.get(i))+ ":" + i + "\n");
				// cost.
				cost.set(rt.indexOf(sort.get(i)), r.nextDouble() + sort.size()
						- i);
			}

			String data = "";
			for (int i = 0; i < sort.size(); i++) {
				data += rt.get(i) + "," + tp.get(i) + "," + cost.get(i) + "\n";
			}
			System.out.print("No. " + n + "\n");
			write(path1 + "service" + counter + ".rtf", data);
			counter++;
		}

	}

	public static void read(String path, List<Double> list) {

		File file = new File(path);

		for (File f : file.listFiles()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = reader.readLine();
				list.add(Double.parseDouble(line.split("-")[1]));
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void write(String path, String data) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));

			bw.write(data);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fix(File file) {
		
	

		List<Double> l = new ArrayList<Double>();
		List<Double> t = new ArrayList<Double>();
		List<Double> c = new ArrayList<Double>();

		List<Double> sort_l = new ArrayList<Double>();
		List<Double> sort_c = new ArrayList<Double>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				String[] s = line.split(",");
				l.add(Double.parseDouble(s[0]));
				t.add(Double.parseDouble(s[1]));
				c.add(Double.parseDouble(s[2]));
			}

			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sort_l.addAll(l);
		for (int i = 0; i < c.size(); i++) {
			if(c.get(i) > 1.0) {
				sort_c.add(c.get(i));
			}
		}
		
		
		String data = "";
		for (int i = 0; i < l.size(); i++) {
			if(l.get(i).equals(c.get(i))) {
				int index = sort_l.indexOf(l.get(i)) + 1;
				double p = index / sort_l.size();
				int new_index = (int)((1-p)*sort_c.size());
				if(new_index == sort_c.size()) {
					new_index = sort_c.size() - 1;
				}
				c.set(i, sort_c.get(new_index));
				//System.out.print(sort_c.get(new_index)+" new one \n" );
			}
			
			if(l.get(i).equals(0.0)) {
				l.set(i, 100000.0); // set a large value
			}
			
			if(t.get(i).equals(0.0)) {
				t.set(i, 1.0/l.get(i));
			}
			
			data += l.get(i) + "," + t.get(i) + "," + c.get(i) + "\n";
		}
		System.out.print("-------" + file.getName()+ "-----\n");
		System.out.print(data+ "-----\n");
		return data;
	}

}
