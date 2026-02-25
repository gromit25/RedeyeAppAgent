package com.redeye.agent.domain.common;

import com.redeye.agent.Config;
import com.redeye.agent.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 어드바이스 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class AdviceUtil {

	
	/**
	 * Line of code
	 */
	@Data
	@AllArgsConstructor
	public static class Loc {
		
		/** 클래스 명 */
		private final String className;
		
		/** 메소드 명 */
		private final String methodName;
		
		/** 라인 넘버 */
		private final int lineNumber;
		
		@Override
		public String toString() {
			
			return new StringBuilder()
				.append(this.className).append(":")
				.append(this.methodName).append(":")
				.append(this.lineNumber)
				.toString();
		}
	}
	
	
	/**
	 * 현재 스레드의 Loc 정보를 반환
	 * 
	 * @return 현재 스레드의 Loc 정보
	 */
	public static Loc getLoc() {
		
		// 어플리케이션 패키지명 획득
		String appPackage = Config.APP_PACKAGE.getValue();
		
		// 스택을 검사하여 Loc 정보를 생성하여 반환
		for(StackTraceElement stack: Thread.currentThread().getStackTrace()) {
			
			String className = stack.getClassName();
			
			// 어플리케이션 패키지가 설정되지 않은 경우와
			// 클래스 명이 어플리케이션 패키지에 해당할 경우
			// 현재 Loc 정보를 생성하여 반환
			if(
				StringUtil.isBlank(appPackage) == true
				|| className.startsWith(appPackage) == true
			) {
				return new Loc(
					className,
					stack.getMethodName(),
					stack.getLineNumber()
				);
			}
		}
		
		// 매칭되는 Loc 정보가 없을 경우 null 반환
		return null;
	}
}
