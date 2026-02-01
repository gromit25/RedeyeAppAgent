package com.redeye.agent.kafka.acquisitor.advice.broker;

import java.lang.reflect.Method;
import java.util.Map;

import com.redeye.agent.kafka.acquisitor.KafkaAcquisitor;

import net.bytebuddy.asm.Advice;

/**
 * 카프카 브로커 설정 객체 생성자 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConfigAdvice {
	
	/**
	 * 브로커 설정 객체 생성 후 훅킹
	 * 
	 * @param config 브로커 설정 정보
	 */
	@SuppressWarnings("unchecked")
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object config) {
		
		try {
			
			// 설정 값을 가져옴
			Method valuesMethod = config.getClass().getMethod("values");
			if(valuesMethod == null) {
				return;
			}
			
			KafkaAcquisitor.setBrokerConfigMap((Map<String, Object>)valuesMethod.invoke(config));
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
