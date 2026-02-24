package com.redeye.agent.domain.jdbc.acquisitor.advice;

import com.redeye.agent.domain.common.IntervalTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class StatementAdvice extends IntervalTimeAdvice {

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
