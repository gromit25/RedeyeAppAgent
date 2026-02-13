package com.redeye.agent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.agent.kafka.KafkaContext;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.loader.MetricsAPILoader;
import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.WebUtil;
import com.redeye.agent.util.http.service.HttpService;
import com.redeye.agent.util.http.service.annotation.Controller;

/**
 * kafka 정보 수집기 클래스
 * 
 * @author jmsohn
 */
public class RedeyeAgent {

	
	/** 컨텍스트 목록 */
	private static List<Context> contextList; 
	
	/** http exporter 서비스*/
	private static HttpService service;
	
	/** API를 통한 성능 정보 저장 크론잡 객체 */
	private static MetricsAPILoader loader;
	

	// 클래스 로딩시 초기화
	static {
		
		// Exporter 컨텍스트 목록 초기화
		contextList = new CopyOnWriteArrayList<>();
		
		// Kafka Exporter 컨텍스트 추가
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
			
			// exporter 서비스 기동
			startExporterService();
			
			// 로더 서비스 기동
			startLoaderService();
			
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
	 * 
	 * 
	 * @param inst java 인스트루먼트 클래스
	 */
	private static void addTransformer(Instrumentation inst) {
		
		for(Context context: contextList) {
			context.addTransformer(inst);;
		}
	}
	
	/**
	 * http exporter 서비스 기동
	 */
	private static void startExporterService() throws Exception {
		
		// ------------------------
		// 서버 기동 여부 확인
		String useLoader = Config.EXPORTER_YN.value;
		if("Y".equalsIgnoreCase(useLoader) == false) {
			System.out.println("http exporter is disabled.");
			return;
		}
		
		// ------------------------
		// 서버 기동을 위한 옵션 획득
		
		// 익스포터 서버 환경 변수 설정값 획득
		String hostPort = Config.EXPORTER_SERVER.value;
		
		// 익스포터 서버명 변수
		String host = "0.0.0.0";
		
		// 익스포터 서버 포트 변수
		int port = 0; // 설정 값이 없는 경우, 서버에서 비어 있는 랜덤 포트를 사용
		
		// 익스포터 호스트 및 포트 번호 획득
		// 없을 경우 기본 설정 값 사용
		if(StringUtil.isBlank(hostPort) == false) {

			if(hostPort.matches("[0-9]+") == true) {
				
				port = Integer.parseInt(hostPort);
				
			} else {
				
				String[] hostPortAry = WebUtil.parseHostPort(hostPort);
				
				host = hostPortAry[0];
				port = Integer.parseInt(hostPortAry[1]);
			}
		}
		
		// 익스포터 서버의 스레드 개수 설정
		int threadCount = Integer
			.parseInt(
				Config.EXPORTER_THREAD_COUNT.value
			);
		
		// -----------------------------
		// Http 서비스 기동
		
		// Http 서버 생성
		service = new HttpService(host, port, threadCount);
		
		// 컨텍스트의 컨트롤러 추가
		for(Context context: contextList) {
			for(Object controller: context.getWebControllerList()) {
				
				// Contoller 어노테이션이 붙은 경우만 등록함
				Controller controllerAnnotation = controller.getClass().getAnnotation(Controller.class);
				if(controllerAnnotation == null) {
					continue;
				}
				
				service.addController(controllerAnnotation);
			}
		}
		
		// Http 서버 기동
		service.start();
		
		System.out.println("http exporter(" + service.getHostStr() + ") is started.");
	}
	
	/**
	 * API 호출 로더 기동
	 */
	private static void startLoaderService() throws Exception {
		
		// ------------------------
		// 로더 기동 여부 확인
		String useLoader = Config.LOADER_YN.value;
		if("Y".equalsIgnoreCase(useLoader) == false) {
			System.out.println("metrics api loader is disabled.");
			return;
		}
		
		// ------------------------
		// 로더 기동을 위한 옵션 획득
		
		// 호출할 API의 기준 패스 획득
		String basePath = Config.LOADER_API_SERVER.value;
		
		// API 호출 스케쥴 획득
		String schedule = Config.LOADER_SCHEDULE.value;
		
		// ------------------------
		// API 호출 로더 목록 설정
		List<APILoader> loaderList = new ArrayList<>();
		
		for(Context context: contextList) {
			loaderList.addAll(context.getAPILoaderList());
		}
		
		// ------------------------
		// API 호출 로더 생성 및 기동
		loader = new MetricsAPILoader(basePath, schedule, loaderList);
		loader.start();
		
		System.out.println("metrics api loader started.");
	}
}
