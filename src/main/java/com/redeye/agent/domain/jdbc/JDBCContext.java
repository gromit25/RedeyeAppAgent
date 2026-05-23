package com.redeye.agent.domain.jdbc;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import com.redeye.agent.Context;
import com.redeye.agent.domain.jdbc.acquisitor.JDBCAcquisitor;
import com.redeye.agent.domain.jdbc.acquisitor.advice.ConnectionAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.DataSourceAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.DriverManagerAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.PreparedStatementAdvice;
import com.redeye.agent.domain.jdbc.loader.SqlStatLoader;
import com.redeye.agent.loader.APILoader;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * JDBC 컨텍스트 클래스
 * 
 * @author jmsohn
 */
public class JDBCContext implements Context {

	@Override
	public void init() {
		JDBCAcquisitor.init();
	}

	@Override
	public void addTransformer(Instrumentation inst) {
		
		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// DriverManager 클래스 관련 변환
		new AgentBuilder.Default()
			.type(ElementMatchers.named("java.sql.DriverManager"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.method(
							ElementMatchers.named("getConnection")
						)
						.intercept(Advice.to(DriverManagerAdvice.getConnection.class));
				}
			)
        	.installOn(inst);
		
		// DataSource 클래스 관련 변환
		new AgentBuilder.Default()
			.type(ElementMatchers.isSubTypeOf(DataSource.class))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.method(
							ElementMatchers.named("getConnection")
						)
						.intercept(Advice.to(DataSourceAdvice.getConnection.class));
				}
			)
        	.installOn(inst);
		
		// Connection 관련 변환
		new AgentBuilder.Default()
			.type(ElementMatchers.isSubTypeOf(Connection.class))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
							
						// prepareStatement
						.method(
							ElementMatchers.named("prepareStatement")
						)
						.intercept(Advice.to(ConnectionAdvice.prepareStatement.class))
						
						// commit
						.method(
							ElementMatchers.named("commit")
						)
						.intercept(Advice.to(ConnectionAdvice.commit.class))
					
						// rollback
						.method(
							ElementMatchers.named("rollback")
						)
						.intercept(Advice.to(ConnectionAdvice.rollback.class))
						
						// close
						.method(
							ElementMatchers.named("close")
						)
						.intercept(Advice.to(ConnectionAdvice.close.class))
						;
				}
			)
        	.installOn(inst);
		
		// PreparedStatement 클래스 관련 변환
		new AgentBuilder.Default()
			.type(ElementMatchers.isSubTypeOf(PreparedStatement.class))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
							
						// 파라미터 바인딩
						.method(
							ElementMatchers.namedOneOf(
								"setString",
								"setInt",
								"setLong",
								"setFloat",
								"setDouble"
							)
						)
						.intercept(Advice.to(PreparedStatementAdvice.setValue.class))
						
						// 쿼리 수행
						.method(
							ElementMatchers.namedOneOf(
								"execute",
								"executeUpdate",
								"executeQuery"
							)
						)
						.intercept(Advice.to(PreparedStatementAdvice.execute.class))
						;
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
		return List.of(new SqlStatLoader());
	}
}
