package com.redeye.agent.domain.kafka.acquisitor.advice.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.redeye.agent.domain.kafka.Constants;

import net.bytebuddy.asm.Advice;

/**
 * Kafka 컨슈머 생성자 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerAdvice {

	
	/** 클라이언트(프로듀서/컨슈머) 별 클라이언트 아이디 (key: 프로듀서/컨슈머 객체 toString 값, value: 클라이언트 아이디 값) */
	public static Map<String, String> clientIdMap = new ConcurrentHashMap<>();
	

	/**
	 * KafkaConsumer 생성자 호출 후 콜백 메소드
	 * 
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		
		// 입력 값 검증
		if(consumer == null) {
			return;
		}
		
		// 클라이
		String clientId = ConsumerConfigAdvice.getClientId();
		if(clientId == null || clientId.equals(Constants.DEFAULT_CLIENT_ID) == true) {
			return;
		}

		// 클라이언트 아이디 맵에 저장
		clientIdMap.put(consumer.toString(), clientId);
	}
	
	/**
	 * 컨슈머 객체에 해당하는 클라이언트 아이디 반환
	 * 
	 * @param consumer 컨슈머 객체
	 * @return 클라이언트 아이디
	 */
	public static String getClientId(Object consumer) {
		
		if(consumer == null) {
			return null;
		}
		
		return clientIdMap.get(consumer.toString());
	}
}
