package com.redeye.agent.loader;

/**
 * 각 성능 정보별 저장 API 호출 인터페이스 클래스 
 * 
 * @author jmsohn
 */
public interface APILoader {
	
	/**
	 * API 호출을 통한 성능 정보 저장
	 * 
	 * @param hostId 호스트 아이디
	 * @param appId 어플리케이션 아이디
	 * @param basePath 기준 패스
	 * @param startTime 시작 시간
	 * @param endTime 다음 실행 시간
	 */
	void load(long hostId, long appId, String basePath, long startTime, long endTime);
}
