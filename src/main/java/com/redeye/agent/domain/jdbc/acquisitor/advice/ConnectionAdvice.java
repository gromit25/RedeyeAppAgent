package com.redeye.agent.domain.jdbc.acquisitor.advice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class ConnectionAdvice {
	
	/**
	 * 
	 */
	public static class commit {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in ConnectionAdvice.commitAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class rollback {

		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in ConnectionAdvice.rollbackAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class close {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in ConnectionAdvice.closeAdvice: ");
		}
	}
}
