package com.redeye.agent.domain.jdbc.acquisitor.advice;

import java.lang.reflect.Method;

import com.redeye.agent.domain.common.IntervalTimeAdvice;
import com.redeye.agent.domain.common.InvokeStatus;

import net.bytebuddy.asm.Advice;

/**
 * PreparedStatement 어드바이스 클래스<br>
 * 모든 속성 및 메소드는 public 이어야 함 - 스프링부트 클래스 로더 때문
 * 
 * @author jmsohn
 */
public class PreparedStatementAdvice extends IntervalTimeAdvice {
	

	/** 반복 호출 상태 */
	public static ThreadLocal<InvokeStatus> invokeStatus = ThreadLocal.withInitial(() -> InvokeStatus.INVOKE_EXIT);
	
	/** 쿼리 시작 시간 */
	public static ThreadLocal<Long> startTime = ThreadLocal.withInitial(() -> System.currentTimeMillis());
	
	
	/**
	 * 반복 호출 상태 설정
	 * 
	 * @param newStatus 설정할 상태
	 */
	public static void setInvokeStatus(InvokeStatus newStatus) {
		invokeStatus.set(newStatus);
	}
	
	/**
	 * 반복 호출 상태 반환
	 * 
	 * @return 반복 호출 상태
	 */
	public static InvokeStatus getInvokeStatus() {
		return invokeStatus.get();
	}
	
	/**
	 * 쿼리 시작 시간을 현재 시간으로 설정
	 */
	public static void resetStartTime() {
		startTime.set(System.currentTimeMillis());
	}
	
	/**
	 * 쿼리 시작 시간 반환
	 * 
	 * @return 쿼리 시작 시간
	 */
	public static long getStartTime() {
		return startTime.get();
	}
	
	/**
	 * 쿼리의 바인딩 변수 설정시 어드바이스 클래스
	 */
	public static class setValue {
		
		/**
		 * 바인딩 변수 설정 전 콜백
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
			
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.setValue: " + method);
			
			for(Object arg: args) {
				System.out.println("ARGS : " + arg);
			}
		}
	}

	/**
	 * 쿼리 실행시 어드바이스 클래스
	 */
	public static class execute {
		
		/**
		 * 쿼리 실행 전 콜백
		 * 
		 * @param method
		 * @param args
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
			
			setInvokeStatus(InvokeStatus.INVOKE_ENTER);
			resetStartTime();
			
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.execute on Enter: " + method);
		}
		
		/**
		 * 쿼리 실행 후 콜백
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			
			if(getInvokeStatus() != InvokeStatus.INVOKE_ENTER) {
				return;
			}
			
			setInvokeStatus(InvokeStatus.INVOKE_EXIT);
			
			long elapsedTime = System.currentTimeMillis() - getStartTime();
			
			System.out.println("*** DEBUG 100 in PreparedStatementAdvice.execute on Exit: " + elapsedTime);
			System.out.println("*** SQL: " + ConnectionAdvice.getSql());
		}
	} // End of execute class
}
