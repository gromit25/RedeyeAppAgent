package com.redeye.agent;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

import com.redeye.agent.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.kafka.acquisitor.KafkaTransformer;
import com.redeye.agent.kafka.exporter.service.KafkaBrokerController;
import com.redeye.agent.kafka.exporter.service.KafkaClientController;
import com.redeye.agent.kafka.exporter.service.KafkaConfigController;
import com.redeye.agent.kafka.exporter.service.KafkaMetricsController;
import com.redeye.agent.loader.APILoader;
import com.redeye.agent.loader.MetricsAPILoader;
import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.WebUtil;
import com.redeye.agent.util.http.service.HttpService;

/**
 * kafka 정보 수집기 클래스
 * 
 * @author jmsohn
 */
public class RedeyeAgent {

	
	/** http exporter 서비스*/
	private static HttpService service;
	
	/** API를 통한 성능 정보 저장 크론잡 객체 */
	private static MetricsAPILoader loader;
	
	
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

			// kafka 메소드 변환
			KafkaTransformer.addKafkaTransformer(inst);
			
			// kafka 정보 수집기 초기화
			KafkaAcquisitor.init();
			
			// exporter 서비스 기동
			startExporterService();
			
			// 로더 서비스 기동
			startLoaderService();
			
		} catch(Exception ex) {
			ex.printStackTrace();
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
		
		// kafka 컨트롤러 추가
		service.addController(new KafkaBrokerController());
		service.addController(new KafkaClientController());
		service.addController(new KafkaConfigController());
		service.addController(new KafkaMetricsController());
		
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
		// API 호출 로더 목록 설정 - 현재 테스트용
		List<APILoader> loaderList = List.of(new APILoader() {

			@Override
			public void load(String basePath, long startTime, long endTime) {
				
				ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
				int threadCount = threadBean.getThreadCount();
				
				System.out.println("### DEBUG LOADER: " + basePath + ", " + startTime + ", " + threadCount);
			}
		});
		
		// ------------------------
		// API 호출 로더 생성 및 기동
		loader = new MetricsAPILoader(basePath, schedule, loaderList);
		loader.start();
		
		System.out.println("metrics api loader started.");
	}
}
