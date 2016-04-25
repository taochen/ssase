package org.ssase.sensor.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.ssase.sensor.Sensor;

public class EnergySensor implements Sensor {

	final String command =  "/bin/sh /root/monitor/energy_monitor.sh";
	//"/bin/sh /Users/tao/research/projects/ssase-core/ssase/test";
	private double total = 0.0;
	private double number = 0.0;
	private int process_live_count = 3;
	public static final int index = 5;
	
	private static final int SAMPLING_INTERVAL = 60000; // We can make the frequency on monitor as 3000ms, last for 60s
	private Timer timer;
	
	private BufferedReader br = null;
	private Process p = null;

	public static void main(String[] a) {
		new EnergySensor().execute();
	}

	public EnergySensor() {

	}

	@Override
	public double[] runMonitoring() {
		double current = 0.0;
		double no = 0.0;
		synchronized (this) {
			current = total;
			no = number;
			total = 0.0;
			number = 0.0;
//			if (no == 0) {
//				no = 1;
//			}
			
			System.out.print("********current="+current+"\n");
			System.out.print("********no="+no+"\n");
		}

		return new double[] { current / no };
	}

	
	private void execute() {

		BufferedReader br = null;
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("muid=")) {
					String[] result = line.split("=");
					synchronized (this) {
						if (!Double.isNaN(Double
								.parseDouble(result[result.length - 1]))) {
							total += Double
									.parseDouble(result[result.length - 1]);
							number++;
						} else {
							System.out.print("Energy is NaN!\n");
						}
					}
				} else {
					//System.out.print(line + " can not read !!!! \n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				p.destroy();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void run() {
		if(timer != null) return;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				execute();
			}
		}, 100, SAMPLING_INTERVAL);
	}
	
	public void testRun() {
		
		if (p != null)
			return;
		
		try {

			new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.print("energy start\n");
					int live_count = 0;
					try {
						p = Runtime.getRuntime().exec(command);

						br = new BufferedReader(new InputStreamReader(p
								.getInputStream()));

						String line = null;

						while ((line = br.readLine()) != null
								&& live_count < process_live_count) {
							//System.out.print("********"+line+"\n");
							live_count++;
							if (line.startsWith("muid=")) {
								String[] result = line.split("=");
								synchronized (this) {
									if (!Double.isNaN(Double
											.parseDouble(result[result.length - 1]))) {
										total += Double
												.parseDouble(result[result.length - 1]);
										number++;
									} else {
										System.out.print("Energy is NaN!\n");
									}
								}
							} else {
								//System.out
									//	.print(line + " can not read !!!! \n");
							}
						}

					} catch (NumberFormatException e) {
						live_count = process_live_count;
						e.printStackTrace();
					} catch (IOException e) {
						live_count = process_live_count;
						e.printStackTrace();
					} finally {
						live_count = process_live_count;
						try {
							System.out.print("energy stop\n");
							p.destroy();
							br.close();
							p = null;
							br = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					run();
				}

			}).start();

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String[] getName() {
		// TODO Auto-generated method stub
		return new String[] { "Energy" };
	}

	@Override
	public void destory() {
		if(timer != null) timer.cancel();
		
		try {
			if(p != null) p.destroy();
			if(br != null) br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initInstance(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public double recordPriorToTask(Object value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub

	}

}
