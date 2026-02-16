package com.redeye.agent.jdbc.acquisitor.advice;

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
	public static class getConnectionAdvice {
		
		/**
		 * getConnection 호출 후 콜백 메소드
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in DataSourceAdvice: ");
		}
	}
}
