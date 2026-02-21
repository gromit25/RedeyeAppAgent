package com.redeye.agent.domain.kafka.exporter.service;

import java.util.List;

import com.redeye.agent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.http.service.annotation.Controller;
import com.redeye.agent.util.http.service.annotation.RequestHandler;

/**
 * 카프카 브로커 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/broker")
public class KafkaBrokerController {
	
	/**
	 * 카프카 브로커 설정 반환
	 * 
	 * @return 카프카 브로커 설정 
	 */
	@RequestHandler(path = "/config")
	public String getConifg() {
		return JSONUtil.toJSON(KafkaAcquisitor.getBrokerConfigMap());
	}

	/**
	 * 카프카 브로커 성능 정보 반환 
	 * 
	 * @return 카프카 브로커 성능 정보
	 */
	@RequestHandler(path = "/metrics")
	public String getMetrics() throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.getBrokerMetrics());
	}

	/**
	 * 카프카 모든 컨슈머 그룹 래그 반환
	 * 
	 * @return 카프카 모든 컨슈머 그룹 래그
	 */
	@RequestHandler(path = "/group/lag")
	public String getAllLag() {
		return "{}";
	}
	
	/**
	 * 카프카 특정 컨슈머 그룹 래그 반환
	 * 
	 * @param pathParamList 요청 패스 파라미터 목록
	 * @return 카프카 특정 컨슈머 그룹 래그
	 */
	@RequestHandler(path = "/group/*/lag")
	public String getGroupLag(List<String> pathParamList) {
		return "{}";
	}
	
	/**
	 * 접속된 모든 클라이언트 목록 반환
	 * 
	 * @return 접속된 모든 클라이언트 목록
	 */
	@RequestHandler(path = "/client")
	public String getAllClientConn() {
		return JSONUtil.toJSON(KafkaAcquisitor.getClientConnMap());
	}
	
//	@RequestHandler(path = "/auth/fail")
//	public String getAuthFail() {
//	}
}
