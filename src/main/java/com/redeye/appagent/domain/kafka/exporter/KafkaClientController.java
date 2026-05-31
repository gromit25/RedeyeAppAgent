package com.redeye.appagent.domain.kafka.exporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redeye.appagent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.appagent.util.JSONUtil;
import com.redeye.appagent.util.http.service.annotation.Controller;
import com.redeye.appagent.util.http.service.annotation.RequestHandler;

/**
 * Kafka 클라이언트 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/client")
public class KafkaClientController {

	/**
	 * Kafka 클라이언트 아이디 목록 반환
	 * 
	 * @return Kafka 클라이언트 아이디 목록
	 */
	@RequestHandler
	public String getClientIdList() {
		
		Map<String, Set<String>> clientIdMap = new HashMap<>();
		
		clientIdMap.put("producer", KafkaAcquisitor.getProducerClientIdSet());
		clientIdMap.put("consumer", KafkaAcquisitor.getConsumerClientIdSet());
		
		return JSONUtil.toJSON(clientIdMap);
	}
	
	/**
	 * 클라이언트 설정 정보 반환
	 * 
	 * @param pathParamList 패스 파라미터 목록
	 * @return 클라이언트 설정 정보
	 */
	@RequestHandler(path = "/*/config")
	public String getClientConfigMap(List<String> pathParamList) {
		
		return JSONUtil.toJSON(
			KafkaAcquisitor.getConfigMap(
				pathParamList.get(0)	// Client Id
			)
		);
	}
	
	/**
	 * 클라이언트 성능 정보 반환
	 * 
	 * @param pathParamList 패스 파라미터 목록
	 * @return 클라이언트 성능 정보
	 */
	@RequestHandler(path = "/*/metrics")
	public String getClientMetrics(List<String> pathParamList) throws Exception {
		
		return JSONUtil.toJSON(
			KafkaAcquisitor.getMetrics(
				pathParamList.get(0)	// Client Id
			)
		);
	}
}
