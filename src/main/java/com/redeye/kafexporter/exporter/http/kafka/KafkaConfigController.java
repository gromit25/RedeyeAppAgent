package com.redeye.kafexporter.exporter.http.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;

/**
 * Kafka 설정 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/config")
public class KafkaConfigController {

	/**
	 * Kafka 전체 설정 정보 반환
	 * 
	 * @return Kafka 전체 설정 정보
	 */
	@RequestHandler
	public String getConfigMap() throws Exception {
		
		// 설정 값 메시지 생성
		Map<String, Object> configMap = new HashMap<>();

		configMap.put("broker", KafkaAcquisitor.getBrokerConfigMap());
		configMap.put("producer", KafkaAcquisitor.getProducerConfigMap());
		configMap.put("consumer", KafkaAcquisitor.getConsumerConfigMap());
		
		return JSONUtil.toJSON(configMap);
	}
	
	/**
	 * 브로커 설정 정보 반환
	 * 
	 * @return 브로커 설정 정보
	 */
	@RequestHandler(path = "/broker")
	public static String getBrokerConfigMap(List<String> pathParamList) throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getBrokerConfigMap());
	}
	
	/**
	 * 프로듀서 설정 정보 반환
	 * 
	 * @return 프로듀서 설정 정보
	 */
	@RequestHandler(path = "/producer")
	public static String getProducerConfigMap(List<String> pathParamList) throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getProducerConfigMap());
	}

	/**
	 * 컨슈머 설정 정보 반환
	 * 
	 * @return 컨슈머 설정 정보
	 */
	@RequestHandler(path = "/consumer")
	public static String getConsumerConfigMap(List<String> pathParamList) throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getConsumerConfigMap());
	}
}
