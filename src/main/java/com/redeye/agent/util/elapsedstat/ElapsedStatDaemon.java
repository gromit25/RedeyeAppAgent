package com.redeye.agent.util.elapsedstat;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.agent.util.daemon.QueueDaemon;
import com.redeye.agent.util.stat.Parameter;

import lombok.Getter;

/**
 * 실행 시간 통계 데몬 클래스
 * 
 * @author jmsohn
 */
public class ElapsedStatDaemon {
	
	
	/** 클라이언트 아이디별 최종 시간 데이터 - key: 클라이언트 아이디, value: 시간 데이터 */
	private final Map<String, Long> elapsedTimeMap = new ConcurrentHashMap<>();
	
	/** 클라이언트 아이디별 시간 통계 데이터 - key: 클라이언트 아이디, value: 시간 통계 데이터 */
	private final Map<String, Parameter> elapsedTimeStatMap = new ConcurrentHashMap<>();

	/** 시간 수집 큐 - 클라이언트 별 시간 데이터를 수신하는 큐 */
	@Getter
	private final BlockingQueue<ElapsedTimeDTO> queue = new LinkedBlockingQueue<>();

	/** 통계 생성 데몬 - 시간 수집 큐에서 데이터를 받아 통계 데이터를 생성하는 데몬 */
	private QueueDaemon<ElapsedTimeDTO> elapsedStatDaemon = null;
	
	
	/**
	 * 생성자
	 */
	public ElapsedStatDaemon() {
		
		// 통계 생성 데몬 생성
		this.elapsedStatDaemon = new QueueDaemon<>(
			this.queue,
			data -> {
				
				// 기존 값 저장 
				Long prePollTime = elapsedTimeMap.get(data.getId());
				
				// 시간 저장
				elapsedTimeMap.put(data.getId(), data.getTimestamp());
				
				// 통계 정보 저장
				Parameter timeStat = elapsedTimeStatMap.computeIfAbsent(
					data.getId(), key -> new Parameter()
				);
				
				if(prePollTime != null) {
					long interval = data.getTimestamp() - prePollTime;
					timeStat.add(interval);
				}
				
				//TODO
				//System.out.println("### STAT : \n" + timeStat);
			}
		);
	}
	
	/**
	 * 통계 생성 데몬 기동
	 * 
	 * @return 현재 객체
	 */
	public ElapsedStatDaemon start() {
		this.elapsedStatDaemon.start();
		return this;
	}
	
	/**
	 * 통계 생성 데몬 중지
	 * 
	 * @return 현재 객체
	 */
	public ElapsedStatDaemon stop() {
		this.elapsedStatDaemon.stop();
		return this;
	}
	
	/**
	 * 통계정보 맵 반환
	 * 
	 * @return 통계정보 맵
	 */
	public Map<String, Parameter> getStat() {
		return this.elapsedTimeStatMap;
	}
	
	/**
	 * 클라이언트 아이디의 통계정보 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return 통계 정보
	 */
	public Parameter getStat(String clientId) {
		return this.getStat().get(clientId);
	}
}
