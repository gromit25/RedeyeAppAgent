package com.redeye.appagent.util.daemon.elapsedstat;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import com.redeye.appagent.util.daemon.QueueDaemon;
import com.redeye.appagent.util.stat.Parameter;

import lombok.Getter;

/**
 * 실행 시간 통계 데몬 클래스<br>
 * 실행 시간 값이 있는 경우
 * 
 * @author jmsohn
 */
public class ElapsedStatDaemon {
	
	
	/** 통계 아이디별 최종 시간 데이터 - key: 통계 아이디, value: 시간 데이터 */
	private final Map<String, Long> elapsedTimeMap = new ConcurrentHashMap<>();
	
	/** 통계 아이디별 시간 통계 데이터 - key: 통계 아이디, value: 시간 통계 데이터 */
	private final Map<String, Parameter> elapsedTimeStatMap = new ConcurrentHashMap<>();

	/** 시간 수집 큐 - 클라이언트 별 시간 데이터를 수신하는 큐 */
	@Getter
	private final BlockingQueue<ElapsedTimeVO> queue = new LinkedBlockingQueue<>();

	/** 통계 생성 데몬 - 시간 수집 큐에서 데이터를 받아 통계 데이터를 생성하는 데몬 */
	private QueueDaemon<ElapsedTimeVO> elapsedStatDaemon = null;
	
	
	/**
	 * 생성자
	 */
	public ElapsedStatDaemon() {
		
		// 통계 생성 데몬 생성
		this.elapsedStatDaemon = new QueueDaemon<>(
			this.queue,
			data -> {
				
				// 시간 저장
				elapsedTimeMap.put(data.getId(), data.getTimestamp());
				
				// 통계 정보 저장
				Parameter timeStat = elapsedTimeStatMap.computeIfAbsent(
					data.getId(), key -> new Parameter()
				);
				
				timeStat.add(data.getElapsedTime());
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
	 * 통계 데이터 삭제하면서 처리 수행
	 * 
	 * @param consumer 통계 데이터 처리 객체
	 */
	public void flush(BiConsumer<String, Parameter> consumer) {
		
		for(String id: this.getStat().keySet()) {
			
			// 통계 정보 확보 및 삭제
			Parameter stat = this.getStat().remove(id);
			
			// 통계 정보 처리 수행
			consumer.accept(id, stat);
		}
	}
}
