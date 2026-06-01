package com.redeye.appagent.domain.metric.dto;

/**
 * 성능 정보 DTO 클래스
 *
 * @author jmsohn
 */
@Data
public class MetricDTO {

  /** CPU 사용율(%) */
  private double cpuUsage;

  /** 최대 힙메모리(Byte) */
  private long maxHeapMem;

  /** 사용중 힙메모리(Byte) */
  private long usedHeapMem;
  
  /** 최대 비힙메모리(Byte) */
  private long maxNonHeapMem;

  /** 사용중 비힙메모리(Byte) */
  private long usedNonHeapMem;

  /** 활성화된 스레드 수 */
  private int threadCount;
}
