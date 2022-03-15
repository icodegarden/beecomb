package io.github.icodegarden.beecomb.executor.sample.handler;
//package com.mycode.beecomb.executor.sample.handler;
//
//import java.util.Random;
//
//import com.mycode.beecomb.executor.registry.JobHandler;
//import com.mycode.beecomb.loadbalance.ExecuteJobResult;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public class SampleScheduleJobHandler implements JobHandler {
//
//	@Override
//	public String name() {
//		return "sampleSchedule";
//	}
//
//	@Override
//	public ExecuteJobResult handle() {
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//		}
//		System.out.println("sampleSchedule done");
//		ExecuteJobResult executeJobResult = new ExecuteJobResult();
//
//		executeJobResult.setExecuteReturns(System.currentTimeMillis() + "");
//		boolean b = new Random().nextInt(10) % 9 == 0;
//		if (b) {
//			executeJobResult.setEnd(true);
//		}
//
//		return executeJobResult;
//	}
//
//}
