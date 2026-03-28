package com.redeye.agent.domain.jdbc;

import com.redeye.agent.domain.common.AdviceUtil;
import com.redeye.agent.domain.jdbc.acquisitor.advice.ConnectionAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.ContextHolder;

/**
 * JDBC 관련 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class JDBCUtil {

	/**
	 * sql 아이디 생성 후 반환
	 * 
	 * @param sql sql
	 * @return sql 아이디
	 */
	public static String getSqlId(String sql) {
		return AdviceUtil.getLoc() + ":" + sql;
	}
	
	/**
	 * sql 아이디 생성 후 반환
	 * 
	 * @return sql 아이디
	 */
	public static String getSqlId() {
		return getSqlId(ContextHolder.getSql());
	}
}
