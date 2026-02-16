package com.redeye.agent.jdbc.acquisitor.advice;

import net.bytebuddy.asm.Advice;

/**
 * DriverManager 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class DriverManagerAdvice {
	
	/**
	 * DriverManager.getConnection 어드바이스 클래스
	 */
	public static class getConnection {
		
		/**
		 * getConnection 호출 후 콜백 메소드
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in DriverManagerAdvice: ");
		}
	}
}
