package com.redeye.appagent.domain.metric.acquisitor;

import javax.management.openmbean.CompositeData;

import com.redeye.appagent.domain.metric.entity.MetricDTO;
import com.redeye.appagent.util.jmx.JMXService;

/**
 *
 *
 * @author jmsohn
 */
public class MetricAcquisitor {

	
	/** */
	private static final JMXService jmxSvc = new JMXService();
	
	// ----------------------------------
	
	/**
	 *
	 *
	 * @return
	 */
	public static MetricDTO getMetric() {
		
		return new MetricDTO(
			getCPUUsage(),
			getMaxHeap(),
			getUsedHeap(),
			getMaxNonHeap(),
			getUsedNonHeap(),
			getThreadCount()
		);
	}

	// ----------------------------------

	/**
	 *
	 *
	 * @return
	 */
	public static double getCPUUsage() {
		
		Double cpuUsage = jmxSvc.get(
			"java.lang:type=OperatingSystem",
			"ProcessCpuLoad",
			Double.class
		);
		
		if(cpuUsage != null) {
			return cpuUsage;
		} else {
			return -1.0;
		}
	}

	// ----------------------------------

	/**
	 *
	 *
	 * @return
	 */
	public static long getMaxHeap() {
		
		CompositeData heapUsage = jmxSvc.get(
			"java.lang:type=Memory",
			"HeapMemoryUsage",
			CompositeData.class
		);
		
		if(heapUsage == null) {
			return -1L;
		}
		
		Object maxObj = heapUsage.get("max");
		if(maxObj == null || maxObj instanceof Long == false) {
			return -1L;
		}
		
		return (Long) maxObj;
	}
	
	/**
	 *
	 *
	 * @return
	 */
	public static long getUsedHeap() {

		CompositeData heapUsage = jmxSvc.get(
			"java.lang:type=Memory",
			"HeapMemoryUsage",
			CompositeData.class
		);
		
		if(heapUsage == null) {
			return -1L;
		}

		Object usedObj = heapUsage.get("used");
		if(usedObj == null || usedObj instanceof Long == false) {
			return -1L;
		}

		return (Long) usedObj;
	}

	/**
	 *
	 *
	 * @return
	 */
	public static long getMaxNonHeap() {
		
		CompositeData nonHeapUsage = jmxSvc.get(
			"java.lang:type=Memory",
			"NonHeapMemoryUsage",
			CompositeData.class
		);
		
		if(nonHeapUsage == null) {
			return -1L;
		}

		Object maxObj = nonHeapUsage.get("max");
		if(maxObj == null || maxObj instanceof Long == false) {
			return -1L;
		}

		return (Long) maxObj;
	}

	/**
	 *
	 *
	 * @return
	 */
	public static long getUsedNonHeap() {

		CompositeData nonHeapUsage = jmxSvc.get(
			"java.lang:type=Memory",
			"NonHeapMemoryUsage",
			CompositeData.class
		);
		
		if(nonHeapUsage == null) {
			return -1L;
		}

		Object usedObj = nonHeapUsage.get("used");
		if(usedObj == null || usedObj instanceof Long == false) {
			return -1L;
		}

		return (Long) usedObj;
	}

	// ----------------------------------

	/**
	 *
	 *
	 * @return 스레드 개수
	 */
	public static int getThreadCount() {

		Integer threadCount = jmxSvc.get(
			"java.lang:type=Threading",
			"ThreadCount",
			Integer.class
		);

		if(threadCount != null) {
			return threadCount;
		} else {
			return -1;
		}
	}
}
