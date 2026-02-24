package com.redeye.agent.domain.kafka.acquisitor.advice.consumer;

import com.redeye.agent.domain.common.IntervalTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer commitAsync 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerCommitAsyncAdvice extends IntervalTimeAdvice {
	
	/**
	 * KafkaConsumer.commitAsyncn 호출 후 콜백 메소드
	 * 
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		
		// 클라이언트 아이디 획득
		String clientId = KafkaConsumerAdvice.getClientId(consumer);
		if(clientId == null) {
			return;
		}
		
		// 처리자에게 현재 시간 정보 전송
		put(clientId);
	}
}
