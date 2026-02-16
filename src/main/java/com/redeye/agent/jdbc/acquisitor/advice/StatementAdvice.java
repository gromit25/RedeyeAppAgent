package com.redeye.agent.jdbc.acquisitor.advice;

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
	public static class executeQueryAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in StatementAdvice.executeQueryAdvice: ");
		}
	}
}
