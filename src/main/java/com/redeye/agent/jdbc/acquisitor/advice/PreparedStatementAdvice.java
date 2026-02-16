package com.redeye.agent.jdbc.acquisitor.advice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class PreparedStatementAdvice {
	
	/**
	 * 
	 */
	public static class setStringAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setStringAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class setIntAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setIntAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class setLongAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setLongAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class setFloatAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setFloatAdvice: ");
		}
	}

	/**
	 * 
	 */
	public static class setDouble {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setDouble: ");
		}
	}
	
	/**
	 * 
	 */
	public static class executeAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeAdvice: ");
		}
	}
	
	/**
	 * 
	 */
	public static class executeUpdateAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeUpdateAdvice: ");
		}
	}
	
	/**
	 * 
	 */
	public static class executeQueryAdvice {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeQueryAdvice: ");
		}
	}
}
