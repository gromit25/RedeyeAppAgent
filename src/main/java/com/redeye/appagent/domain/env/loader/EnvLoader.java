package com.redeye.appagent.domain.env.loader;

import java.util.HashMap;
import java.util.Map;

import com.redeye.appagent.loader.APILoader;
import com.redeye.appagent.loader.entity.APIContextDTO;
import com.redeye.appagent.util.JSONUtil;
import com.redeye.appagent.util.LogUtil;
import com.redeye.appagent.util.RESTUtil;

/**
 * 환경 변수 로더 클래스
 * 
 * @author jmsohn
 */
public class EnvLoader implements APILoader {
	
	
	/** 환경 변수 서브패스 */
	private static String SUBPATH = "/api/prc/%d/%d/env";
	
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
	public void load(APIContextDTO context) {
		
		// 이미 전송되었으면 전송하지 않음
		if(this.isSend == true) {
			return;
		}
		
		// 전송할 url 패스 생성
		String path = makePath(context);
		
		// 전송 메시지 생성
		String message = this.makeMessage(context);
				
		try {
			
			RESTUtil.post(
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
	 * @param context API 컨텍스트 객체
	 * @return 생성된 패스
	 */
	private static String makePath(APIContextDTO context) {
		
		return new StringBuilder()
			.append(context.getBasePath())
			.append(String.format(SUBPATH, context.getHostId(), context.getAppId()))
			.toString();
	}
	
	/**
	 * Sql 통계 Json 메시지 생성 및 반환
	 * 
	 * @param context API 컨텍스트 객체
	 * @return Sql 통계 Json 메시지
	 */
	private String makeMessage(APIContextDTO context) {

		Map<String, Object> msgMap = new HashMap<>();

		msgMap.put("startTime", context.getStartTime());
		msgMap.put("endTime", context.getEndTime());
		
		msgMap.put("envMap", this.envMap);
		
		return JSONUtil.toJSON(msgMap);
	}
}
