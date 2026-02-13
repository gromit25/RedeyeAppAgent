package com.redeye.agent;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.redeye.agent.loader.APILoader;

/**
 * 외부 데이터 표출 및 저장 컨택스트 인터페이스 클래스
 * 
 * @author jmsohn
 */
public interface Context {
	
	/**
	 * 컨텍스트 초기화
	 */
	void init();
	
	/**
	 * 클래스 변환 수행
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	void addTransformer(Instrumentation inst);

	/**
	 * 웹 컨트롤러 목록 반환
	 * 
	 * @return 웹 컨트롤러 목록
	 */
	List<Object> getWebControllerList();
	
	/**
	 * API 로더 목록 반환
	 * 
	 * @return API 로더 목록
	 */
	List<APILoader> getAPILoaderList();
}
