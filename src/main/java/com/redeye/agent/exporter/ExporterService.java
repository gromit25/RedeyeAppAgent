package com.redeye.agent.exporter;

import java.util.List;

import com.redeye.agent.Config;
import com.redeye.agent.Context;
import com.redeye.agent.util.LogUtil;
import com.redeye.agent.util.StringUtil;
import com.redeye.agent.util.WebUtil;
import com.redeye.agent.util.http.service.HttpService;
import com.redeye.agent.util.http.service.annotation.Controller;

/**
 * Http Exporter Service 클래스<br>
 * 외부에서 Http를 통해 성능 정보를 접근할 수 있는 서비스
 *
 * @author jmsohn
 */
public class ExporterService {
	
	
	/** http exporter 서비스*/
	private static HttpService service;
	

	/**
	 * Http Exporter Service 기동
	 *
	 * @param contextList 컨텍스트 목록
	 */
	public static void start(List<Context> contextList) throws Exception {

		// ------------------------
		// 서버 기동 여부 확인
		String useLoader = Config.EXPORTER_YN.getValue();
		if("Y".equalsIgnoreCase(useLoader) == false) {
			LogUtil.log("http exporter is disabled.");
			return;
		}
		
		// ------------------------
		// 서버 기동을 위한 옵션 획득
		
		// 익스포터 서버 환경 변수 설정값 획득
		String hostPort = Config.EXPORTER_SERVER.getValue();
		
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
				Config.EXPORTER_THREAD_COUNT.getValue()
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
		
		LogUtil.log("http exporter(" + service.getHostStr() + ") is started.");
	}
}
