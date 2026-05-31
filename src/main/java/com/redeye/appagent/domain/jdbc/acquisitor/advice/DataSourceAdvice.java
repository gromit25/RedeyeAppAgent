package com.redeye.appagent.domain.jdbc.acquisitor.advice;

import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;

/**
 * DataSource 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class DataSourceAdvice {
	
	/**
	 * DataSource.getConnection 어드바이스 클래스
	 */
	public static class getConnection {
		
		/**
		 * getConnection 호출 전 콜백 메소드
		 * 
		 * @param method 호출된 실제 메소드
		 * @param args 메소드의 아규먼트
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
			
			System.out.println("*** DEBUG 100 in DataSourceAdvice: ");
			
			for(Object arg: args) {
				System.out.println("ARGS : " + arg);
			}
		}
	}
}
