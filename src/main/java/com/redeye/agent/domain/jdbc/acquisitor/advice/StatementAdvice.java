package com.redeye.agent.domain.jdbc.acquisitor.advice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class StatementAdvice {

	/**
	 * 
	 */
	public static class executeQuery {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in StatementAdvice.executeQueryAdvice: ");
		}
	}
}
