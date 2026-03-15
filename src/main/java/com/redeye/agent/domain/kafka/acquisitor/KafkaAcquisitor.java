package com.redeye.agent.domain.kafka.acquisitor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.redeye.agent.domain.kafka.ClientType;
import com.redeye.agent.domain.kafka.acquisitor.advice.consumer.ConsumerConfigAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.consumer.KafkaConsumerAdvice;
import com.redeye.agent.domain.kafka.acquisitor.advice.provider.ProducerConfigAdvice;
import com.redeye.agent.util.KafkaUtil;
import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.daemon.intervalstat.IntervalStatDaemon;
import com.redeye.agent.util.jmx.JMXService;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {


	/** 브로커 설정 맵 (key: 프로퍼티 명, value: 설정 값) */
	public static Map<String, Object> brokerConfigMap;

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	public static final Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	public static final Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();

	
	/** 컨슈머 폴링 시간 통계 데몬 */
	public final static IntervalStatDaemon poolTimeStatDaemon = new IntervalStatDaemon();
	
	/** 프로듀서 동기 커밋 시간 통계 데몬 */
	public final static IntervalStatDaemon commitSyncTimeStatDaemon = new IntervalStatDaemon();
	
	/** 프로듀서 비동기 시간 통계 데몬 */
	public final static IntervalStatDaemon commitAsyncTimeStatDaemon = new IntervalStatDaemon();
	
	
	/** 클라이언트 접속 정보 맵 - (key: 클라이언트 아이피:아이디 문자열, value: 최초 접속시간 */
	final static Map<String, Long> clientConnMap = new ConcurrentHashMap<>();

	
	/** Kafka JMX 데이터 수집 객체 */
	private static final JMXService jmx = new JMXService();
	

	/**
	 * 초기화
	 */
	public static void init() {
		
		// ProducerConfig 생성자 호출 어드바이스 설정
		ProducerConfigAdvice.init(KafkaAcquisitor.producerConfigMap);
		
		// ConsumerConfig 생성자 호출 어드바이스 설정
		ConsumerConfigAdvice.init(KafkaAcquisitor.consumerConfigMap);

		// 컨슈머 관련 초기화
		KafkaConsumerAdvice.poll.init(KafkaAcquisitor.poolTimeStatDaemon);
		KafkaConsumerAdvice.commitSync.init(KafkaAcquisitor.commitSyncTimeStatDaemon);
		KafkaConsumerAdvice.commitAsync.init(KafkaAcquisitor.commitAsyncTimeStatDaemon);
		
		// 통계 데몬 기동
		KafkaAcquisitor.poolTimeStatDaemon.start();
		KafkaAcquisitor.commitSyncTimeStatDaemon.start();
		KafkaAcquisitor.commitAsyncTimeStatDaemon.start();
	}

	/**
	 * 클라이언트 아이디의 타입을 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return 클라이언트 타입
	 */
	public static ClientType getClientType(String clientId) {
		
		// 프로듀서 클라이언트 아이디일 경우
		if(
			KafkaAcquisitor.producerConfigMap != null
			&& KafkaAcquisitor.producerConfigMap.containsKey(clientId) == true
		) {
			return ClientType.PRODUCER;
		}
		
		// 컨슈머 클라이언트 아이디일 경우
		if(
			KafkaAcquisitor.consumerConfigMap != null
			&& KafkaAcquisitor.consumerConfigMap.containsKey(clientId) == true
		) {
			return ClientType.CONSUMER;
		}
		
		// 둘다 아닐 경우
		return ClientType.NONE;
	}
	
	/**
	 * 프로듀서 클라이언트 아이디 별 설정 맵 반환
	 * 
	 * @return 프로듀서 클라이언트 아이디 별 설정 맵 
	 */
	public static Map<String, Map<String, Object>> getProducerConfigMap() {
		return KafkaAcquisitor.producerConfigMap;
	}
	
	/**
	 * 컨슈머 클라이언트 아이디 별 설정 맵 
	 * 
	 * @return 컨슈머 클라이언트 아이디 별 설정 맵
	 */
	public static Map<String, Map<String, Object>> getConsumerConfigMap() {
		return KafkaAcquisitor.consumerConfigMap;
	}
	
	/**
	 * 프로듀서 클라이언트 아이디 셋 반환
	 * 
	 * @return 프로듀서 클라이언트 아이디 셋
	 */
	public static Set<String> getProducerClientIdSet() {
		return getProducerConfigMap().keySet();
	}
	
	/**
	 * 컨슈머 클라이언트 아이디 셋 반환
	 * 
	 * @return 컨슈머 클라이언트 아이디 셋
	 */
	public static Set<String> getConsumerClientIdSet() {
		return getConsumerConfigMap().keySet();
	}

	/**
	 * 브로커 설정 맵 반환
	 * 
	 * @return 브로커 설정 맵
	 */
	public static Map<String, Object> getBrokerConfigMap() {
		return KafkaAcquisitor.brokerConfigMap;
	}

	/**
	 * 브로커 설정 맵 설정
	 * 
	 * @param configMap 브로커 설정 맵
	 */
	public static void setBrokerConfigMap(Map<String, Object> configMap) {
		KafkaAcquisitor.brokerConfigMap = configMap;
	}
	
	/**
	 * 클라이언트(프로듀서/컨슈머)의 설정 맵 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return 설정 맵
	 */
	public static Map<String, Object> getConfigMap(String clientId) {

		// 입력 값 검증
		if(StringUtil.isBlank(clientId) == true) {
			return Map.of();
		}
		
		// 클라이언트 아이디의 클라이언트 타입 획득
		ClientType clientType = getClientType(clientId);
		
		// 클라이언트 아이디가 프로듀서일 경우 프로듀서 설정 값을 반환
		if(clientType == ClientType.PRODUCER) {
			return KafkaAcquisitor.producerConfigMap.get(clientId);
		}
		
		// 클라이언트 아이디가 컨슈머일 경우 컨슈머 설정 값을 반환
		if(clientType == ClientType.CONSUMER) {
			return KafkaAcquisitor.consumerConfigMap.get(clientId);
		}
		
		// 둘다 아닐 경우 빈 값 반환
		return Map.of();
	}
	
	/**
	 * 설정 속성 값 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @param propName 설정 속성 명
	 * @return 설정 속성 값
	 */
	public static Object getConfigValue(String clientId, String propName) {
		
		if(StringUtil.isBlank(clientId) == true || StringUtil.isBlank(propName) == true) {
			return null;
		}
		
		return getConfigMap(clientId).get(propName);
	}

	/**
	 * 설정 속성 값 문자열 반환<br>
	 * toString 한 결과
	 * 
	 * @param clientId 클라이언트 아이디
	 * @param propName 설정 속성 명
	 * @return 설정 속성 값 문자열
	 */
	public static String getConfigStr(String clientId, String propName) {
		
		Object value = getConfigValue(clientId, propName);
		
		if(value == null) {
			return null;
		} else {
			return value.toString();
		}
	}
	
	/**
	 * 시스템 JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getSystemMetrics() throws Exception {
		
		return KafkaAcquisitor.jmx.getByQuery(
			"java.lang:type=OperatingSystem",
			"SystemCpuLoad",
			"FreePhysicalMemorySize"
		);
	}
	
	/**
	 * Broker JMX 성능 정보 수집
	 * 
	 * @return Broker JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getBrokerMetrics() throws Exception {
		
		return KafkaAcquisitor.jmx.getByQuery(
			"kafka.log:type=*,name=*,topic=*,partition=*",
			"kafka.server:type=*,name=*",
			"kafka.controller:type=*,name=*",
			"kafka.coordinator.group:type=*,name=*",
			"kafka.network:type=*,name=*"
		);
	}
	
	/**
	 * JMX 성능 정보 수집
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getMetrics(String clientId) throws Exception {
		
		ClientType clientType = getClientType(clientId);
		
		if(ClientType.PRODUCER == clientType) {
			return getProducerMetrics(clientId);
		} else if(ClientType.CONSUMER == clientType) {
			return getConsumerMetrics(clientId);
		}
		
		return Map.of();
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getProducerMetrics(String clientId) throws Exception {
		
		// 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
		if(StringUtil.isBlank(clientId) == true) {
			clientId = "*";
		}
		
		// 성능 조회 및 반환
		return KafkaAcquisitor.jmx.getByQuery(
			"kafka.producer:type=producer-metrics,client-id=" + clientId
		);
	}
	
	/**
	 * Producer JMX 성능 정보 수집<br>
	 * 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getConsumerMetrics(String clientId) throws Exception {
		
		// 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
		if(StringUtil.isBlank(clientId) == true) {
			clientId = "*";
		}
		
		// 성능 조회 및 반환
		return KafkaAcquisitor.jmx.getByQuery(
			"kafka.consumer:type=consumer-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-fetch-manager-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-topic-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-coordinator-metrics,client-id=" + clientId
		);
	}
	
	/**
	 * 클라이언트 접속 정보 맵 반환
	 * 
	 * @return 클라이언트 접속 정보 맵 
	 */
	public static Map<String, Long> getClientConnMap() {
		return KafkaAcquisitor.clientConnMap;
	}

	/**
	 * 클라이언트 접속 정보 저장
	 * 
	 * @param clientIp 클라이언트 아이피
	 * @param clientId 클라이언트 아이디
	 */
	public static void putClientConn(String clientIp, String clientId) {
		
		// 클라이언트 아이피:아이디 키를 만듦
		String clientIpId = KafkaUtil.makeClientIpIdPair(clientIp, clientId);
		
		// 만일 기존에 이미 있으면 업데이트 하지 않음
		// 최초 접속 시간을 기록하기 위함
		if(KafkaAcquisitor.clientConnMap.containsKey(clientIpId) == true) {
			return;
		}
		
		// 신규 접속 정보 저장
		KafkaAcquisitor.clientConnMap.put(
			clientIpId,
			System.currentTimeMillis()
		);
	}
}
