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
  private long maxHeapMem;

  /** 사용중 힙메모리(Byte) */
  private long usedHeapMem;
  
  /** 최대 비힙메모리(Byte) */
  private long maxNonHeapMem;

  /** 사용중 비힙메모리(Byte) */
  private long usedNonHeapMem;

  /** 활성화된 스레드 수 */
  private int threadCount;
  

  @Override
  public String toJSON(String indent) {

    if(indent != null) {
      indent = "\r\n" + indent;
    } else {
      indent = "";
    }
    
    return new StringBuilder()
      .append("{")
      .append(indent).append(String.format("\"cpuUsage\": %f5.2,", this.cpuUsage))
      .append(indent).append(String.format("\"maxHeap\": %d,", this.maxHeapMem))
      .append(indent).append(String.format("\"usedHeap\": %d,", this.usedHeapMem))
      .append(indent).append(String.format("\"maxNonHeap\": %d,", this.maxNonHeapMem))
      .append(indent).append(String.format("\"usedNonHeap\": %d,", this.usedNonHeapMem))
      .append(indent).append(String.format("\"threadCount\": %d", this.threadCount))
      .append(indent).append("}")
      .toString();
  }
}
