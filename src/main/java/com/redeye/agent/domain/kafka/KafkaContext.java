package com.redeye.agent.domain.kafka;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

import com.redeye.agent.Context;
import com.redeye.agent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.domain.kafka.acquisitor.advice.broker.KafkaConfigAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.broker.RequestContextAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.broker.SaslServerAuthenticatorAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.consumer.ConsumerConfigAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.consumer.KafkaConsumerAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.provider.ProducerConfigAdvice;
import com.redeye.agent.domain.kafka.exporter.service.KafkaBrokerController;
import com.redeye.agent.domain.kafka.exporter.service.KafkaClientController;
import com.redeye.agent.domain.kafka.exporter.service.KafkaConfigController;
import com.redeye.agent.domain.kafka.exporter.service.KafkaMetricsController;
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
						.intercept(Advice.to(KafkaConfigAdvice.constructor.class));
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
						.intercept(Advice.to(RequestContextAdvice.constructor.class));
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
    					.intercept(Advice.to(SaslServerAuthenticatorAdvice.authenticate.class));
				}
			)
			.installOn(inst);

		
		// ---- 프로듀서 관련 어드바이스 설정
		
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.producer.ProducerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ProducerConfigAdvice.constructor.class));
				}
			)
        	.installOn(inst);
		
		
		// ---- 컨슈머 관련 어드바이스 설정
		
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.ConsumerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ConsumerConfigAdvice.constructor.class));
				}
			)
			.installOn(inst);
		
		// KafkaConsumer의 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerAdvice.constructor.class))
						.visit(
							Advice
								.to(KafkaConsumerAdvice.poll.class)
								.on(
									ElementMatchers
										.named("poll")
										.and(ElementMatchers.takesArguments(1))
								)
						)
						.visit(
							Advice
								.to(KafkaConsumerAdvice.commitSync.class)
								.on(ElementMatchers.named("commitSync"))
						)
						.visit(
							Advice
								.to(KafkaConsumerAdvice.commitAsync.class)
								.on(ElementMatchers.named("commitAsync"))
						)
						;
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
