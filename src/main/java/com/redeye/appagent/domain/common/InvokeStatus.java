package com.redeye.appagent.domain.common;

/**
 * 반복 호출에 따른 처리를 위한 상태<br>
 * executeQuery의 경우에 ORM 툴이나 스프링 부트에 의해 반복 호출이 발생함<br>
 * 따라서, 단순히 execute onExit에서 성능 측정을 하게 되면<br>
 * 실제로 한번 발생한 쿼리가 여러번 발생한 것으로 오인됨
 * 
 * @author jmsohn
 */
public enum InvokeStatus {
	
	/** 호출 상태 */
	INVOKE_ENTER,
	
	/** 호출 완료 상태 */
	INVOKE_EXIT
}
