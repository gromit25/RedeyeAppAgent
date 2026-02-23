package com.redeye.agent.domain.jdbc.acquisitor.advice;

import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class ConnectionAdvice {
	
	
	/** */
	public static ThreadLocal<String> sql = ThreadLocal.withInitial(() -> "");
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static String getSql() {
		return sql.get();
	}
	
	/**
	 * 
	 */
	public static class prepareStatement {
		
		/**
		 * 
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {

			// sql 있는 경우 쿼리 설정
			if(args.length > 0) {
				sql.set(args[0].toString());
			}
		}
	}
	
	/**
	 * 
	 */
	public static class commit {
		
		/**
		 * 
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			System.out.println("*** DEBUG 100 in ConnectionAdvice.commit: ");
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
			System.out.println("*** DEBUG 100 in ConnectionAdvice.rollback: ");
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
			System.out.println("*** DEBUG 100 in ConnectionAdvice.close: ");
		}
	}
}
