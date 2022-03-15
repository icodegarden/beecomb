package io.github.icodegarden.beecomb.test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MemoryTest {
	public static void main(String[] args) throws IOException {
//		new Thread() {
//			public void run() {
//				for(;;) {
//					System.out.println("Xmx:"+RuntimeUtils.getRuntime().getJvmMaxMemory());
//					System.out.println("use:"+RuntimeUtils.getRuntime().getJvmUsedMemory());	
//					System.out.println("total:"+RuntimeUtils.getRuntime().getTotalPhysicalMemorySize());	
//					System.out.println("use:"+RuntimeUtils.getRuntime().getUsedPhysicalMemorySize());	
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			};
//		}.start();
		
		for(int i=0;;i++) {
			System.out.println(i);
			System.in.read();
			for(int j=0;j<1000;j++) {
				new Thread() {
					public void run() {
						for(;;) {
							try {
								new MemoryTest().testStack(0);
								Thread.sleep(1000000000);
							} catch (InterruptedException e) {
							}
						}
					};
				}.start();
			}
		}
	}
	
	private final int maxLoop = 1;
	private List list = new LinkedList<>();
	
	private void testStack(int loop) throws InterruptedException {
		if(loop != maxLoop) {
			for(int i=0;i<1000;i++) {
				list.add(new Object());
			}
			testStack(++loop);
		}else {
			Thread.sleep(100000000);
			System.out.println(list.size());
		}
	}
	

}
