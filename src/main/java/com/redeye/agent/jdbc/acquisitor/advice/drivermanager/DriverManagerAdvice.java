package com.redeye.agent.jdbc.acquisitor.advice.drivermanager;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class DriverManagerAdvice {
	
	/**
	 * 
	 * 
	 * @param config
	 */
	@Advice.OnMethodExit
	public static void onExit() {
		System.out.println("*** DEBUG 100 in DriverManagerAdvice: ");
	}
}
