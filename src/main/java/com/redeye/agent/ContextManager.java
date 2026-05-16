package com.redeye.agent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.agent.domain.env.EnvContext;
import com.redeye.agent.domain.jdbc.JDBCContext;
import com.redeye.agent.domain.kafka.KafkaContext;

/**
 * 컨텍스트 관리자 클래스
 *
 * @author jmsohn
 */
class ContextManager {

  
	/** 컨텍스트 목록 */
	private static List<Context> contextList = null;

	/** 컨텍스트 생성시 lock 용 객체 */
	private static Object lockObj = new Object();

  
	/**
	 * 컨텍스트 초기화
	 */
	private static void init() {

		synchronized(lockObj) {

			// 컨텍스트가 이미 설정되어 있으면 반환
			if(contextList == null) {
				return;
			}
      
			// ---- 컨텍스트 목록 초기화
			contextList = new CopyOnWriteArrayList<>();
		
			// 환경 변수 컨텍스트 추가
			contextList.add(new EnvContext());
		
			// JDBC 컨텍스트 추가
			contextList.add(new JDBCContext());
		
			// Kafka 컨텍스트 추가
			contextList.add(new KafkaContext());

			// ---- 컨텍스트 객체 초기화
			for(Context context: contextList) {
				context.init();
			}
		}
	}
  
	/**
	 * 컨텍스트 목록 반환
	 *
	 * @return 컨텍스트 목록
	 */
	static List<Context> getContextList() {

		// 컨텍스트 목록이 없을 경우 초기화 수행
		if(contextList == null) {
			init();
		}

		return contextList;
	}
}
