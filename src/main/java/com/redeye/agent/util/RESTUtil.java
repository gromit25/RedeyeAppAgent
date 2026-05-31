package com.redeye.agent.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.redeye.agent.Config;

import lombok.Getter;

/**
 * Http 관련 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class RESTUtil {
	
	/**
	 * Http 메소드 종류
	 */
	private static enum HttpMethod {
		
		GET("GET"),
		POST("POST");
		
		
		/** Http 메소드 문자열 */
		@Getter
		private String methodStr;
		
		
		/**
		 * 생성자
		 * 
		 * @param methodStr
		 */
		HttpMethod(String methodStr) {
			this.methodStr = methodStr;
		}
	}
	
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
	 * GET 요청 전송<br>
	 * path가 / 으로 시작시 화면에만 출력
	 * 
	 * @param path api 호출 url
	 * @param respConsumer 응답 처리 컨슈머
	 */
	public static void get(String path, RespConsumer respConsumer) throws Exception {
		process(HttpMethod.GET, path, null, respConsumer);
	}

	/**
	 * POST 요청 전송<br>
	 * path가 / 으로 시작시 화면에만 출력
	 * 
	 * @param path api 호출 url
	 * @param message 송신할 JSON 메시지
	 * @param respConsumer 응답 처리 컨슈머
	 */
	public static void post(String path, String message, RespConsumer respConsumer) throws Exception {
		process(HttpMethod.POST, path, message, respConsumer);
	}
	
	/**
	 * REST Api 호출 처리
	 * 
	 * @param method Http 메소드
	 * @param path api 호출 url
	 * @param message 송신할 JSON 메시지
	 * @param respConsumer 응답 처리 컨슈머
	 */
	private static void process(
		HttpMethod method,
		String path,
		String message,
		RespConsumer respConsumer
	) throws Exception {
		
		// 입력 값 검증
		if(method == null) {
			throw new IllegalArgumentException("'method' is null.");
		}
		
		if(StringUtil.isBlank(path) == true) {
			throw new IllegalArgumentException("'path' is null or blank.");
		}
		
		// 요청 헤더 생성
		Map<String, String> headerMap = Map.of(
			"Content-Type", "application/json; utf-8",
			"Accept", "application/json",
			"X-Token", getToken()
		);
		
		// path가 '/'로 시작하는 경우(ex. /api/host/info/id) 화면에 발송내역을 출력
		// 아닐 경우(http://localhost/api/host/info/id) 서버로 JSON 전송
		if(path.startsWith("/") == true) {
			print(method, path, headerMap, message, respConsumer);
		} else {
			send(method, path, headerMap, message, respConsumer);
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
			.append(Config.DOMAIN_CODE.getValue())
			.toString();
	}
	
	/**
	 * REST API 서버에 호출 및 컨슈머 호출
	 * 
	 * @param method Http 메소드
	 * @param path api 호출 url
	 * @param headerMap 헤더 맵 객체
	 * @param message 송신할 JSON 메시지
	 * @param respConsumer 응답 처리 컨슈머
	 */
	private static void send(
		HttpMethod method,
		String path,
		Map<String, String> headerMap,
		String message,
		RespConsumer respConsumer
	) throws Exception {
		
		// 메시지가 없는 경우 공란으로 대체
		if(message == null) {
			message = "";
		}
		
		// url 연결 생성
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        
		// 메소드 설정
		conn.setRequestMethod(method.getMethodStr());
		
		// 헤더 설정
		if(headerMap != null && headerMap.size() != 0) {
			headerMap.forEach((key, value) -> {
				conn.setRequestProperty(key, value);
			});
		}

		conn.setDoOutput(true);

		// GET 메소드가 아닐 경우, JSON 데이터 전송 (Write)
		if(method != HttpMethod.GET) {
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = message.getBytes("utf-8");
				os.write(input, 0, input.length);           
			}
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
	 * REST API 호출 내용 화면 출력
	 * 
	 * @param method Http 메소드
	 * @param path api 호출 url
	 * @param headerMap 헤더 맵 객체
	 * @param message 송신할 JSON 메시지
	 * @param respConsumer 응답 처리 컨슈머
	 */
	private static void print(
		HttpMethod method,
		String path,
		Map<String, String> headerMap,
		String message,
		RespConsumer respConsumer
	) {
		
		// 요청 메소드 및 패스 출력
		StringBuilder req = new StringBuilder()
			.append(method.getMethodStr()).append(" ").append(path).append("\n");
		
		// 요청 헤더 출력
		headerMap.forEach((key, value) -> {
			req.append(key).append(": ").append(value).append("\n");
		});
		
		req.append("\n");
		
		// 요청 메시지 출력
		if(StringUtil.isBlank(message) == false) {
			req
				.append(message)
				.append("\n")
				.append("\n");
		}
		
		// 화면 출력
		System.out.println(req.toString());
	}
}
