package com.redeye.appagent.util;

import com.redeye.appagent.Config;

/**
 * 로그 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class LogUtil {
	
	/**
	 * 로깅 수행
	 * 
	 * @param message 메시지
	 */
	public static void log(String message) {
		
		if(Config.LOG_USE_YN.getValue().equals("Y") == false) {
			return;
		}

		System.out.println(message);
	}

	/**
	 * 예외 로깅 수행
	 * 
	 * @param ex 에외 객체
	 */
	public static void log(Exception ex) {
		
		if(Config.LOG_USE_YN.getValue().equals("Y") == false) {
			return;
		}

		ex.printStackTrace();
	}
}
