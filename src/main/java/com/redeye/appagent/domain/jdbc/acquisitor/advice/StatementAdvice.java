package com.redeye.appagent.domain.jdbc.acquisitor.advice;

import java.lang.reflect.Method;

import com.redeye.appagent.domain.common.ElapsedTimeAdvice;
import com.redeye.appagent.domain.common.InvokeStatus;
import com.redeye.appagent.domain.jdbc.JDBCUtil;

import net.bytebuddy.asm.Advice;

/**
 * Statement 어드바이스 클래스<br>
 * 모든 속성 및 메소드는 public 이어야 함 - 스프링부트 클래스 로더 때문
 * 
 * @author jmsohn
 */
public class StatementAdvice extends ElapsedTimeAdvice {

	/**
	 * 쿼리 실행시 어드바이스 클래스
	 */
	public static class executeQuery {

		/**
		 * 쿼리 실행
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
	
			ContextHolder.setSql(args[0].toString());
			ContextHolder.setInvokeStatus(InvokeStatus.INVOKE_ENTER);
			ContextHolder.setStartTime();
		}

		/**
		 * 쿼리 실행 후 콜백
		 */
		@Advice.OnMethodExit
		public static void onExit() {
			
			// 호출 상태 확인
			// 반복 호출에 의해 여러번 호출된 것으로 계산되는 것을 방지하기 위함
			// INVOKE_ENTER -> INVOKE_EXIT 상태 변경시에만 한번 호출된 것으로 계산
			if(ContextHolder.getInvokeStatus() != InvokeStatus.INVOKE_ENTER) {
				return;
			}
			
			ContextHolder.setInvokeStatus(InvokeStatus.INVOKE_EXIT);
			
			// 쿼리 수행 시간 계산
			long elapsedTime = System.currentTimeMillis() - ContextHolder.getStartTime();
			
			// 쿼리 수행 시간을 통계 처리자에게 전송
			put(JDBCUtil.getSqlId(), elapsedTime);
		}
	}
}
