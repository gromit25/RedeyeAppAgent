package com.redeye.agent.util;

import com.redeye.agent.Config;

/**
 * 
 * 
 * @author jmsohn
 */
public class LogUtil {
	
	/**
	 * 
	 * 
	 * @param message
	 */
	public static void log(String message) {
		
		if(Config.LOG_USE_YN.getValue().equals("Y") == true) {
			System.out.println(message);
		}
	}
}
