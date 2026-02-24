package com.redeye.agent.domain.common;

import java.util.concurrent.BlockingQueue;

import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.daemon.elapsedstat.ElapsedStatDaemon;
import com.redeye.agent.util.daemon.elapsedstat.ElapsedTimeVO;

/**
 * 수행 시간 처리 어드바이스 상위(공통) 클래스
 * 
 * @author jmsohn
 */
public class ElapsedTimeAdvice {
	
	// 아래의 멤버 변수는 public 이어야 함 - SpringBoot 클래스로더에서 문제가 생김
	
	/** 시간 데이터 전송용 큐 */
	public static BlockingQueue<ElapsedTimeVO> queue;
	
	
	/**
	 * 초기화<br>
	 * 시간 통계 데몬의 큐와 연결
	 *
	 * @param timeStatDaemon 시간 통계 데몬
	 */
	public static void init(ElapsedStatDaemon timeStatDaemon) {
		queue = timeStatDaemon.getQueue();
	}
	
	/**
	 * 수행 시간 정보를 처리자에게 전송
	 * 
	 * @param id
	 * @param elapsedTime
	 */
	public static void put(String id, long elapsedTime) {
		
		// 입력 값 및 큐 검사
		if(StringUtil.isBlank(id) == true || queue == null) {
			return;
		}
		
		try {
			
			// 큐에 클라이언트 아이디 및 현재 시간 전송
			queue.put(
				new ElapsedTimeVO(
					id,	// 클라이언트 아이디
					System.currentTimeMillis(),	// 현재 시간
					elapsedTime // 수행 시간
				)
			);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
