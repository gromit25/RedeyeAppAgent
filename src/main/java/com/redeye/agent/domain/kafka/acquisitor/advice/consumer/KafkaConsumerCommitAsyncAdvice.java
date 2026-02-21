package com.redeye.agent.domain.kafka.acquisitor.advice.consumer;

import com.redeye.agent.domain.kafka.acquisitor.advice.ClientTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer commitAsync 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerCommitAsyncAdvice extends ClientTimeAdvice {
	
	/**
	 * KafkaConsumer.commitAsyncn 호출 후 콜백 메소드
	 * 
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		sendCurTime(consumer);
	}
}
