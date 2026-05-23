package com.redeye.agent.domain.env;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import com.redeye.agent.Context;
import com.redeye.agent.domain.env.loader.EnvLoader;
import com.redeye.agent.loader.APILoader;

/**
 * 환경 변수 컨텍스트 클래스
 * 
 * @author jmsohn
 */
public class EnvContext implements Context {
	
	
	/** 환경 변수 맵 객체 */
	private Map<String, String> envMap;
	

	@Override
	public void init() {
		
		// 모든 환경 변수를 수집 및 저장
		this.envMap = System.getenv();
	}

	@Override
	public void addTransformer(Instrumentation inst) {
		// do nothing
	}

	@Override
	public List<Object> getWebControllerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<APILoader> getAPILoaderList() {
		return List.of(new EnvLoader(this.envMap));
	}
}
