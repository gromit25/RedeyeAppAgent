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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setString: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setInt: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setLong: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setFloat: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.execute: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeUpdate: ");
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
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.executeQuery: ");
		}
	}
}
