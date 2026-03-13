package com.redeye.agent.domain.kafka.exporter.loader;

import com.redeye.agent.loader.APILoader;

/**
 * kafka 통계 데이터 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaStatLoader implements APILoader {
	
	
	/** kafka 통계 정보 서브패스 */
	private static String SUBPATH = "/api/kafka";


	@Override
	public void load(String basePath, long startTime, long endTime) {
		// TODO Auto-generated method stub

	}
}
