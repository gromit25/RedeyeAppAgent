package com.redeye.agent.loader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.redeye.agent.util.CronJob;
import com.redeye.agent.util.CronJob.Job;
import com.redeye.agent.util.StringUtil;

/**
 * 성능 정보 저장용 로더 클래스<br>
 * 외부 API 로 성능 정보 저장
 * 
 * @author jmsohn
 */
public class MetricsAPILoader {
	
	
	/**
	 * API 호출을 위한 기준 패스<br>
	 * 예) http://192.168.1.1:8080
	 */
	private final String basePath;
	
	/** API 저장 로더 목록 */
	private final List<APILoader> loaderList;

	/** 스케쥴에 따라 저장 API를 실행하는 크론잡 */
	private CronJob cronJob;
	
	
	/**
	 * 생성자
	 * 
	 * @param basePath API 호출을 위한 기준 패스
	 * @param schedule 저장 API 실행 스케쥴
	 * @param loaderList API 저장 로더 목록
	 */
	public MetricsAPILoader(
		String basePath,
		String schedule,
		List<APILoader> loaderList
	) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(basePath) == true) {
			throw new IllegalArgumentException("'basePath(RE_LOADER_API_SERVER env)' is null or blank.");
		}
		
		if(StringUtil.isBlank(schedule) == true) {
			throw new IllegalArgumentException("'schedule(RE_LOADER_SCHEDULE env)' is null or blank.");
		}
		
		if(loaderList == null) {
			throw new IllegalArgumentException("'loaderList' is null.");
		}
		
		if(loaderList.size() == 0) {
			throw new IllegalArgumentException("'loaderList' has no element.");
		}
		
		// 기준 패스 설정
		this.basePath = basePath;
		
		// API 저장 로더 목록 설정
		this.loaderList = loaderList;
		
		// 크론잡 생성
		this.cronJob = new CronJob(
			schedule,
			new Job() {
				
				/** API 호출 작업 실행시 사용할 스레드 풀 */
				private ExecutorService pool = Executors.newFixedThreadPool(loaderList.size());
				
				@Override
				public void run(long startTime, long nextTime) {
					
					//
					for(APILoader loader: loaderList) {
						
						//
						pool.execute(new Runnable() {
							
							@Override
							public void run() {
								loader.load(basePath, startTime, nextTime);
							}
						});
					}
				}
			} // End of Job Class
		);
	}
	
	/**
	 * API 호출 크론잡 시작
	 * 
	 * @return 현재 객체
	 */
	public MetricsAPILoader start() {
		
		this.cronJob.start();
		
		return this;
	}
	
	/**
	 * API 호출 크론잡 종료
	 * 
	 * @return 현재 객체
	 */
	public MetricsAPILoader stop() {
		
		this.cronJob.stop();
		
		return this;
	}
}
