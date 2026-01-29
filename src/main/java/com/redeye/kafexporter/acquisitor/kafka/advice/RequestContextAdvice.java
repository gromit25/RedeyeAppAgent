package com.redeye.kafexporter.acuisitor.kafka.advice;

import java.net.InetAddress;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class RequestContextAdvice {

	@Advice.OnMethodExit
	public static void onExit(@Advice.AllArguments Object[] params) {
		
		for(Object param: params) {
			if(param instanceof InetAddress) {
				InetAddress addr = (InetAddress)param;
				System.out.println("### DEBUG Client IP: " + addr.getHostAddress());
			}
		}
	}
}
