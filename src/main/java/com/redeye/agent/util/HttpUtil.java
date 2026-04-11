package com.redeye.agent.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.redeye.agent.Config;

/**
 * Http 관련 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class HttpUtil {
	
	
	/**
	 * Http 응답 처리 컨슈머 인터페이스
	 */
	@FunctionalInterface
	public static interface RespConsumer {
		
		/**
		 * 응답 메시지 처리 메소드
		 * 
		 * @param respCode 응답 코드
		 * @param respMessage 응답 메시지
		 */
		public void consume(int respCode, String respMessage);
	}
	

	/**
	 * JSON 메시지 전송<br>
	 * POST 방식
	 * 
	 * @param path 패스
	 * @param message 메시지
	 * @param respConsumer 응답 처리 컨슈머
	 */
	public static void postJSON(String path, String message, RespConsumer respConsumer) throws Exception {
		
		// 입력 값 검증
		if(StringUtil.isBlank(path) == true) {
			throw new IllegalArgumentException("'path' is null or blank.");
		}
		
		if(message == null) {
			message = "";
		}
		
		// url 연결 생성
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        
		// 메소드 및 헤더 설정
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json; utf-8");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("X-Token", getToken());
		conn.setDoOutput(true);

		// JSON 데이터 전송 (Write)
		try(OutputStream os = conn.getOutputStream()) {
			byte[] input = message.getBytes("utf-8");
			os.write(input, 0, input.length);           
		}

		// 응답 코드 확인 및 데이터 읽기 (Read)
		int code = conn.getResponseCode();
		
		try(
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))
		) {
			
			StringBuilder response = new StringBuilder();
			
			String responseLine = null;
			while((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			
			// 응답 처리 컨슈머에게 처리 전달
			// 컨슈머가 null 일 경우 처리하지 않음
			if(respConsumer != null) {
				respConsumer.consume(code, response.toString());
			}
		}
	}
	
	/**
	 * 토큰 문자열 반환
	 * 
	 * @return 토큰 문자열
	 */
	private static String getToken() {
		return new StringBuilder()
			.append(Config.ORGAN_CODE.getValue()).append("/")
			.append(Config.DOMAIN_CODE.getValue()).append("/")
			.append(Config.APP_CODE.getValue())
			.toString();
	}
}
