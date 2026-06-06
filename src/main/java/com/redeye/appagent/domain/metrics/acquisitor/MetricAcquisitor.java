package com.redeye.appagent.domain.metrics.acquisitor;

import javax.management.openmbean.CompositeData;

import com.redeye.appagent.domain.metrics.entity.MetricDTO;
import com.redeye.appagent.util.jmx.JMXService;

/**
 * 프로세스(JVM) 의 성능 정보(CPU, Memory 등) 수집기
 *
 * @author jmsohn
 */
public class MetricAcquisitor {

	
	/** 성능 측정을 위한 JMX 객체 */
	private static final JMXService jmxSvc = new JMXService();
	
	
	// ----------------------------------
	
	/**
	 * 프로세스(JVM) 의 성능 정보 반환
	 *
	 * @return 프로세스(JVM) 의 성능 정보
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
	 * 프로세스(JVM) 의 CPU 성능 정보 반환
	 *
	 * @return 프로세스(JVM) 의 CPU 성능 정보
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
	 * 프로세스(JVM) 의 최대 힙메모리 크기(byte) 반환
	 *
	 * @return 최대 힙메모리 크기
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
	 * 프로세스(JVM) 의 사용 중 힙메모리 크기(byte) 반환
	 *
	 * @return 사용 중 힙메모리 크기
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
	 * 프로세스(JVM) 의 최대 논힙메모리 크기(byte) 반환
	 *
	 * @return 최대 힙메모리 크기
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
	 * 프로세스(JVM) 의 사용 중 논힙메모리 크기(byte) 반환
	 *
	 * @return 사용 중 논힙메모리 크기
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
	 * 프로세스(JVM) 의 스레드 개수 반환
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
