package com.redeye.appagent.loader;

import java.util.ArrayList;
import java.util.List;

import com.redeye.appagent.Config;
import com.redeye.appagent.Context;
import com.redeye.appagent.util.LogUtil;

/**
 * API Loader Service 클래스<br>
 * 외부 API 서비스(SentryAPI)로 성능 정보를 로드하는 서비스
 *
 * @author jmsohn
 */
public class LoaderService {
	
	
	/** API를 통한 성능 정보 저장 크론잡 객체 */
	private static APILoaderCronJob loader;
	

	/**
	 * API Loader Service 기동
	 * 
	 * @param contextList 컨텍스트 목록
	 */
	public static void start(List<Context> contextList) throws Exception {

		// ------------------------
		// 로더 기동 여부 확인
		String useLoader = Config.LOADER_YN.getValue();
		if(
			"Y".equalsIgnoreCase(useLoader) == false
			|| contextList == null
			|| contextList.size() == 0
		) {
			LogUtil.log("metrics api loader is disabled.");
			return;
		}
		
		// ------------------------
		// API 호출 로더 목록 설정
		List<APILoader> loaderList = new ArrayList<>();
		
		for(Context context: contextList) {
			loaderList.addAll(context.getAPILoaderList());
		}
		
		// ------------------------
		// API 호출 로더 생성 및 기동
		loader = new APILoaderCronJob(
			Config.LOADER_API_SERVER.getValue(),
			Config.LOADER_SCHEDULE.getValue(),
			loaderList
		);
		
		loader.start();
		
		LogUtil.log("metrics api loader is started.");
	}
}
