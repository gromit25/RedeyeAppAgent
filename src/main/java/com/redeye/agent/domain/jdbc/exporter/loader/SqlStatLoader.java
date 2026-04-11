package com.redeye.agent.domain.jdbc.exporter.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redeye.agent.Config;
import com.redeye.agent.domain.jdbc.acquisitor.JDBCAcquisitor;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.util.HttpUtil;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.LogUtil;
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
		
		// 전송할 url 패스 생성
		String path = makePath(basePath);
		
		JDBCAcquisitor.sqlStatDaemon.flush(
			(id, stat) -> {
				
				// sql 아이디 패턴 확인
				Matcher idM = idP.matcher(id);
				if(idM.matches() == false) {
					return;
				}

				// 전송할 JSON 메시지 생성
				String message = makeMessage(startTime, endTime, idM, stat);
				
				// JSON 메시지 전송
				try {
					
					System.out.println("#### DEBUG SQL ####");
					System.out.println(message);
					
//					HttpUtil.postJSON(
//						path,
//						message,
//						(respCode, respMessage) -> {
//							
//							// 실패시 메시지 출력
//							if(respCode != 200) {
//								LogUtil.log("fail to send sql stat(" + respCode + "): " + path);
//							}
//						}
//					);
					
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		);
	}
	
	/**
	 * 호출 패스 생성
	 * 
	 * @param basePath 기본 패스
	 * @return 생성된 패스
	 */
	private static String makePath(String basePath) {
		
		return new StringBuilder()
			.append(basePath)
			.append(SUBPATH)
			.toString();
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
	private static String makeMessage(long startTime, long endTime, Matcher idM, Parameter stat) {
		
		// json 메시지 변수
		StringBuilder json = new StringBuilder("");
		
		// 시간 정보 메시지 추가
		json
			.append("{")
			.append("\"startTime\":").append(startTime)
			.append(",\"endTime\":").append(endTime);
		
		// sql 기본 정보
		json.append(",\"sqlInfo\":{");
		
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
		
		// sql 통계 정보
		json.append(",\"sqlStat\":");
		json.append(JSONUtil.toJSON(stat));
		
		json.append("}");
		
		return json.toString();
	}
}
