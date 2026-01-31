package com.redeye.agent.kafka.acquisitor;

import java.lang.instrument.Instrumentation;

import com.redeye.agent.kafka.acquisitor.advice.ConsumerConfigAdvice;
import com.redeye.agent.kafka.acquisitor.advice.KafkaConfigAdvice;
import com.redeye.agent.kafka.acquisitor.advice.KafkaConsumerAdvice;
import com.redeye.agent.kafka.acquisitor.advice.KafkaConsumerCommitAsyncAdvice;
import com.redeye.agent.kafka.acquisitor.advice.KafkaConsumerCommitSyncAdvice;
import com.redeye.agent.kafka.acquisitor.advice.KafkaConsumerPollAdvice;
import com.redeye.agent.kafka.acquisitor.advice.ProducerConfigAdvice;
import com.redeye.agent.kafka.acquisitor.advice.RequestContextAdvice;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * kafka 메소드 변환 클래스
 * 
 * @author jmsohn
 */
public class KafkaTransformer {
	
	/**
	 * 메소드 인터셉터 등록 - 바이트 코드 변환
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	public static void addKafkaTransformer(Instrumentation inst) {

		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}

		// Kafka KafkaConfig 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("kafka.server.KafkaConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConfigAdvice.class));
				}
			)
        	.installOn(inst);

		// Kafka 요청 컨택스트 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.common.requests.RequestContext"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(RequestContextAdvice.class));
				}
			)
			.installOn(inst);
		
		// Kafka ProducerConfig 생성자 호출 어드바이스 설정
		ProducerConfigAdvice.init(KafkaAcquisitor.producerConfigMap);
			
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.producer.ProducerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ProducerConfigAdvice.class));
				}
			)
        	.installOn(inst);
		
		// Kafka ConsumerConfig 생성자 호출 어드바이스 설정
		ConsumerConfigAdvice.init(KafkaAcquisitor.consumerConfigMap);
		
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.ConsumerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ConsumerConfigAdvice.class));
				}
			)
			.installOn(inst);
		
		// KafkaConsumer의 생성자 호출 어드바이스 설정
		
		// 초기화
		KafkaConsumerPollAdvice.init(KafkaAcquisitor.poolTimeStatDaemon);
		KafkaConsumerCommitSyncAdvice.init(KafkaAcquisitor.commitSyncTimeStatDaemon);
		KafkaConsumerCommitAsyncAdvice.init(KafkaAcquisitor.commitAsyncTimeStatDaemon);

		// 설정 수행
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerAdvice.class))
						.visit(
							Advice
								.to(KafkaConsumerPollAdvice.class)
								.on(
									ElementMatchers
										.named("poll")
										.and(ElementMatchers.takesArguments(1))
								)
						)
						.visit(
							Advice
								.to(KafkaConsumerCommitAsyncAdvice.class)
								.on(ElementMatchers.named("commitAsync"))
						)
						.visit(
							Advice
								.to(KafkaConsumerCommitSyncAdvice.class)
								.on(ElementMatchers.named("commitSync"))
						);
				}
			)
			.installOn(inst);
		
		// 스프링부트의 KafkaConsumer의 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.springframework.kafka.core.DefaultKafkaConsumerFactory$ExtendedKafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerAdvice.class));
				}
			)
			.installOn(inst);
	}
}
