package com.redeye.appagent.loader;

import com.redeye.appagent.loader.entity.APIContextDTO;

/**
 * 각 성능 정보별 저장 API 호출 인터페이스 클래스 
 * 
 * @author jmsohn
 */
public interface APILoader {
	
	/**
	 * API 호출을 통한 성능 정보 저장
	 * 
	 * @param context API 컨텍스트 객체
	 */
	void load(APIContextDTO context);
}
