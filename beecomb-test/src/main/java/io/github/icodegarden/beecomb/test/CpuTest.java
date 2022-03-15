package io.github.icodegarden.beecomb.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CpuTest {
	
	public static void main(String[] args) {
		System.out.println(Runtime.getRuntime().availableProcessors());
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10000);
		
		for(int i=0;i<10000;i++) {
			executorService.scheduleAtFixedRate(()->{
				try {
					for(int j=0;j<3000;j++) {
						new Object();
					}
				}catch (Exception e) {
					System.out.println(e);
				}
			}, 0, 1000, TimeUnit.MICROSECONDS);
		}
	}
}
