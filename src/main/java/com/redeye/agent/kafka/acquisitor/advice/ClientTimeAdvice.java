package com.redeye.agent.kafka.acquisitor.advice;

import java.util.concurrent.BlockingQueue;

import com.redeye.agent.kafka.acquisitor.advice.consumer.KafkaConsumerAdvice;
import com.redeye.agent.kafka.model.ClientTimeDTO;
import com.redeye.agent.kafka.stat.TimeStatDaemon;

/**
 * kafka 클라이언트의 시간 처리 관련 어드바이스 상위(공통) 클래스
 * 
 * @author jmsohn
 */
public class ClientTimeAdvice {

	// 아래의 멤버 변수는 public 이어야 함 - SpringBoot 클래스로더에서 문제가 생김
	
	/** 시간 데이터 전송용 큐 */
	public static BlockingQueue<ClientTimeDTO> queue;
	
	
	/**
	 * 초기화<br>
	 * 시간 통계 데몬의 큐와 연결
	 *
	 * @param timeStatDaemon 시간 통계 데몬
	 */
	public static void init(TimeStatDaemon timeStatDaemon) {
		ClientTimeAdvice.queue = timeStatDaemon.getQueue();
	}
	
	/**
	 * 현재 시간을 처리자에게 전송
	 * 
	 * @param consumer 클라이언트 객체
	 */
	protected static void sendCurTime(Object consumer) {
		
		// 입력 값 및 큐 검사
		if(consumer == null || queue == null) {
			return;
		}
		
		try {
			
			// 클라이언트 아이디 획득
			String clientId = KafkaConsumerAdvice.getClientId(consumer);
			if(clientId == null) {
				return;
			}

			// 큐에 클라이언트 아이디 및 현재 시간 전송
			ClientTimeAdvice.queue.put(
				new ClientTimeDTO(
					clientId,	// 클라이언트 아이디
					System.currentTimeMillis()	// 현재 시간
				)
			);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
