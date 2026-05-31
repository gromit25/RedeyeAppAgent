package com.redeye.appagent.domain.jdbc;

import com.redeye.appagent.domain.common.AdviceUtil;
import com.redeye.appagent.domain.jdbc.acquisitor.advice.ContextHolder;

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
