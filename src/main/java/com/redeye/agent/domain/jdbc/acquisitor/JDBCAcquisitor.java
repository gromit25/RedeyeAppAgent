package com.redeye.agent.domain.jdbc.acquisitor;

import com.redeye.agent.domain.jdbc.acquisitor.advice.PreparedStatementAdvice;
import com.redeye.agent.domain.jdbc.acquisitor.advice.StatementAdvice;
import com.redeye.agent.util.daemon.elapsedstat.ElapsedStatDaemon;
import com.redeye.agent.util.daemon.intervalstat.IntervalStatDaemon;

/**
 * JDBC 성능 정보 수집기
 * 
 * @author jmsohn
 */
public class JDBCAcquisitor {
	
	
	/** sql 수행 시간 통계 데몬 */
	public final static ElapsedStatDaemon sqlStatDaemon = new ElapsedStatDaemon();
	
	
	/**
	 * 초기화 및 기동
	 */
	public static void init() {
		
		// 어드바이스 초기화
		PreparedStatementAdvice.init(sqlStatDaemon);
		StatementAdvice.init(sqlStatDaemon);
		
		// sql 통계 데몬 기동
		sqlStatDaemon.start();
	}
}
