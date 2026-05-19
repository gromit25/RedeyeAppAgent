package com.redeye.agent.domain.kafka.loader;

import com.redeye.agent.loader.APILoader;

/**
 * kafka 브로커 데이터 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaBrokerLoader implements APILoader {
	
	
	/** kafka 클라이언트 정보 서브패스 */
	private static String SUBPATH = "/api/kafka/broker";
	

	@Override
	public void load(String basePath, long startTime, long endTime) {
		// TODO Auto-generated method stub

	}
}
