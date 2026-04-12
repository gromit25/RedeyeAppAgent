package com.redeye.agent.domain.env.exporter.loader;

import java.util.Map;

import com.redeye.agent.Config;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.util.HttpUtil;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.LogUtil;

/**
 * 환경 변수 로더 클래스
 * 
 * @author jmsohn
 */
public class EnvLoader implements APILoader {
	
	
	/** 환경 변수 서브패스 */
	private static String SUBPATH = "/api/app/env";
	
	/** 환경 변수 맵 */
	private Map<String, String> envMap;
	
	/** 전송 성공 여부 */
	private volatile boolean isSend = false;
	
	
	/**
	 * 생성자
	 * 
	 * @param envMap 환경 변수 맵
	 */
	public EnvLoader(Map<String, String> envMap) {
		this.envMap = envMap;
	}
	
	@Override
	public void load(String basePath, long startTime, long endTime) {
		
		// 이미 전송되었으면 전송하지 않음
		if(this.isSend == true) {
			return;
		}
		
		// 전송할 url 패스 생성
		String path = makePath(basePath);
		
		// 전송 메시지 생성
		String message = this.makeMessage(startTime, endTime);
				
		try {
			
			HttpUtil.postJSON(
				path,
				message,
				(respCode, respMessage) -> {
					
					// 실패시 메시지 출력
					if(respCode != 200) {
						LogUtil.log("fail to send env(" + respCode + "): " + path);
						return;
					}
					
					// 전송 성공시 성공 설정
					isSend = true;
				}
			);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	 * @return Sql 통계 Json 메시지
	 */
	private String makeMessage(long startTime, long endTime) {
		
		// json 메시지 변수
		StringBuilder json = new StringBuilder("");
		
		// 시간 정보 메시지 추가
		json
			.append("{")
			.append("\"startTime\":").append(startTime)
			.append(",\"endTime\":").append(endTime);
		
		// 환경 변수 정보 설정
		json
			.append(",\"envMap\":")
			.append(JSONUtil.toJSON(this.envMap));
		
		json.append("}");
		
		return json.toString();
	}
}
