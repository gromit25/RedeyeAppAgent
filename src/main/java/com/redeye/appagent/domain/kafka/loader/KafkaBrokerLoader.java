package com.redeye.appagent.domain.kafka.loader;

import com.redeye.appagent.loader.APILoader;
import com.redeye.appagent.loader.entity.APIContextDTO;

/**
 * kafka 브로커 데이터 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaBrokerLoader implements APILoader {
	
	
	/** kafka 클라이언트 정보 서브패스 */
	private static String SUBPATH = "/api/kafka/broker";
	

	@Override
	public void load(APIContextDTO context) {
		// TODO Auto-generated method stub
	}
}
