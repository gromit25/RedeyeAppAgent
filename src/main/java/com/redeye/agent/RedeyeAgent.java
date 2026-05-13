package com.redeye.agent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.agent.domain.env.EnvContext;
import com.redeye.agent.domain.jdbc.JDBCContext;
import com.redeye.agent.domain.kafka.KafkaContext;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.loader.APILoaderCronJob;
import com.redeye.agent.util.LogUtil;
import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.WebUtil;
import com.redeye.agent.util.http.service.HttpService;
import com.redeye.agent.util.http.service.annotation.Controller;

/**
 * 에이전트 클래스
 * 
 * @author jmsohn
 */
public class RedeyeAgent {

	
	/** 컨텍스트 목록 */
	private static List<Context> contextList; 
	
	/** http exporter 서비스*/
	private static HttpService service;
	
	/** API를 통한 성능 정보 저장 크론잡 객체 */
	private static APILoaderCronJob loader;
	

	// 클래스 로딩시 초기화
	static {
		
		// 컨텍스트 목록 초기화
		contextList = new CopyOnWriteArrayList<>();
		
		// 환경 변수 컨텍스트 추가
		contextList.add(new EnvContext());
		
		// JDBC 컨텍스트 추가
		contextList.add(new JDBCContext());
		
		// Kafka 컨텍스트 추가
		contextList.add(new KafkaContext());
	}
	
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

			// 컨택스트 객체 초기화 메소드 호출
			initContext();
			
			// 익스포터 서비스 기동
			ExporterService.start(contextList);
			
			// 로더 서비스 기동
			LoaderService.start(contextList);
			
			// 클래스 변환기(transformer) 추가
			addTransformer(inst);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 컨택스트 객체 초기화 메소드 호출
	 */
	private static void initContext() {
		
		for(Context context: contextList) {
			context.init();
		}
	}
	
	/**
	 * 컨택스트 목록의 클래스 변환기(transformer)들을 추가 
	 * 
	 * @param inst java 인스트루먼트 클래스
	 */
	private static void addTransformer(Instrumentation inst) {
		
		for(Context context: contextList) {
			context.addTransformer(inst);;
		}
	}
}
