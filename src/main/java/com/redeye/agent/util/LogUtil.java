package com.redeye.agent.util;

import com.redeye.agent.Config;

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
		
		if(Config.LOG_USE_YN.getValue().equals("Y") == true) {
			System.out.println(message);
		}
	}
}
