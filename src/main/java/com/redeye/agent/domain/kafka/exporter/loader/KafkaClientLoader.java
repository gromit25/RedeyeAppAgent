package com.redeye.agent.domain.kafka.exporter.loader;

import java.util.Set;

import com.redeye.agent.Config;
import com.redeye.agent.domain.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.util.HttpUtil;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.LogUtil;
import com.redeye.agent.util.stat.Parameter;

/**
 * kafka 클라이언트(프로듀서/컨슈머) 성능 정보 로더 클래스
 * 
 * @author jmsohn
 */
public class KafkaClientLoader implements APILoader {
	
	
	/** kafka 클라이언트 정보 서브패스 */
	private static String SUBPATH = "/api/kafka/client";


	@Override
	public void load(String basePath, long startTime, long endTime) {
		
		try {
			
			// 메시지 전송 url path 생성
			String path = makePath(basePath);
			
			// 메시지 생성
			String message = makeMessage(startTime, endTime);
			System.out.println("### DEBUG MESSAGE: " + message);
			
			// 메시지 전송
			HttpUtil.postJSON(
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
	private static String makePath(String basePath) {
		
		return new StringBuilder(basePath)
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
	private static String makeMessage(long startTime, long endTime) throws Exception {
		
		// json 메시지 변수
		StringBuilder json = new StringBuilder("");
		
		// 시간 정보 메시지 추가
		json
			.append("{")
			.append("\"startTime\":").append(startTime)
			.append(",\"endTime\":").append(endTime);
		
		// 프로듀서 정보 메시지 추가
		json.append(",\"producer\": {");
		
		Set<String> producerIdSet = KafkaAcquisitor.getProducerClientIdSet();
		for(String producerId: producerIdSet) {
			json.append(makeProducerMessage(producerId));
		}
		
		json.append("}");
		
		// 컨슈머 정보 메시지 추가
		json.append(",\"consumer\": {");
		
		Set<String> consumerIdSet = KafkaAcquisitor.getConsumerClientIdSet();
		for(String consumerId: consumerIdSet) {
			json.append(makeConsumerMessage(consumerId));
		}
		
		json.append("}");
		
		json.append("}");
		
		return json.toString();
	}
	
	/**
	 * 프로듀서 메시지 생성 및 반환
	 * 
	 * @param producerId 프로듀서 아이디
	 * @return 생성된 프로듀서 메시지
	 */
	private static String makeProducerMessage(String producerId) throws Exception {
		
		// json 메시지 변수
		StringBuilder json = new StringBuilder();
		
		// 클라이언트 아이디 추가
		json
			.append("\"")
			.append(producerId)
			.append("\": {");
		
		// 설정값 추가
		json
			.append("\"config\": ")
			.append(JSONUtil.toJSON(KafkaAcquisitor.getConfigMap(producerId)));
		
		// 성능 정보 추가
		json
			.append(",\"metrics\": ")
			.append(JSONUtil.toJSON(KafkaAcquisitor.getProducerMetrics(producerId)));
		
		// 통계 정보 추가 시작
		json
			.append(",\"stat\":{");
		
		// commitSync 간격 통계 정보 추가
		Parameter commitSyncStat = KafkaAcquisitor.commitSyncTimeStatDaemon.getStat().remove(producerId);
		json
			.append("\"commitSync\": ")
			.append(JSONUtil.toJSON(commitSyncStat));
		
		// commitAsync 간격 통계 정보 추가
		Parameter commitAsyncStat = KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(producerId);
		json
			.append(",\"commitAsync\": ")
			.append(JSONUtil.toJSON(commitAsyncStat));
	
		// 통계 정보 종료
		json
			.append("}");
		
		return json.append("}").toString();
	}
	
	/**
	 * 컨슈머 메시지 생성 및 반환
	 * 
	 * @param consumerId 컨슈머 아이디
	 * @return 생성된 컨슈머 메시지
	 */
	private static String makeConsumerMessage(String consumerId) throws Exception {
		
		// json 메시지 변수
		StringBuilder json = new StringBuilder();
		
		// 클라이언트 아이디 추가
		json
			.append("\"")
			.append(consumerId)
			.append("\": {");
		
		// 설정값 추가
		json
			.append("\"config\": ")
			.append(JSONUtil.toJSON(KafkaAcquisitor.getConfigMap(consumerId)));
		
		// 성능 정보 추가
		json
			.append(",\"metrics\": ")
			.append(JSONUtil.toJSON(KafkaAcquisitor.getConsumerMetrics(consumerId)));
		
		// 폴링 간격 통계 정보 추가
		Parameter pollingStat = KafkaAcquisitor.commitAsyncTimeStatDaemon.getStat().remove(consumerId);
		json
			.append(",\"stat\":{")
			.append("\"polling\":")
			.append(JSONUtil.toJSON(pollingStat))
			.append("}");
		
		return json.append("}").toString();
	}
}
