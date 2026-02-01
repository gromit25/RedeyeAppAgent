package com.redeye.agent.kafka.acquisitor.advice.broker;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;

import javax.security.sasl.SaslServer;

import net.bytebuddy.asm.Advice;

/**
 * 카프카 인증 객체의 인증 메소드 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class SaslServerAuthenticatorAuthenticateAdvice {

	/**
	 * 인증 메소드 수행 후 콜백 메소드<br>
	 * 인증 실패시 실패 결과를 저장
	 * 
	 * @param throwable 인증 예외 객체
	 * @param transportLayer 클라이언트 접속 정보 객체
	 * @param saslServer 
	 */
	@Advice.OnMethodExit(onThrowable = Throwable.class)
	public static void onExit(
		@Advice.Thrown Throwable throwable,
		@Advice.FieldValue("transportLayer") Object transportLayer,
		@Advice.FieldValue("saslServer") SaslServer saslServer,
		@Advice.FieldValue(value = "header", readOnly = false) Object header
	) {
		
		// 인증 성공시 반환
		if(throwable == null) {
			return;
		}
		
		// 인증 실패시 기록 저장
		
		// 아이피 추출
		String ip = getIp(transportLayer);
		
		// 인증 아이디 추출
		String authId = getAuthId(saslServer);
		
		// 클라이언트 아이디 추출
		String clientId = getClientId(header);
		
		// 테스트용
		System.out.println("### DEBUG AUTH FAIL: " + ip + ", " + authId + ", " + clientId);
	}
	
	/**
	 * 
	 * 
	 * @param transportLayer
	 * @return
	 */
	private static String getIp(Object transportLayer) {
		
		String ip = "UNKNOWN";
		
		try {
			Method socketChannelMethod = transportLayer.getClass().getMethod("socketChannel");
			SocketChannel ch = (SocketChannel)socketChannelMethod.invoke(transportLayer);
			ip = ch.getRemoteAddress().toString().replace("/", "");
		} catch(Exception ex) {}
		
		return ip;
	}
	
	/**
	 * 
	 * 
	 * @param saslServer
	 * @return
	 */
	private static String getAuthId(SaslServer saslServer) {
		return (saslServer != null) ? saslServer.getAuthorizationID() : "NONE";
	}
	
	/**
	 * 
	 * 
	 * @param header
	 * @return
	 */
	private static String getClientId(Object header) {
		
		String clientId = "UNKNOWN";
 
		try {
			Method clientIdMethod = header.getClass().getMethod("clientId");
			clientId = (String)clientIdMethod.invoke(header);
		} catch (Exception ex) {}
		
		return clientId;
	}
}
