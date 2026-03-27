package com.redeye.agent.domain.jdbc.acquisitor.advice;

import com.redeye.agent.domain.common.ElapsedTimeAdvice;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class StatementAdvice extends ElapsedTimeAdvice {

	/**
	 * 
	 */
	public static class executeQuery {

		/**
		 * 쿼리 실행
		 */
		@Advice.OnMethodEnter
		public static void onEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
			// TODO
		}
	}
}
