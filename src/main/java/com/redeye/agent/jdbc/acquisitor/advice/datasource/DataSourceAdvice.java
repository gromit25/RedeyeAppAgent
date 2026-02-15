package com.redeye.agent.jdbc.acquisitor.advice.datasource;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class DataSourceAdvice {
	
	/**
	 * 
	 * 
	 * @param config
	 */
	@Advice.OnMethodExit
	public static void onExit() {
		System.out.println("*** DEBUG 100 in DataSourceAdvice: ");
	}
}
