package com.redeye.agent.kafka;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

import com.redeye.agent.Context;
import com.redeye.agent.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.kafka.acquisitor.advice.broker.KafkaConfigAdvice;
import com.redeye.agent.kafka.acquisitor.advice.broker.RequestContextAdvice;
import com.redeye.agent.kafka.acquisitor.advice.broker.SaslServerAuthenticatorAuthenticateAdvice;
import com.redeye.agent.kafka.acquisitor.advice.consumer.ConsumerConfigAdvice;
import com.redeye.agent.kafka.acquisitor.advice.consumer.KafkaConsumerAdvice;
import com.redeye.agent.kafka.acquisitor.advice.consumer.KafkaConsumerCommitAsyncAdvice;
import com.redeye.agent.kafka.acquisitor.advice.consumer.KafkaConsumerCommitSyncAdvice;
import com.redeye.agent.kafka.acquisitor.advice.consumer.KafkaConsumerPollAdvice;
import com.redeye.agent.kafka.acquisitor.advice.provider.ProducerConfigAdvice;
import com.redeye.agent.kafka.exporter.service.KafkaBrokerController;
import com.redeye.agent.kafka.exporter.service.KafkaClientController;
import com.redeye.agent.kafka.exporter.service.KafkaConfigController;
import com.redeye.agent.kafka.exporter.service.KafkaMetricsController;
import com.redeye.agent.loader.APILoader;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Kafka 컨텍스트 클래스
 * 
 * @author jmsohn
 */
public class KafkaContext implements Context {
	
	@Override
	public void init() {
		KafkaAcquisitor.init();
	}
	
	@Override
	public void addTransformer(Instrumentation inst) {
		
		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// ---- 브로커 어드바이스 설정

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
		
		// Kafka 인증 메소드 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.common.security.authenticator.SaslServerAuthenticator"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
    					.method(ElementMatchers.named("authenticate"))
    					.intercept(Advice.to(SaslServerAuthenticatorAuthenticateAdvice.class));
				}
			)
			.installOn(inst);


		// ---- 프로듀서 관련 어드바이스 설정
		
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
		
		
		// ---- 컨슈머 관련 어드바이스 설정
		
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

	@Override
	public List<Object> getWebControllerList() {
		
		return List.of(
			new KafkaBrokerController(),
			new KafkaClientController(),
			new KafkaConfigController(),
			new KafkaMetricsController()
		);
	}

	@Override
	public List<APILoader> getAPILoaderList() {
		
		return List.of(
			new APILoader() {

				@Override
				public void load(String basePath, long startTime, long endTime) {
					
					ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
					int threadCount = threadBean.getThreadCount();
					
					System.out.println("### DEBUG LOADER: " + basePath + ", " + startTime + ", " + threadCount);
				}
			}
		);
	}
}
