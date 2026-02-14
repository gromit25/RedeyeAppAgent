package com.redeye.agent.jdbc;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.redeye.agent.Context;
import com.redeye.agent.loader.APILoader;

/**
 * 
 * 
 * @author jmsohn
 */
public class JDBCContext implements Context {

	@Override
	public void init() {
	}

	@Override
	public void addTransformer(Instrumentation inst) {
	}

	@Override
	public List<Object> getWebControllerList() {
		return List.of();
	}

	@Override
	public List<APILoader> getAPILoaderList() {
		return List.of();
	}
}
