package com.redeye.agent.domain.jdbc.acquisitor.advice;

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
	public static class setString {
		
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
	public static class setInt {
		
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
	public static class setLong {
		
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
	public static class setFloat {
		
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
	public static class execute {
		
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
	public static class executeUpdate {
		
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
	public static class executeQuery {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeQueryAdvice: ");
		}
	}
}
