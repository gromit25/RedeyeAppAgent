package com.redeye.appagent.domain.metric.dto;

/**
 * 성능 정보 DTO 클래스
 *
 * @author jmsohn
 */
@Data
public class MetricDTO implements JSONEntity {
  

  /** CPU 사용율(%) */
  private double cpuUsage;

  /** 최대 힙메모리(Byte) */
  private long maxHeap;

  /** 사용중 힙메모리(Byte) */
  private long usedHeap;
  
  /** 최대 비힙메모리(Byte) */
  private long maxNonHeap;

  /** 사용중 비힙메모리(Byte) */
  private long usedNonHeap;

  /** 활성화된 스레드 수 */
  private int threadCount;
  

  @Override
  public String toJSON(String indent) {

    return new StringBuilder()
      .append("{")
      .append(indent).append(String.format("\"cpuUsage\": %f.2,", this.cpuUsage))
      .append(indent).append(String.format("\"maxHeap\": %d,", this.maxHeap))
      .append(indent).append(String.format("\"usedHeap\": %d,", this.usedHeap))
      .append(indent).append(String.format("\"maxNonHeap\": %d,", this.maxNonHeap))
      .append(indent).append(String.format("\"usedNonHeap\": %d,", this.usedNonHeap))
      .append(indent).append(String.format("\"threadCount\": %d", this.threadCount))
      .append(indent).append("}")
      .toString();
  }
}
