package com.redeye.appagent.loader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.redeye.appagent.Config;
import com.redeye.appagent.loader.entity.APIContextDTO;
import com.redeye.appagent.util.CronJob;
import com.redeye.appagent.util.JSONUtil;
import com.redeye.appagent.util.LogUtil;
import com.redeye.appagent.util.RESTUtil;
import com.redeye.appagent.util.StringUtil;
import com.redeye.appagent.util.CronJob.Job;

/**
 * 성능 정보 저장용 로더 실행 클래스<br>
 * 외부 API 호출하여 성능 정보 저장
 * 
 * @author jmsohn
 */
public class APILoaderCronJob {
	
	
	/**
	 * API 호출을 위한 기준 패스<br>
	 * 예) http://192.168.1.1:8080
	 */
	private final String basePath;
	
	/** 크론잡에서 로드를 수행하는 잡 객체 */
	private LoaderJob loaderJob;
	
	/** 스케쥴에 따라 저장 API를 실행하는 크론잡 */
	private CronJob cronJob;
	
	
	/**
	 * 생성자
	 * 
	 * @param basePath API 호출을 위한 기준 패스
	 * @param schedule 저장 API 실행 스케쥴
	 * @param loaderList API 저장 로더 목록
	 */
	public APILoaderCronJob(
		String basePath,
		String schedule,
		List<APILoader> loaderList
	) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(basePath) == true) {
			throw new IllegalArgumentException("'basePath' is null or blank.");
		}
		
		if(StringUtil.isBlank(schedule) == true) {
			throw new IllegalArgumentException("'schedule' is null or blank.");
		}
		
		// 기준 패스 설정
		this.basePath = basePath;
		
		// 크론잡에서 로드를 수행하는 잡 객체 및 크론잡 생성
		this.loaderJob = new LoaderJob(loaderList);
		this.cronJob = new CronJob(schedule, this.loaderJob);
	}
	
	/**
	 * 크론잡에서 로드를 수행하는 잡 클래스<br>
	 * 가 로더들을 멀티스레드로 수행시키기 위한 클래스
	 * 
	 * @author jmsohn
	 */
	class LoaderJob implements Job {


		/** 호스트 아이디 URL */
		private static final String HOST_ID_URL = "/api/host/info/id?organCode=%s&domainCode=%s&hostName=%s";

		/** 어플리케이션 아이디 URL */
		private static final String APP_ID_URL = "/api/app/info/id?organCode=%s&domainCode=%s&appCode=%s";


		/** 호스트 아이디 */
		private long hostId = -1;

		/** 어플리케이션 아이디 */
		private long appId = -1;
		
		/** API 저장 로더 목록 */
		private final List<APILoader> loaderList;

		/** API 호출 작업 실행시 사용할 스레드 풀 */
		private ExecutorService pool;
		
		
		/**
		 * 생성자
		 * 
		 * @param loaderList API 저장 로더 목록
		 */
		LoaderJob(List<APILoader> loaderList) {
			
			// 입력값 저장
			if(loaderList == null) {
				throw new IllegalArgumentException("'loaderList' is null.");
			}
			
			if(loaderList.size() == 0) {
				throw new IllegalArgumentException("'loaderList' has no element.");
			}
			
			// API 저장 로더 목록 설정
			this.loaderList = loaderList;
			
			// 스레드 풀 생성 및 설정
			this.pool = Executors.newFixedThreadPool(this.loaderList.size());
		}
		
		@Override
		public void run(long startTime, long nextTime) {

			try {
				
				// basePath가 설정된 경우에만 수행
				// 설정되어 있지 않은 경우 스킵
				if(StringUtil.isBlank(basePath) == false) {
					
					// 호스트 아이디 설정
					if(this.hostId == -1) {
						this.hostId = getHostId();
					}
	
					// 어플리케이션 아이디 설정
					if(this.appId == -1) {
						this.appId = getAppId();
					}
				}
				
			} catch(Exception ex) {
				
				// 예외 발생시, 로더를 실행하지 않음
				LogUtil.log(ex);
				return;
			}
			
			// API 컨택스트 객체 생성
			APIContextDTO context = new APIContextDTO(
				this.hostId,
				this.appId,
				basePath,
				startTime,
				nextTime
			);
			
			// 목록의 각 로더들을 하나씩 멀티스레드로 수행함
			for(APILoader loader: loaderList) {
				
				pool.execute(new Runnable() {
					
					@Override
					public void run() {
						loader.load(context);
					}
				});
			}
		}

		/**
		 * 호스트 아이디 반환
		 *
		 * @return 호스트 아이디
		 */
		private long getHostId() throws Exception {
			
			AtomicLong hostId = new AtomicLong(-1L);
			
			RESTUtil.get(

				// 접속 URL
				String.format(
					basePath + HOST_ID_URL,
					Config.ORGAN_CODE.getValue(),
					Config.DOMAIN_CODE.getValue(),
					Config.HOST_NAME.getValue()
				),

				// 응답 처리
				(respCode, respMsg) -> {
					
					if(respCode == 200) {
						try {
							
							hostId.set(
								(long)JSONUtil.parseMap(respMsg).get("id")
							);
							
						} catch(Exception ex) {
							
							LogUtil.log(ex);
							hostId.set(-1L);
						}
					}
				}
			);
			
			return hostId.get();
		}

		/**
		 * 어플리케이션 아이디 반환
		 *
		 * @return 어플리케이션 아이디
		 */
		private long getAppId() throws Exception {
			
			AtomicLong hostId = new AtomicLong(-1L);
			
			RESTUtil.get(

				// 접속 URL
				String.format(
					basePath + APP_ID_URL,
					Config.ORGAN_CODE.getValue(),
					Config.DOMAIN_CODE.getValue(),
					Config.APP_CODE.getValue()
				),

				// 응답 처리
				(respCode, respMsg) -> {
					
					if(respCode == 200) {
						try {
							
							hostId.set(
								(long)JSONUtil.parseMap(respMsg).get("id")
							);
							
						} catch(Exception ex) {
							
							LogUtil.log(ex);
							hostId.set(-1L);
						}
					}
				}
			);
			
			return hostId.get();
		}
		
		/**
		 * 멀티 스레드 중단 메소드
		 */
		void stop() {
			this.pool.shutdown();
		}
	}
	
	/**
	 * API 호출 크론잡 시작
	 * 
	 * @return 현재 객체
	 */
	public APILoaderCronJob start() {
		
		this.cronJob.start();
		
		return this;
	}
	
	/**
	 * API 호출 크론잡 종료
	 * 
	 * @return 현재 객체
	 */
	public APILoaderCronJob stop() {
		
		this.cronJob.stop();
		
		return this;
	}
}
