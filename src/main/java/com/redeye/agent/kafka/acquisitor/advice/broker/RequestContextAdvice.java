package com.redeye.agent.kafka.acquisitor.advice.broker;

import java.lang.reflect.Method;
import java.net.InetAddress;

import com.redeye.agent.kafka.acquisitor.KafkaAcquisitor;

import net.bytebuddy.asm.Advice;

/**
 * 브로커에서 클라이언트 접속 IP 획득을 위한 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class RequestContextAdvice {

	/**
	 * RequestContext 객체 생성 후 콜백되는 메소드
	 * 
	 * @param requestContext kafka의 클라이언트 접속 정보 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object requestContext) {
		
		// 입력값 검증
		if(requestContext == null) {
			System.out.println("'requestContext' is null.");
			return;
		}
		
		try {
			
			// 클라이언트 아이피 획득
			Method clientAddressMethod = requestContext.getClass().getMethod("clientAddress");
			if(clientAddressMethod == null) {
				return;
			}

			InetAddress clientAddr = (InetAddress)clientAddressMethod.invoke(requestContext);

			// 클라이언트 아이디 획득
			Method clientIdMethod = requestContext.getClass().getMethod("clientId");
			if(clientIdMethod == null) {
				return;
			}
			
			String clientId = clientIdMethod.invoke(requestContext).toString();
			
			// 클라이언트 아이피:아이디 정보 저장
			KafkaAcquisitor.putClientConn(clientAddr.getHostAddress(), clientId);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
