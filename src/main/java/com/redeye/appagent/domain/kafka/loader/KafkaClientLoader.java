package com.redeye.appagent.domain.kafka.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.redeye.appagent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.appagent.loader.APILoader;
import com.redeye.appagent.loader.entity.APIContextDTO;
import com.redeye.appagent.util.JSONUtil;
import com.redeye.appagent.util.LogUtil;
import com.redeye.appagent.util.RESTUtil;

/**
 * kafka 클라이언트(프로듀서/컨슈머) 성능 정보 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaClientLoader implements APILoader {
	
	
	/** kafka 클라이언트 정보 서브패스 */
	private static String SUBPATH = "/api/kafka/client";


	@Override
	public void load(APIContextDTO context) {
		
		try {
			
			// 메시지 전송 url path 생성
			String path = makePath(context);
			
			// 메시지 생성
			String message = makeMessage(context);
			
			// 메시지 전송
			RESTUtil.post(
				path,
				message,
				(respCode, respMessage) -> {
					
					// 실패시 메시지 출력
					if(respCode != 200) {
						LogUtil.log("fail to send sql stat(" + respCode + "): " + path);
						return;
					}
				}
			);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 호출 패스 생성
	 * 
	 * @param basePath 기본 패스
	 * @return 생성된 패스
	 */
	private static String makePath(APIContextDTO context) {
		
		return new StringBuilder(context.getBasePath())
			.append(SUBPATH)
			.toString();
	}
	
	/**
	 * Kafka 클라이언트 Json 메시지 생성 및 반환
	 * 
	 * @param startTime 시작 시간
	 * @param endTime 다음 실행 시간
	 * @return 생성된 Json 메시지
	 */
	private static String makeMessage(APIContextDTO context) throws Exception {
		
		// json 맵 객체 변수
		Map<String, Object> jsonMap = new HashMap<>();
		
		// 통계 수집 시간 정보 추가
		jsonMap.put("startTime", context.getStartTime());
		jsonMap.put("endTime", context.getEndTime());
		
		// 프로듀서 정보 맵 추가
		Map<String, Object> producerMap = new HashMap<>();
		
		Set<String> producerIdSet = KafkaAcquisitor.getProducerClientIdSet();
		for(String producerId: producerIdSet) {
			producerMap.put(producerId, makeProducerMessage(producerId));
		}
		
		jsonMap.put("producer", producerMap);
		
		// 컨슈머 정보 맵 추가
		Map<String, Object> consumerMap = new HashMap<>();
		
		Set<String> consumerIdSet = KafkaAcquisitor.getConsumerClientIdSet();
		for(String consumerId: consumerIdSet) {
			consumerMap.put(consumerId, makeConsumerMessage(consumerId));
		}
		
		jsonMap.put("consumer", consumerMap);

		// JSON 메시지 변환 및 반환
		return JSONUtil.toJSON(jsonMap);
	}
	
	/**
	 * 프로듀서 메시지 맵 객체 생성 및 반환
	 * 
	 * @param producerId 프로듀서 아이디
	 * @return 프로듀서 메시지 맵 객체
	 */
	private static Map<String, Object> makeProducerMessage(String producerId) throws Exception {
		
		return Map.of(
					
			// 설정값 추가
			"config", KafkaAcquisitor.getConfigMap(producerId),
			
			// 성능 정보 추가
			"metrics", KafkaAcquisitor.getProducerMetrics(producerId),
			
			// 통계 정보 추가
			"stat",
				Map.of(
					
					// commitSync, commitAsync 간격 통계 정보 추가
					// 통계 정보 삭세 후 삭제된 값을 저장
					"commitSync", KafkaAcquisitor.commitSyncTimeStatDaemon.getStat().remove(producerId).toMap(),
					"commitAsync", KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(producerId).toMap()
				)
		);
	}
	
	/**
	 * 컨슈머 메시지 생성 및 반환
	 * 
	 * @param consumerId 컨슈머 아이디
	 * @return 생성된 컨슈머 메시지
	 */
	private static Map<String, Object> makeConsumerMessage(String consumerId) throws Exception {
		
		return Map.of(
			
			// 설정값 추가
			"config", KafkaAcquisitor.getConfigMap(consumerId),
			
			// 성능 정보 추가
			"metrics", KafkaAcquisitor.getConsumerMetrics(consumerId),
			
			// 통계 정보 추가
			"stat",
				Map.of(
					// 폴링 간격 통계 정보 추가
					// 통계 정보 삭세 후 삭제된 값을 저장
					"polling", KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(consumerId).toMap()
				)
		);
	}
}
