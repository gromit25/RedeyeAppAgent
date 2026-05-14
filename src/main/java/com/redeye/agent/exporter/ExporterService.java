package com.redeye.agent.exporter;

/**
 * Http Exporter Service 클래스<br>
 * 외부에서 Http를 통해 성능 정보를 접근할 수 있는 서비스
 *
 * @author jmsohn
 */
public class ExporterService {

	/**
	 * Http Exporter Service 기동
	 *
	 * @param contextList 컨텍스트 목록
	 */
	public static void start(List<Context> contextList) {

		// 컨텍스트가 없는 경우 반환
		if(contextList == null || contextList.size() == 0) {
			return;
		}
    
		// ------------------------
		// 로더 기동 여부 확인
		String useLoader = Config.LOADER_YN.value;
		if("Y".equalsIgnoreCase(useLoader) == false) {
			LogUtil.log("metrics api loader is disabled.");
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
		loader = new APILoaderCronJob(basePath, schedule, loaderList);
		loader.start();
		
		LogUtil.log("metrics api loader is started.");
  }
}
