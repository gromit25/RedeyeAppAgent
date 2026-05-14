package com.redeye.agent.exporter;

/**
 *
 *
 * @author jmsohn
 */
public class ExporterService {

	/**
	 *
	 *
	 * @param contextList
	 */
	public static void start(List<Context> contextList) {

		//
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
