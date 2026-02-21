package com.redeye.agent.domain.kafka.exporter.service;

import java.util.HashMap;
import java.util.Map;

import com.redeye.agent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.http.service.annotation.Controller;
import com.redeye.agent.util.http.service.annotation.RequestHandler;

/**
 * Kafka 성능 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/metrics")
public class KafkaMetricsController {
	
	/**
	 * Kafka 전체 성능 정보 반환
	 * 
	 * @return Kafka 전체 성능 정보
	 */
	@RequestHandler
	public static String getMetrics() throws Exception {
		
		Map<String, Object> metricsMap = new HashMap<>();
		
		metricsMap.put("system", KafkaAcquisitor.getSystemMetrics());
		metricsMap.put("broker", KafkaAcquisitor.getBrokerMetrics());
		metricsMap.put("producer", KafkaAcquisitor.getProducerMetrics("*"));
		metricsMap.put("consumer", KafkaAcquisitor.getConsumerMetrics("*"));
		
		return JSONUtil.toJSON(metricsMap);
	}
	
	/**
	 * 브로커 성능 정보 반환
	 * 
	 * @return 브로커 성능 정보
	 */
	@RequestHandler(path = "/broker")
	public static String getBrokerMetrics() throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getBrokerMetrics());
	}
	
	/**
	 * 컨슈머 래그 성능 정보 반환
	 * 
	 * @return 컨슈머 래그 성능 정보
	 */
	@RequestHandler(path = "/broker/lag")
	public static String getConsumerLag() throws Exception {
		return "{}";
//		return JSONUtil.toJSON(KafkaAcquisitor.getConsumerLag());
	}
	
	/**
	 * 프로듀서 성능 정보 반환
	 * 
	 * @return 프로듀서 성능 정보
	 */
	@RequestHandler(path = "/producer")
	public static String getProducerMetrics() throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getProducerMetrics("*"));
	}
	
	/**
	 * 컨슈머 성능 정보 반환
	 * 
	 * @return 컨슈머 성능 정보
	 */
	@RequestHandler(path = "/consumer")
	public static String getConsumerMetrics() throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getConsumerMetrics("*"));
	}
}
