package com.redeye.agent.domain.jdbc.acquisitor;

import com.redeye.agent.domain.jdbc.acquisitor.advice.PreparedStatementAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.StatementAdvice;
import com.redeye.agent.util.daemon.intervalstat.IntervalStatDaemon;

/**
 * JDBC 수집기
 * 
 * @author jmsohn
 */
public class JDBCAcquisitor {
	
	
	/** sql 수행 시간 통계 데몬 */
	public final static IntervalStatDaemon sqlStatDaemon = new IntervalStatDaemon();
	
	
	/**
	 * 초기화
	 */
	public static void init() {
		
		PreparedStatementAdvice.init(sqlStatDaemon);
		StatementAdvice.init(sqlStatDaemon);
	}
}
