package com.redeye.agent.util;


import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redeye.agent.util.stat.Parameter;

/**
 * JSON 유틸리티 클래스
 * 
 * @author jmsohn
 */
public class JSONUtil {
	
	/**
	 * Map 객체를 JSON 문자열로 변환하여 반환
	 * 
	 * @param map 대상 Map 객체
	 * @return JSON 문자열
	 */
	public static String toJSON(Map<?, ?> map) {
		
		// 입력 값 검증
		if(map == null) {
			return "{}";
		}
		
		// 생성할 json 객체
		StringBuffer json = new StringBuffer("{");
		
		// 첫 번째 항목 여부
		// 중간에 ","를 넣기 위함
		boolean isFirst = true;
		
		for(Object key: map.keySet()) {
			
			// 중간에 "," 추가
			if(isFirst == false) {
				json.append(", ");
			}
			
			// Map 항목에 대해 JSON 문자열로 변환하여 추가
			json
				.append('"')
				.append(key.toString())
				.append("\": ")
				.append(getJSONValue(map.get(key)));
			
			isFirst = false;
		}
		
		json.append("}");
		
		return json.toString();
	}
	
	/**
	 * List 객체를 JSON 문자열로 변환하여 반환
	 * 
	 * @param list 대상 Map 객체
	 * @return JSON 문자열
	 */
	public static String toJSON(List<?> list) {
		
		// 입력 값 검증
		if(list == null) {
			return "[]";
		}
		
		// 생성할 json 객체
		StringBuffer json = new StringBuffer("[");
		
		// 첫 번째 항목 여부
		// 중간에 ","를 넣기 위함
		boolean isFirst = true;
		
		for(Object value: list) {
			
			// 중간에 "," 추가
			if(isFirst == false) {
				json.append(", ");
			}
			
			// List 항목에 대해 JSON 문자열로 변환하여 추가
			json.append(getJSONValue(value));
			
			isFirst = false;
		}
		
		json.append("]");
		
		return json.toString();
	}
	
	/**
	 * Set 객체를 JSON 문자열로 변환하여 반환
	 * 
	 * @param set 대상 Set 객체
	 * @return JSON 문자열
	 */
	public static String toJSON(Set<?> set) {
		
		// 입력 값 검증
		if(set == null) {
			return "[]";
		}
		
		// Set을 List 형태로 변환하여 JSON 문자열을 만들어 반환
		return toJSON(List.of(set.toArray()));
	}
	
	/**
	 * 항목에 대해 JSON 문자열로 변환하여 추가
	 * 
	 * @param obj 대상 항목
	 * @return JSON 문자열
	 */
	@SuppressWarnings("rawtypes")
	private static String getJSONValue(Object obj) {
		
		if(obj == null) {
			return "null";
		}
		
		Class<?> type = obj.getClass();
		
		if(Map.class.isAssignableFrom(type) == true) {
			
			return toJSON((Map)obj);
			
		} else if(List.class.isAssignableFrom(type) == true) {
			
			return toJSON((List)obj);
			
		} else if(Set.class.isAssignableFrom(type) == true) {
			
			return toJSON((Set)obj);
			
		} else {
			
			if(TypeUtil.isPrimitive(type) == true) {
				
				String value = obj.toString();
				
				if(value.equals("NaN") == true) {	// Not a Number 처리
					return "null";
				} else {
					return value;
				}
				
			} else {
				return '"' + obj.toString() + '"';
			}
		}
	}
	
	/**
	 * 모수 통계량(Parameter) 에 대해 JSON 문자열로 반환
	 * 
	 * @param stat 모수 통계량
	 * @param startTime 통계량 수집 시작 시간
	 * @param endTime 통계량 수집 종료 시간
	 * @return JSON 문자열
	 */
	public static String toJSON(Parameter stat, long startTime, long endTime) {
		
		// 통계량이 없을 경우 디폴트 값으로 생성
		if(stat == null) {
			stat = new Parameter();
		}
		
		// JSON 문자열 생성
		StringBuilder json = new StringBuilder("{");
		
		json.append("\"startTime\":").append(startTime);
		json.append(",\"endTime\":").append(endTime);
		json.append(",\"count\":").append(stat.getCount());
		json.append(",\"sumX\":").append(stat.getSum());
		json.append(",\"sumX2\":").append(stat.getSquaredSum());
		json.append(",\"sumX3\":").append(stat.getCubedSum());
		json.append(",\"sumX4\":").append(stat.getFourthPoweredSum());
		json.append(",\"minX\":").append(stat.getMin());
		json.append(",\"maxX\":").append(stat.getMax());
		
		return json.append("}").toString();
	}
}
