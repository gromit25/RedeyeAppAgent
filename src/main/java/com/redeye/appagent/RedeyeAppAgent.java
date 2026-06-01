package com.redeye.appagent;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.redeye.appagent.exporter.ExporterService;
import com.redeye.appagent.loader.LoaderService;

/**
 * 에이전트 클래스
 * 
 * @author jmsohn
 */
public class RedeyeAppAgent {
	
	/**
	 * 메인 메소드
	 * 
	 * @param args javaagent 옵션 문자열
	 * @param inst java 인스트루먼트 클래스
	 */
	public static void premain(String args, Instrumentation inst) {
		
		try {
			
			// 환경 변수 획득 및 설정
			Config.init();

			// 익스포터 서비스 기동
			ExporterService.start(ContextManager.getContextList());
			
			// 로더 서비스 기동
			LoaderService.start(ContextManager.getContextList());
			
			// 클래스 변환기(transformer) 추가
			addTransformer(inst, ContextManager.getContextList());
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 컨택스트 목록의 클래스 변환기(transformer)들을 추가 
	 * 
	 * @param inst 인스트루먼트 객체
	 * @param contextList 컨텍스트 목록
	 */
	private static void addTransformer(Instrumentation inst, List<Context> contextList) {
		
		for(Context context: contextList) {
			context.addTransformer(inst);;
		}
	}
}
