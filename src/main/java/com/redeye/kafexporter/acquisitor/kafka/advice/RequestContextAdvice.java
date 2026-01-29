package com.redeye.kafexporter.acquisitor.kafka.advice;

import java.net.InetAddress;

import net.bytebuddy.asm.Advice;

/**
 * 브로커에서 클라이언트 접속 IP 획득을 위한 어드바이스 클래스
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
