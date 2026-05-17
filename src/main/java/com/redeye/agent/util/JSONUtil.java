package com.redeye.agent.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	// ----------------------------------
	// JSON 파서
	// ----------------------------------
	
	/**
	 * JSON 파싱용 Reader 클래스
	 * 
	 * @author jmsohn
	 */
	private static class JSONReader extends PushbackReader {
		
		
		/** 이전 행별 열 수 */
		private Stack<AtomicInteger> rowStack;
		
		/** 현재 행의 읽은 열 수 */
		private AtomicInteger column;
		

		/**
		 * 생성자
		 * 
		 * @param in
		 */
		public JSONReader(Reader reader) {
			
			super(reader);
			
			this.rowStack = new Stack<>();
			this.column = new AtomicInteger(0);
		}
		
		@Override
		public int read() throws IOException {
			
			int read = super.read();
			
			synchronized(this) {
				
				if((char)read == '\n') {
					
					this.rowStack.push(this.column);
					this.column = new AtomicInteger(0);
					
				} else {
					
					this.column.addAndGet(1);
				}
			}
			
			return read;
		}
		
		@Override
		public void unread(int read) throws IOException {
			
			super.unread(read);
			
			synchronized(this) {
				
				if((char)read == '\n') {
					this.column = this.rowStack.pop();
				} else {
					this.column.addAndGet(-1);
				}
			}
		}
		
		/**
		 * 행 수 반환
		 * 
		 * @return 행 수
		 */
		int getRow() {
			return this.rowStack.size() + 1;
		}
		
		/**
		 * 열 수 반환
		 * 
		 * @return 열 수
		 */
		int getColumn() {
			return this.column.get();
		}
	}

	/**
	 * 파싱 오류 예외 클래스
	 * 
	 * @author jmsohn
	 */
	public static class UnexpectedCharException extends RuntimeException {

		/** 시리얼 번호 */
		private static final long serialVersionUID = -2510361935622783552L;
		
		/**
		 * 생성자
		 * 
		 * @param reader JSON 리더 객체
		 * @param ch 현재 읽은 문자
		 */
		UnexpectedCharException(JSONReader reader, char ch) {
			super(makeMsg(reader, ch));
		}
		
		/**
		 * 예외 메시지 생성 및 반환
		 * 
		 * @param reader JSON 리더 객체
		 * @param ch 현재 읽은 문자
		 * @return 예외 메시지
		 */
		private static String makeMsg(JSONReader reader, char ch) {
			
			if(reader == null) {
				return "'reader' is null.";
			}
			
			return String.format(
				"Unexpected char(%c) at (%d, %d).",
				ch,
				reader.getRow(),
				reader.getColumn()
			);
		}
	}
	
	/**
	 * 맵 형태의 JSON 파싱 후 결과 반환
	 * 
	 * @param jsonMsg 맵 형태의 JSON 메시지
	 * @return 파싱된 맵 객체 반환
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseMap(String jsonMsg) throws Exception {
		return (Map<String, Object>)parse(jsonMsg, Map.class);
	}
	
	/**
	 * 목록 형태의 JSON 파싱 후 결과 반환
	 * 
	 * @param jsonMsg 목록 형태의 JSON 메시지
	 * @return 파싱된 목록 객체 반환
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> parseList(String jsonMsg) throws Exception {
		return (List<Object>)parse(jsonMsg, List.class);
	}

	/**
	 * JSON 파싱 후 결과 반환
	 * 
	 * @param <T> JSON 파싱 결과 반환 타입
	 * @param jsonMsg 파싱할 JSON 메시지
	 * @param returnType 반환 타입 클래스 (주의, Map.class, List.class 두개만 설정 가능)
	 * @return 파싱 결과 반환
	 */
	public static <T> T parse(String jsonMsg, Class<T> returnType) throws Exception {
		
		if(returnType == null) {
			throw new RuntimeException("'returnType' is null.");
		}
			
		if(jsonMsg == null) {
			return null;
		}
		
		//
		try(JSONReader reader = new JSONReader(new StringReader(jsonMsg))) {
			
			int read = -1;
			while((read = reader.read()) != -1) {
				
				char ch = (char)read;
				
				if(ch == '{') {
										
					if(returnType == Map.class) {
						reader.unread(ch);
						return returnType.cast(parseMap(reader));
					} else {
						throw new UnexpectedCharException(reader, ch);
					}
					
				} else if(ch == '[') {
					
					if(returnType == List.class) {
						reader.unread(ch);
						return returnType.cast(parseList(reader));
					} else {
						throw new UnexpectedCharException(reader, ch);
					}
					
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
			}
			
			// 아무 내용이 없는 경우
			return null;
		}
	}

	/**
	 * JSON 맵 파싱 상태
	 */
	private enum MapParserStatus {
		START,
		ITEM_START,
		NAME,
		SEPARATOR,
		VALUE_END
	}

	/**
	 * JSON 맵 파싱
	 * 
	 * @param reader JSON 리더 객체
	 * @return 파싱된 맵 객체
	 */
	private static Map<String, Object> parseMap(JSONReader reader) throws Exception {
		
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
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
				
			case ITEM_START:
				
				if(ch == '"') {
					status = MapParserStatus.NAME;
				} else if(ch == '}') {
					return jsonMap;
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
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
					Object value = parseValue(reader);
					if(value != null) {
						jsonMap.put(name.toString(), value);
					}
					
					// name 클리어
					name.delete(0, name.length());
					
					status = MapParserStatus.VALUE_END;
					
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
				
			case VALUE_END:
				
				if(ch == '}') {
					return jsonMap;
				} else if(ch == ',') {
					status = MapParserStatus.ITEM_START;
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("unexpected end.");
	}
	
	/**
	 * JSON 목록 파싱 상태
	 */
	private enum ListParserStatus {
		START,
		ITEM_END
	}
	
	/**
	 * JSON 목록 파싱
	 * 
	 * @param reader JSON 리더 객체
	 * @return 파싱된 목록 객체
	 */
	private static List<Object> parseList(JSONReader reader) throws Exception {
		
		//
		List<Object> jsonList = new ArrayList<>();
		
		ListParserStatus status = ListParserStatus.START;
		
		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			case START:
				
				if(ch == '[') {
					
					Object value = parseValue(reader);
					if(value != null) {
						jsonList.add(value);
					}
					
					status = ListParserStatus.ITEM_END;
					
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
				
			case ITEM_END:
				
				if(ch == ']') {
					return jsonList;
				} else if(ch == ',') {
					
					Object value = parseValue(reader);
					if(value != null) {
						jsonList.add(value);
					}
					
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("unexpected end.");
	}
	
	/**
	 * JSON 값(value) 파싱<br>
	 * 문자열, 숫자 등
	 * 
	 * @param reader JSON 리더 객체
	 * @return 파싱된 값 객체
	 */
	private static Object parseValue(JSONReader reader) throws Exception {
		
		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			if(ch == '"') {
				
				reader.unread(ch);
				return parseStr(reader);
				
			} else if(ch >= '0' && ch <= '9') {
				
				reader.unread(ch);
				return parseNum(reader);
				
			} else if(ch == '{') {
				
				reader.unread(ch);
				return parseMap(reader);
				
			} else if(ch == '[') {
				
				reader.unread(ch);
				return parseList(reader);
				
			} else if(isSpace(ch) == false) {
				
				reader.unread(ch);
				return null;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("unexpected end.");
	}
	
	/**
	 * 문자열 파싱 상태
	 */
	private enum StrParserStatus {
		START,
		STR,
		SPECIAL
	}
	
	/**
	 * JSON 문자열 파싱
	 * 
	 * @param reader JSON 리더 객체
	 * @return 파싱된 문자열 객체
	 */
	private static Object parseStr(JSONReader reader) throws Exception {
		
		StringBuilder str = new StringBuilder();
		
		StrParserStatus status = StrParserStatus.START;

		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			
			case START:
				
				if(ch == '"') {
					status = StrParserStatus.STR;
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
				
			case STR:
				
				if(ch == '"') {
					return str.toString();
				} else if(ch == '\\') {
					str.append(ch);
					status = StrParserStatus.SPECIAL;
				} else {
					str.append(ch);
				}
				
				break;
				
			case SPECIAL:
				
				str.append(ch);
				status = StrParserStatus.STR;
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("unexpected end.");
	}
	
	/**
	 * 숫자 파싱 상태
	 */
	private enum NumParserStatus {
		START,
		DECIMAL,
		FRACTION
	}
	
	/**
	 * JSON 숫자 파싱<br>
	 * Long(ex. 12) 또는 Double(ex. 12.34) 형으로 반환
	 * 
	 * @param reader JSON 리더 객체
	 * @return 파싱된 숫자 객체
	 */
	private static Object parseNum(JSONReader reader) throws Exception {
		
		StringBuilder num = new StringBuilder();
		
		NumParserStatus status = NumParserStatus.START;

		int read = -1;
		while((read = reader.read()) != -1) {
			
			char ch = (char)read;
			
			switch(status) {
			
			case START:
				
				if(ch >= '0' && ch <= '9') {
					num.append(ch);
					status = NumParserStatus.DECIMAL;
				} else if(isSpace(ch) == false) {
					throw new UnexpectedCharException(reader, ch);
				}
				
				break;
				
			case DECIMAL:
				
				if(ch >= '0' && ch <= '9') {
					num.append(ch);
				} else if(ch == '.') {
					num.append(ch);
					status = NumParserStatus.FRACTION;
				} else {
					reader.unread(ch);
					return Long.parseLong(num.toString());
				}
				
				break;
				
			case FRACTION:
				
				if(ch >= '0' && ch <= '9') {
					num.append(ch);
				} else {
					reader.unread(ch);
					return Double.parseDouble(num.toString());
				}
				
				break;
			}
		}
		
		// 정상 종료 되지 않은 경우
		throw new RuntimeException("unexpected end.");
	}
	
	/**
	 * 공백 캐릭터 여부 반환
	 * 
	 * @param ch 검사할 캐릭터
	 * @return 공백 캐릭터 여부
	 */
	private static boolean isSpace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
	}
}
