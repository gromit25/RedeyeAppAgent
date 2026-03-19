package com.redeye.agent;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;

import com.redeye.agent.util.StringUtil;

import lombok.Getter;

/**
 * Agent 설정 값<br>
 * 환경 변수에서 읽어와 설정함
 * 
 * @author jmsohn
 */
public enum Config {
	

	/** 호스트 명 */
	HOST_NAME(
		"RE_HOST_NM",
		null,
		"호스트 명",
		false
	),
	
	/** 프로세스 아이디(pid) */
	APP_PID(
		null,
		null,
		"프로세스 아이디(pid)",
		false
	),
	
	/** 캐릭터셋 */
	CHARSET(
		"RE_CHARSET",
		Charset.defaultCharset().name(),
		"캐릭터셋 설정",
		false
	),
	
	//--- 관리 코드 관련
	
	/** 기관 코드 */
	ORGAN_CODE(
		"RE_ORGAN_CODE",
		null,
		"기관 코드",
		true
	),
	
	/** 도메인 코드 */
	DOMAIN_CODE(
		"RE_DOMAIN_CODE",
		null,
		"도메인 코드",
		true
	),
	
	/** 어플리케이션 코드 */
	APP_CODE(
		"RE_APP_CODE",
		null,
		"어플리케이션 코드",
		true
	),
	
	APP_PACKAGE(
		"RE_APP_PACKAGE",
		"",
		"어플리케이션 패키지명 - ex) com.redeye",
		false
	),
	
	LOG_USE_YN(
		"RE_LOG_USE",
		"Y",
		"로그 출력 여부(Y/N)",
		false
	),
	
	//---------------------------
	
	/** 익스포터 서비스 사용 여부 */
	EXPORTER_YN(
		"RE_EXPORTER",
		"N",
		"익스포터 서비스 사용 여부(Y/N)",
		false
	),
	
	/** 익스포터 서비스 서버명(ip), 포트 설정 */
	EXPORTER_SERVER(
		"RE_EXPORTER_SERVER",
		"0.0.0.0:0",
		"익스포터 서비스 서버명(ip), 포트 설정",
		false
	),
	
	/** 익스포터 서버의 스레드 개수 */
	EXPORTER_THREAD_COUNT(
		"RE_EXPORTER_THREAD_COUNT",
		"-1",
		"익스포터 서버의 스레드 개수",
		false
	),
	
	//---------------------------
	
	/** 성능 정보 로더 사용 여부 */
	LOADER_YN(
		"RE_LOADER",
		"N",
		"성능 정보 로더 사용 여부(Y/N)",
		false
	),
	
	/** 로더에서 호출할 API의 기준 패스 */
	LOADER_API_SERVER(
		"RE_LOADER_API_SERVER",
		null,
		"로더에서 호출할 API의 기준 패스 - ex) http://localhost:8080",
		false
	),
	
	/** API 호출 스케쥴 */
	LOADER_SCHEDULE(
		"RE_LOADER_SCHEDULE",
		"*/10 * * * * *",
		"API 호출 스케쥴",
		false
	);

	//---------------------------
	
	/**
	 * 설정 초기화 수행
	 */
	public static void init() throws Exception {
		
		// 환경 변수에서 설정 값을 읽어와 값을 설정
		Config[] configs = Config.values();
		
		for(Config config: configs) {
			
			// 환경 변수로 설정 불가인 변수인 경우 스킵함
			// 환경 변수명이 없는 경우 설정 불가임
			if(config.key == null) {
				continue;
			}
			
			// 환경 변수의 값을 가져와 설정
			config.value = getEnv(config);
		}
		
		// PID와 현재 서버명 설정
		// runtimeName = PID@현재서버명 형태로 되어 있음
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		
		if(runtimeName != null) {
			
			String[] splitedRuntimeName = runtimeName.split("@");
			
			if(splitedRuntimeName.length > 1) {
				
				// PID 설정
				Config.APP_PID.value = splitedRuntimeName[0];
				
				// 호스트 명 설정, 환경변수에 의해 설정되지 않았을 경우만
				if(Config.HOST_NAME.value == null) {
					Config.HOST_NAME.value = splitedRuntimeName[1];
				}
			}
		}
	}
	
	/**
	 * 환경 변수 설정 값 반환
	 * 
	 * @param config 환경 변수 정보
	 * @return 환경 변수 설정 값
	 */
	private static String getEnv(Config config) {
		
		// 설정되어 있는 환경 변수 값 획득
		String value = System.getenv(config.key);
		
		// 필수 여부 검사
		if(value == null && config.isMandatory == true) {
			throw new IllegalArgumentException("'" + config.key + "' is not set.");
		}
		
		// 환경 변수 값이 있을 경우 환경 변수 값 반환
		// 없을 경우 디폴트 값 반환
		if(StringUtil.isBlank(value) == true) {
			return config.defaultValue;
		} else {
			return value;
		}
	}
	
	/**
	 * 설정 가능한 환경변수 목록 문자열 반환
	 * 
	 * @return 설정 가능한 환경변수 목록 문자열
	 */
	public String showConfigurableEnv() {
		
		// 설정 가능한 환경변수 목록 문자열 생성 변수
		StringBuilder builder = new StringBuilder("");
		
		// 환경 변수 별로 문자열 생성
		Config[] configs = Config.values();
		
		for(Config config: configs) {
			
			// 환경 변수로 설정 불가인 변수인 경우 스킵함
			// 환경 변수명이 없는 경우 설정 불가임
			if(config.key == null) {
				continue;
			}
			
			// 환경 변수의 문자열 추가
			builder.append(config).append("\n");
		}
		
		return builder.toString();
	}
	
	//---------------------------
	
	/**
	 * 환경 변수 키<br>
	 * - null 일 경우, 환경 변수에서 가져오지 않음, init에서 강제 설정하는 경우
	 */
	protected String key;
	
	/** 환경 변수 값 */
	@Getter
	protected String value;
	
	/** 환경 변수 디폴트 값 */
	@Getter
	protected String defaultValue;
	
	/** 환경 변수 설명 */
	@Getter
	protected String desc;
	
	/** 필수 여부 */
	@Getter
	protected boolean isMandatory;

	/**
	 * 생성자
	 * 
	 * @param key 환경 변수 키
	 * @param value 환경 변수 디폴트 값
	 * @param desc 환경 변수 설명
	 * @param isMandatory 필수 여부
	 */
	private Config(String key, String defaultValue, String desc, boolean isMandatory) {
		
		this.key = key;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.desc = desc;
		this.isMandatory = isMandatory;
	}

	/**
	 * 생성자
	 * 
	 * @param key 환경 변수 키
	 * @param value 환경 변수 디폴트 값
	 * @param desc 환경 변수 설명
	 */
	private Config(String key, String defaultValue, String desc) {
		this(key, defaultValue, desc, false);
	}
	
	@Override
	public String toString() {
		return this.key + "(" + this.defaultValue + ", " + this.isMandatory + "):" + this.desc;
	}
}
