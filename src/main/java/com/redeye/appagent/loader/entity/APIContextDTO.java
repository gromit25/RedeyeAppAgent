package com.redeye.appagent.loader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 컨텍스트 클래스
 * 
 * @author jmsohn
 */
@Getter
@AllArgsConstructor
public class APIContextDTO {
	
	/** 호스트 아이디 */
	private final long hostId;
	
	/** 어플리케이션 아이디 */
	private final long appId;
	
	/** 기준 패스 */
	private final String basePath;
	
	/** 시작 시간 */
	private final long startTime;
	
	/** 종료 시간 */
	private final long endTime;
}
