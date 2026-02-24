package com.redeye.agent.domain.jdbc.acquisitor.advice;

import com.redeye.agent.domain.common.ElapsedTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class StatementAdvice extends ElapsedTimeAdvice {

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
