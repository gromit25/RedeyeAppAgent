package com.redeye.agent.domain.jdbc.acquisitor.advice;

import com.redeye.agent.domain.common.InvokeStatus;

/**
 * JDBC 실행 관련 컨텍스트 클래스
 * 
 * @author jmsohn
 */
public class ContextHolder {
	
	
	/** Statement, PreparedStatement의 sql 문 저장용 변수 */
	private static ThreadLocal<String> sql = ThreadLocal.withInitial(() -> "");
		
	/** 쿼리 시작 시간 */
	private static ThreadLocal<Long> startTime = ThreadLocal.withInitial(() -> System.currentTimeMillis());

	/** 반복 호출 상태 */
	private static ThreadLocal<InvokeStatus> invokeStatus = ThreadLocal.withInitial(() -> InvokeStatus.INVOKE_EXIT);


	/**
	 * 스레드 로컬 변수에 sql 설정
	 * 
	 * @param sql 설정할 sql
	 */
	public static void setSql(String sql) {
		
		if(sql == null) {
			throw new IllegalArgumentException("'sql' is null.");
		}
		
		ContextHolder.sql.set(sql);
	}
	
	/**
	 * 스레드 로컬 변수에 설정된 sql 문 반환
	 * 
	 * @return sql 문
	 */
	public static String getSql() {
		return sql.get();
	}
	
	/**
	 * 현재 시간으로 쿼리 시작 시간으로 설정
	 */
	public static void setStartTime() {
		setStartTime(System.currentTimeMillis());
	}
	
	/**
	 * 주어진 시간으로 쿼리 시작 시간 설정
	 * 
	 * @param startTime 설정할 쿼리 시작 시간
	 */
	public static void setStartTime(long startTime) {
		ContextHolder.startTime.set(startTime);
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
	 * 반복 호출 상태 설정
	 * 
	 * @param invokeStatus 반복 호출 상태
	 */
	public static void setInvokeStatus(InvokeStatus invokeStatus) {
		
		if(invokeStatus == null) {
			throw new IllegalArgumentException("'invokeStatus' is null.");
		}
		
		ContextHolder.invokeStatus.set(invokeStatus);
	}
	
	/**
	 * 반복 호출 상태 반환
	 * 
	 * @return 반복 호출 상태
	 */
	public static InvokeStatus getInvokeStatus() {
		return invokeStatus.get();
	}
}
