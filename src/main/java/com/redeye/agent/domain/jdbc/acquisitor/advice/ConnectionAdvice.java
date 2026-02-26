package com.redeye.agent.domain.jdbc.acquisitor.advice;

import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;

/**
 * Connection 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class ConnectionAdvice {
	
	
	/** PreparedStatement 생성시 sql 문 저장용 변수 */
	public static ThreadLocal<String> sql = ThreadLocal.withInitial(() -> "");
	
	
	/**
	 * PreparedStatement 생성시 sql 문 반환
	 * 
	 * @return
	 */
	public static String getSql() {
		return sql.get();
	}
	
	/**
	 * Connection.prepareStatement 어드바이스 클래스
	 */
	public static class prepareStatement {

		/**
		 * prepareStatement 호출 전 콜백 메소드
		 * 
		 * @param method 호출된 실제 메소드
		 * @param args 메소드의 아규먼트
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {

			// sql 있는 경우 쿼리 설정
			if(args.length == 0) {
				return;
			}
			
			sql.set(args[0].toString());
		}
	}
	
	/**
	 * Connection.commit 어드바이스 클래스
	 */
	public static class commit {
		
		/**
		 * commit 호출 후 콜백 메소드
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			// TODO
		}
	}

	/**
	 * Connection.rollback 어드바이스 클래스
	 */
	public static class rollback {

		/**
		 * rollback 호출 후 콜백 메소드
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			// TODO
		}
	}

	/**
	 * Connection.close 어드바이스 클래스
	 */
	public static class close {
		
		/**
		 * close 호출 후 콜백 메소드
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			// TODO
		}
	}
}
