package com.redeye.appagent.util.daemon.intervalstat;

import lombok.Getter;

/**
 * 시간 DTO 클래스
 * 
 * @author jmsohn
 */
public class IntervalTimeVO {
	
	
	/** 구분 아이디 */
	@Getter
	private final String id;
	
	/** 발생 시간 */
	@Getter
	private final long timestamp;
	
	
	/**
	 * 생성자
	 * 
	 * @param id 아이디
	 * @param timestamp 발생 시간
	 */
	public IntervalTimeVO(String id, long timestamp) {
		this.id = id;
		this.timestamp = timestamp;
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
			.toString();
	}
}
