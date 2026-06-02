package com.redeye.appagent.domain.metrics.acquisitor;

/**
 *
 *
 * @author jmsohn
 */
public class MetricAcquisitor {

  
  /** */
  private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

  /** */
  private static final MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

  /** */
  private static final MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
  
  /** */
  private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
  

  // ----------------------------------
  
  /**
   *
   *
   * @return
   */
  public static MetricDTO getMetric() {
    
    return new MetricDTO(
      getCPUUsage(),
      getMaxHeapMem(),
      getUsedHeapMem(),
      getMaxNonHeapMem(),
      getUsedNonHeapMem(),
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
    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  }

  // ----------------------------------

  /**
   *
   *
   * @return
   */
  public static long getUsedHeapMem() {
    return heapUsage.getUsed();
  }

  /**
   *
   *
   * @return
   */
  public static long getMaxHeapMem() {
    return heapUsage.getMax();
  }

  /**
   *
   *
   * @return
   */
  public static long getUsedNonHeapMem() {
    return nonHeapUsage.getUsed();
  }

  /**
   *
   *
   * @return
   */
  public static long getMaxNonHeapMem() {
    return nonHeapUsage.getMax();
  }

  // ----------------------------------

  /**
   *
   *
   * @return 스레드 개수
   */
  public static int getThreadCount() {
    return threadBean.getThreadCount();
  }
}
