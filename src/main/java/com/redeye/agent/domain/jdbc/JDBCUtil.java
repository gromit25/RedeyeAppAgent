package com.redeye.agent.domain.jdbc;

import com.redeye.agent.domain.common.AdviceUtil;
import com.redeye.agent.domain.jdbc.acquisitor.advice.ConnectionAdvice;

/**
 * 
 * 
 * @author jmsohn
 */
public class JDBCUtil {

	/**
	 * 
	 * 
	 * @return
	 */
	public static String getSqlId(String sql) {
		return AdviceUtil.getLoc() + ":" + sql;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static String getSqlId() {
		return getSqlId(ConnectionAdvice.getSql());
	}
}
