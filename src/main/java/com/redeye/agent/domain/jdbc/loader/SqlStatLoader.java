package com.redeye.agent.domain.jdbc.loader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redeye.agent.Config;
import com.redeye.agent.domain.jdbc.acquisitor.JDBCAcquisitor;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.util.stat.Parameter;

/**
 * sql 통계 데이터 로더 클래스
 * 
 * @author jmsohn
 */
public class SqlStatLoader implements APILoader {
	
	
	/** sql 통계 정보 서브패스 */
	private static String SUBPATH = "/api/app/sql";
	
	/** sql 아이디 패턴 문자열 */
	private static String ID_PATTERN = "(?<class>[^:]+)\\:(?<method>[^:]+)\\:(?<lineNum>(\\-)?[0-9]+)\\:(?<stmt>.+)";
	
	/** sql 아이디 패턴 객체 */
	private static Pattern idP = Pattern.compile(ID_PATTERN, Pattern.DOTALL);


	@Override
	public void load(String basePath, long startTime, long endTime) {
		
		JDBCAcquisitor.sqlStatDaemon.flush(
			(id, stat) -> {
				
				// sql 아이디 패턴 확인
				Matcher idM = idP.matcher(id);
				if(idM.matches() == false) {
					return;
				}

				// 전송할 JSON 메시지 생성
				String message = makeJsonMessage(startTime, endTime, idM, stat);
				
				// JSON 메시지 생성
				try {
					send(basePath, message);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		);
	}
	
	/**
	 * Sql 통계 Json 메시지 생성 및 반환
	 * 
	 * @param startTime 통계 수집 시작 시간
	 * @param endTime 통계 수집 종료 시간
	 * @param idM sql 아이디에 대한 정규표현식 매처
	 * @param stat sql 통계 정보
	 * @return Sql 통계 Json 메시지
	 */
	private static String makeJsonMessage(long startTime, long endTime, Matcher idM, Parameter stat) {
		
		StringBuilder json = new StringBuilder("");
		json.append("{\"sqlInfo\":{");
		
		json.append("\"className\":\"").append(idM.group("class")).append("\"");
		json.append(",\"methodName\":\"").append(idM.group("method")).append("\"");
		json.append(",\"lineNum\":").append(idM.group("lineNum"));
		json.append(",\"stmt\":\"")
			.append(
				idM.group("stmt")
					.replaceAll("\n", "\\\\n")
					.replaceAll("\r", "\\\\r")
					.replaceAll("\t", "\\\\t")
			)
			.append("\"");

		json.append("}");
		
		
		json.append(",\"sqlStat\":{");
		json.append("\"startTime\":").append(startTime);
		json.append(",\"endTime\":").append(endTime);
		json.append(",\"count\":").append(stat.getCount());
		json.append(",\"sumX\":").append(stat.getSum());
		json.append(",\"sumX2\":").append(stat.getSquaredSum());
		json.append(",\"sumX3\":").append(stat.getCubedSum());
		json.append(",\"sumX4\":").append(stat.getFourthPoweredSum());
		json.append(",\"minX\":").append(stat.getMin());
		json.append(",\"maxX\":").append(stat.getMax());
		json.append("}");
		
		json.append("}");
		
		return json.toString();
	}
	
	/**
	 * JSON 메시지 전송
	 * 
	 * @param basePath 기본 패스
	 * @param message 메시지
	 */
	private static void send(String basePath, String message) throws Exception {
		
		// 패스 생성
		StringBuilder path = new StringBuilder(basePath);
		path.append(SUBPATH)
			.append("/").append(Config.DOMAIN_CODE.getValue())
			.append("/").append(Config.APP_CODE.getValue());
		
		//
		URL url = new URL(path.toString());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        
		// 헤더 설정
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json; utf-8");
		conn.setRequestProperty("Accept", "application/json");
		conn.setDoOutput(true);

		// JSON 데이터 전송 (Write)
		try(OutputStream os = conn.getOutputStream()) {
			byte[] input = message.getBytes("utf-8");
			os.write(input, 0, input.length);           
		}

		// 응답 코드 확인 및 데이터 읽기 (Read)
		int code = conn.getResponseCode();
		
		System.out.println("### DEBUG RESPONSE CODE: " + code);

		try(
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))
		) {
			
			StringBuilder response = new StringBuilder();
			
			String responseLine = null;
			while((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			
			System.out.println("Response Body: " + response.toString());
		}
	}
}
