package com.redeye.agent.jdbc;

import java.lang.instrument.Instrumentation;
import java.util.List;

import javax.sql.DataSource;

import com.redeye.agent.Context;
import com.redeye.agent.jdbc.acquisitor.advice.DataSourceAdvice;
import com.redeye.agent.jdbc.acquisitor.advice.DriverManagerAdvice;
import com.redeye.agent.loader.APILoader;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 
 * 
 * @author jmsohn
 */
public class JDBCContext implements Context {

	@Override
	public void init() {
	}

	@Override
	public void addTransformer(Instrumentation inst) {
		
		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// 
		new AgentBuilder.Default()
			.with(AgentBuilder.Listener.StreamWriting.toSystemOut())	// Debug 용
			.type(ElementMatchers.named("java.sql.DriverManager"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.method(
							ElementMatchers.named("getConnection")
							.and(
								ElementMatchers.takesArguments(1)	// url
								.or(ElementMatchers.takesArguments(3))	// url, id, pwd
							)
						)
						.intercept(Advice.to(DriverManagerAdvice.getConnection.class));
				}
			)
        	.installOn(inst);
		
		// 
		new AgentBuilder.Default()
			.with(AgentBuilder.Listener.StreamWriting.toSystemOut())	// Debug 용
			.type(ElementMatchers.isSubTypeOf(DataSource.class))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.method(
							ElementMatchers.named("getConnection")
							.and(
								ElementMatchers.takesArguments(0)
								.or(ElementMatchers.takesArguments(2))
							)
						)
						.intercept(Advice.to(DataSourceAdvice.getConnection.class));
				}
			)
        	.installOn(inst);
	}

	@Override
	public List<Object> getWebControllerList() {
		return List.of();
	}

	@Override
	public List<APILoader> getAPILoaderList() {
		return List.of();
	}
}
