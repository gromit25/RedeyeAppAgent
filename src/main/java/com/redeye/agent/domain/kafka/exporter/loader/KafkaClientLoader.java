package com.redeye.agent.domain.kafka.exporter.loader;

import java.util.Set;

import com.redeye.agent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.stat.Parameter;

/**
 * kafka 클라이언트 데이터 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaClientLoader implements APILoader {
	
	
	/** kafka 클라이언트 정보 서브패스 */
	private static String SUBPATH = "/api/kafka/client";


	@Override
	public void load(String basePath, long startTime, long endTime) {
		
		try {
			System.out.println(makeMessage(startTime, endTime));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * 
	 * @param startTime 시작 시간
	 * @param endTime 다음 실행 시간
	 * @return
	 */
	private static String makeMessage(long startTime, long endTime) throws Exception {
		
		StringBuilder json = new StringBuilder("");
		
		json.append("{");
		
		// 프로듀서 정보 메시지 추가
		Set<String> producerIdSet = KafkaAcquisitor.getProducerClientIdSet();
		for(String producerId: producerIdSet) {
			json.append(makeProducerMessage(producerId, startTime, endTime));
		}
		
		// 컨슈머 정보 메시지 추가
		Set<String> consumerIdSet = KafkaAcquisitor.getConsumerClientIdSet();
		for(String consumerId: consumerIdSet) {
			json.append(makeConsumerMessage(consumerId, startTime, endTime));
		}
		
		json.append("}");
		
		return json.toString();
	}
	
	/**
	 * 프로듀서 메시지 생성 및 반환
	 * 
	 * @param producerId 프로듀서 아이디
	 * @param startTime 시작 시간
	 * @param endTime 다음 실행 시간
	 * @return 생성된 프로듀서 메시지
	 */
	private static String makeProducerMessage(String producerId, long startTime, long endTime) throws Exception {
		
		StringBuilder json = new StringBuilder();
		
		// 클라이언트 아이디와 타입 추가
		json.append("");
		
		// 설정값 추가
		json.append(
			JSONUtil.toJSON(KafkaAcquisitor.getConfigMap(producerId))
		);
		
		// 성능 정보 추가
		json.append(
			JSONUtil.toJSON(KafkaAcquisitor.getProducerMetrics(producerId))
		);
		
		// commitSync 간격 통계 정보 추가
		Parameter commitSyncStat = KafkaAcquisitor.commitSyncTimeStatDaemon.getStat().remove(producerId);
		json.append(JSONUtil.toJSON(commitSyncStat, startTime, endTime));
		
		// commitAsync 간격 통계 정보 추가
		Parameter commitAsyncStat = KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(producerId);
		json.append(JSONUtil.toJSON(commitAsyncStat, startTime, endTime));
		
		return json.toString();
	}
	
	/**
	 * 컨슈머 메시지 생성 및 반환
	 * 
	 * @param consumerId 컨슈머 아이디
	 * @param startTime 시작 시간
	 * @param endTime 다음 실행 시간
	 * @return 생성된 컨슈머 메시지
	 */
	private static String makeConsumerMessage(String consumerId, long startTime, long endTime) throws Exception {
		
		StringBuilder json = new StringBuilder();
		
		// 클라이언트 아이디와 타입 추가
		json.append("");
		
		// 설정값 추가
		json.append(
			JSONUtil.toJSON(KafkaAcquisitor.getConfigMap(consumerId))
		);
		
		// 성능 정보 추가
		json.append(
			JSONUtil.toJSON(KafkaAcquisitor.getConsumerMetrics(consumerId))
		);
		
		// 폴링 간격 통계 정보 추가
		Parameter pollingStat = KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(consumerId);
		json.append(JSONUtil.toJSON(pollingStat, startTime, endTime));
		
		return json.toString();
	}
}
