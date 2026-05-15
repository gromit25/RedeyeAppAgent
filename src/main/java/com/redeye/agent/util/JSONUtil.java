package com.redeye.agent.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
				return '"' + replaceSpecialChar(obj.toString()) + '"';
			}
		}
	}

	/**
	 * 문자열의 특수문자 변환
	 * 
	 * @param str 문자열
	 * @return 변환된 문자열
	 */
	private static String replaceSpecialChar(String str) {

		// 문자열이 비어 있는 경우 반환
		if(StringUtil.isEmpty(str) == true) {
			return str;
		}

		// 변환된 문자열 버퍼 변수
		StringBuilder buffer = new StringBuilder();

		// 문자열의 각 문자를 확인하여 변환 수행
		for(int index = 0; index < str.length(); index++) {

			char ch = str.charAt(index);

			switch(ch) {
				case '\n':
					buffer.append("\\\\n");
					break;
				case '\r':
					buffer.append("\\\\r");
					break;
				case '\t':
					buffer.append("\\\\t");
					break;
				case '"':
					buffer.append("\\\\\"");
					break;
				default:
					buffer.append(ch);
			}
		}

		// 변환된 문자열 반환
		return buffer.toString();
	}

	public static Object parse(String jsonMsg) throws Exception {
		
		if(jsonMsg == null) {
			return null;
		}
		
		//
		try(PushbackReader reader = new PushbackReader(new StringReader(jsonMsg))) {
			
			int read = -1;
			while((read = reader.read()) != -1) {
				
				char ch = (char)read;
				
				if(ch == '{') {
					
					reader.unread(ch);
					return parseMap(reader);
					
				} else if(ch == '[') {
					
					reader.unread(ch);
					return parseList(reader);
					
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
			}
			
			// 아무 내용이 없는 경우
			return null;
		}
	}

	private enum MapParserStatus {
		START,
		ITEM_START,
		NAME,
		SEPARATOR,
		VALUE_END
	}

	private static Map<String, Object> parseMap(PushbackReader reader) throws Exception {
		
		//
		Map<String, Object> jsonMap = new HashMap<>();
		
		//
		StringBuilder name = new StringBuilder();
		
		MapParserStatus status = MapParserStatus.START;

		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			
			case START:
				
				if(ch == '{') {
					status = MapParserStatus.ITEM_START;
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
				
			case ITEM_START:
				
				if(ch == '"') {
					status = MapParserStatus.NAME;
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
				
			case NAME:
				
				if(ch == '"') {
					status = MapParserStatus.SEPARATOR;
				} else {
					name.append(ch);
				}
				
				break;
				
			case SEPARATOR:
				
				if(ch == ':') {
					
					// map에 아이템 추가
					jsonMap.put(name.toString(), parseValue(reader));
					
					// name 클리어
					name.delete(0, name.length());
					
					status = MapParserStatus.VALUE_END;
					
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
				
			case VALUE_END:
				
				if(ch == '}') {
					return jsonMap;
				} else if(ch == ',') {
					status = MapParserStatus.ITEM_START;
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("");
	}
	
	private enum ListParserStatus {
		START,
		ITEM_END
	}
	
	private static List<Object> parseList(PushbackReader reader) throws Exception {
		
		//
		List<Object> jsonList = new ArrayList<>();
		
		ListParserStatus status = ListParserStatus.START;
		
		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			case START:
				
				if(ch == '[') {
					
					jsonList.add(parseValue(reader));
					status = ListParserStatus.ITEM_END;
					
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
				
			case ITEM_END:
				
				if(ch == ']') {
					return jsonList;
				} else if(ch == ',') {
					jsonList.add(parseValue(reader));
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("");
	}
	
	private enum ValueParserStatus {
		START,
		DECIMAL,
		FRACTIONAL,
		STRING
	}
	
	private static Object parseValue(PushbackReader reader) throws Exception {
		
		StringBuilder value = new StringBuilder();
		
		ValueParserStatus status = ValueParserStatus.START;

		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			
			case START:
				
				if(ch == '"') {
					status = ValueParserStatus.STRING;
				} else if(ch >= '0' && ch <= '9') {
					value.append(ch);
					status = ValueParserStatus.DECIMAL;
				} else if(isSpace(ch) == false) {
					throw new RuntimeException("");
				}
				
				break;
				
			case DECIMAL:
				
				if(ch >= '0' && ch <= '9') {
					value.append(ch);
				} else if(ch == '.') {
					value.append(ch);
					status = ValueParserStatus.FRACTIONAL;
				} else {
					reader.unread(ch);
					return Long.parseLong(value.toString());
				}
				
				break;
				
			case FRACTIONAL:
				
				if(ch >= '0' && ch <= '9') {
					value.append(ch);
				} else {
					reader.unread(ch);
					return Double.parseDouble(value.toString());
				}
				
				break;
				
			case STRING:
				
				if(ch == '"') {
					return value.toString();
				} else {
					value.append(ch);
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("");
	}
	
	private static boolean isSpace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
	}
}
