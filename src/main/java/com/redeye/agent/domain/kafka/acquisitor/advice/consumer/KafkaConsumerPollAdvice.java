package com.redeye.agent.domain.kafka.acquisitor.advice.consumer;

import com.redeye.agent.domain.common.IntervalTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer poll 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerPollAdvice extends IntervalTimeAdvice {

	/**
	 * KafkaConsumer.poll 수행 후
	 *
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		sendCurTime(consumer);
	}
}
