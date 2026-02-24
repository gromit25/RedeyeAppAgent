package com.redeye.agent.util.daemon.elapsedstat;

import lombok.Getter;

/**
 * 시간 DTO 클래스
 * 
 * @author jmsohn
 */
public class ElapsedTimeVO {
	
	
	/** 통계 아이디 */
	@Getter
	private final String id;
	
	/** 발생 시간 */
	@Getter
	private final long timestamp;
	
	/** 수행 시간 */
	@Getter
	private final long elapsedTime;
	
	
	/**
	 * 생성자
	 * 
	 * @param id 아이디
	 * @param timestamp 발생 시간
	 * @param elapsedTime 수행 시간
	 */
	public ElapsedTimeVO(String id, long timestamp, long elapsedTime) {
		this.id = id;
		this.timestamp = timestamp;
		this.elapsedTime = elapsedTime;
	}
	
	/**
	 * 객체 정보를 문자열로 변환하여 반환
	 */
	@Override
	public String toString() {
		
		return new StringBuilder()
			.append(this.id)
			.append(":")
			.append(this.timestamp)
			.append(":")
			.append(this.elapsedTime)
			.toString();
	}
}
