package com.redeye.appagent.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 크론잡 수행 클래스
 * 
 * @author jmsohn
 */
@Slf4j
public class CronJob {
	
	
	/** 다음 기간동안 대기를 위한 객체 */
	private Object waitObj = new Object();
	
	/** 크론 시간 표현식 */
	private CronExp cronExp;
	
	/** 수행할 잡 */
	private Object job;
	
	/** 크론 쓰레드 객체 */
	private Thread cronThread;
	
	/** 잡 스레드 수 */
	private int threadCount = -1;

	/** 잡 스레드 객체 */
	private ExecutorService jobService;
	
	/** 타임아웃(ms) */
	private long timeout = -1;
	
	/** 타임아웃 검사 큐 */
	private BlockingQueue<Timer> timerQueue = new LinkedBlockingQueue<>();
	
	/** 타임아웃 처리 스레드 객체 */
	private Thread timerThread;
	
	
	/**
	 * 생성자
	 * 
	 * @param cronExp 크론 시간 표현식
	 * @param job 수행할 잡
	 * @param timeout 잡 타임아웃, 만일 1000ms(1초) 보다 작을 경우 타임아웃은 동작하지 않음, 타임아웃 발생시 잡 종료
	 * @param threadCount 잡 스레드 수, 만일 0 보다 같거나 작을 경우 cachedThreadPool 을 사용 
	 */
	@Builder
	public CronJob(String cronExp, Runnable job, long timeout, int threadCount) throws Exception {
		
		// 크론 스케줄 설정
		this.setCronExp(cronExp);
		
		// 실행할 크론 잡 설정
		this.setJob(job);
		
		// 스레드 수 설정
		this.threadCount = threadCount;
		
		// 타임아웃 설정
		this.timeout = timeout;
	}
	
	/**
	 * 생성자
	 * 
	 * @param cronExp 크론 시간 표현식
	 * @param job 수행할 잡
	 * @param timeout 잡 타임아웃, 만일 1000ms(1초) 보다 작을 경우 타임아웃은 동작하지 않음, 타임아웃 발생시 잡 종료
	 * @param threadCount 잡 스레드 수, 만일 0 보다 같거나 작을 경우 cachedThreadPool 을 사용 
	 */
	@Builder
	public CronJob(String cronExp, Job job, long timeout, int threadCount) throws Exception {
		
		// 크론 스케줄 설정
		this.setCronExp(cronExp);
		
		// 실행할 크론 잡 설정
		this.setJob(job);
		
		// 스레드 수 설정
		this.threadCount = threadCount;
		
		// 타임아웃 설정
		this.timeout = timeout;
	}
	
	/**
	 * 생성자
	 * 
	 * @param cronExp 크론 시간 표현식
	 * @param job 수행할 잡
	 */
	public CronJob(String cronExp, Runnable job) throws Exception {
		this(cronExp, job, -1, -1);
	}
	
	/**
	 * 생성자
	 * 
	 * @param cronExp 크론 시간 표현식
	 * @param job 수행할 잡
	 */
	public CronJob(String cronExp, Job job) throws Exception {
		this(cronExp, job, -1, -1);
	}
	
	/**
	 * cron job 수행
	 */
	public void start() {
		
		// 스레드 수에 따라 ExecutorService 생성
		if(this.threadCount <= 0) {
			this.jobService = Executors.newCachedThreadPool(); 
		} else {
			this.jobService = Executors.newFixedThreadPool(this.threadCount);
		}
		
		// 잡 스케줄 스레드 수행 -----------------------
		this.cronThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// 시작 기준 시간
				long baseTime = System.currentTimeMillis(); 
				
				// 다음 수행 시간 획득 - 초기 시간
				long nextTime = cronExp.getNextTimeInMillis();
				
				// 인터럽트 미발생시 다음 잡 수행
				while(Thread.currentThread().isInterrupted() == false) {

					try {
						
						// 다음 수행 시간까지 대기
						synchronized(waitObj) {
							
							long delay = nextTime - System.currentTimeMillis();
							
							if(delay > 0) {
								waitObj.wait(delay);
							}
						}
						
						// 현재 수행 시간에 다음 수행 시간 설정
						baseTime = nextTime;

						// 다음 수행 시간 획득
						nextTime = cronExp.getNextTimeInMillis();
						
						// 잡 수행
						Future<?> jobResult = null;
						
						if(job instanceof Runnable) {
							
							jobResult = jobService.submit((Runnable)job);
							
						} else if(job instanceof Job) {
							
							final long baseTimeParam = baseTime;
							final long nextTimeParam = nextTime;
							
							jobResult = jobService.submit(() -> {
								((Job)job).run(baseTimeParam, nextTimeParam);
							});
						}
						
						// 타임아웃 시간을 계산하여 추가
						if(timeout > 0 && jobResult != null) {
							
							Timer timer = new Timer();
							
							timer.deadline = baseTime + timeout;
							timer.job = jobResult;
							
							timerQueue.add(timer);
						}

					} catch(InterruptedException iex) {

						// 현재 스레드에 인터럽트 정보 설정
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		
		// 크론잡 수행
		this.cronThread.setDaemon(true);
		this.cronThread.start();
		
		// -------------------------------
		
		// 타임아웃 크론잡 생성 및 수행 ------------
		
		// 타임아웃 시간이 음수일 경우 타임아웃 없는 것으로 간주함
		if(this.timeout <= 0) {
			return;
		}
		
		// 타임아웃 스레드 생성
		this.timerThread = new Thread(() -> {
			
			while(Thread.currentThread().isInterrupted() == false) {
			
				try {
					
					// 타임아웃 객체 반환
					Timer timer = timerQueue.poll();
					
					// 대기 시간 계산 및 대기
					long delay = timer.deadline - System.currentTimeMillis();
					if(delay > 0) {
						Thread.sleep(delay);
					}
					
					// 정상 종료가 되지 않았으면 종료 시도
					if(timer.job.isDone() == false) {
						timer.job.cancel(true);
					}
					
				} catch(InterruptedException iex) {
					Thread.currentThread().interrupt();
				}
			} // End of while
		});
		
		// 타임아웃 스레드 수행
		this.timerThread.setDaemon(true);
		this.timerThread.start();
		
		// -------------------------------
	}

	/**
	 * 현재 작업 중지, 예약도 중지됨 
	 */
	public void stop() {
		
		// 잡 스케줄 스레드 중단
		if(this.cronThread != null) {
			this.cronThread.interrupt();
		}
		
		// 실행 중 인 잡 중단
		if(this.jobService != null) {
			
			// 신규 작업 제출 거부
			this.jobService.shutdown();

			try {

				// 기존 작업들이 모두 끝날 때까지 최대 5초 기다림
				if(this.jobService.awaitTermination(5, TimeUnit.SECONDS) == false) {

					// 실행 중 작업 인터럽트
					this.jobService.shutdownNow(); 
					if(this.jobService.awaitTermination(5, TimeUnit.SECONDS) == true) {
						log.error("fail to shutdown cron job: " + this.toString());
					}
				}

			} catch(InterruptedException ie) {
				
				this.jobService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		// 타임아웃 스케줄 스레드 중단
		if(this.timerThread != null) {
			this.timerThread.interrupt();
		}
	}
	
	/**
	 * 크론 시간 표현식 설정
	 * 
	 * @param cronExp 크론 시간 표현식 문자열
	 */
	public void setCronExp(String cronExp) throws Exception {
		this.cronExp = CronExp.create(cronExp);
	}
	
	/**
	 * 설정된 크론 시간 표현식 반환
	 * 
	 * @return 설정된 시간 표현식
	 */
	public String getCronExp() {
		return this.cronExp.getCronExp();
	}
	
	/**
	 * 수행할 잡 설정
	 * 
	 * @param job 수행할 잡
	 */
	public void setJob(Object job) {
		
		if(job == null) {
			throw new IllegalArgumentException("job is null");
		}
		
		if(
			job instanceof Runnable == false
			&& job instanceof Job == false 
		) {
			throw new IllegalArgumentException("job type must be Runnable or CronJob.Job.");
		}
		
		this.job = job;
	}
	
	/**
	 * CronJob 객체의 정보 문자열 반환
	 * 
	 * @return CronJob 객체의 정보 문자열
	 */
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		
		builder
			.append(this.getCronExp())
			.append(" ")
			.append(this.job.getClass().getCanonicalName());
		
		return builder.toString();
	}
	
	/**
	 * 크론 표현식 클래스<br>
	 * Linux의 크론잡 표현과 동일함</br>
	 * 단, 초는 옵션 사항임
	 * <pre>
	 * ex) * * * * * *
	 *     초 분 시 일 월 요일
	 *     15 * * * * *
	 *     매 분 15초 마다 실행 
	 * </pre>
	 * 
	 * @author jmsohn
	 */
	public static class CronExp {
		
		/**
		 * 크론 표현식 설정 가능한 시간의 종류 코드
		 * 
		 * @author jmsohn
		 */
		private enum CronTimeUnit {
			
			SECOND(0, 59),
			MIN(0, 59),
			HOUR(0, 23),
			DAY(1, 31),
			MONTH(1, 12),
			WEEK(0, 7);		// 0, 7은 일요일, 1부터 월요일, 6은 토요일
			
			/** 최소값 */
			@Getter
			private int lower;
			/** 최대값 */
			@Getter
			private int upper;
			
			/**
			 * 생성자
			 * 
			 * @param lower 최소값
			 * @param upper 최대값
			 */
			CronTimeUnit(int lower, int upper) {
				this.lower = lower;
				this.upper = upper;
			}
		}
		
		/**
		 * 크론 표현식에서 시간 단위별(초/분/시/일/월/요일) 표현 방법 코드
		 * 
		 * @author jmsohn
		 */
		private enum TimeExpType {
			
			FIXED_LIST("[0-9]+(\\,[0-9]+)*") { // 고정된 시간 형태 : 0,15,30,45
				
				@Override
				protected int[] makeTimeListByExpType(String exp, CronTimeUnit unit) throws Exception {
					
					// 문자열 내에 시간(숫자)을 찾아 목록에 추가 
					Pattern fixedTimeP = Pattern.compile("[0-9]+");
					Matcher fixedTimeM = fixedTimeP.matcher(exp);
					
					ArrayList<Integer> timeList = new ArrayList<>();
					while(fixedTimeM.find() == true) {
						
						int time = Integer.parseInt(fixedTimeM.group());
						if(time > unit.getUpper() || time < unit.getLower()) {
							throw new Exception("invalid value:" + time);
						}
						
						timeList.add(time);
					}
					
					return TypeUtil.toIntArray(timeList);
				}
				
			},
			
			RANGE("[0-9]+\\-[0-9]+") {		// 시간 범위 형태 : 0-30
				
				@Override
				protected int[] makeTimeListByExpType(String exp, CronTimeUnit unit) throws Exception {
					
					// 범위의 최대값, 최소값을 추출
					Pattern rangeP = Pattern.compile("(?<lower>[0-9]+)\\-(?<upper>[0-9]+)");
					Matcher rangeM = rangeP.matcher(exp);
					
					if(rangeM.matches() == false) {
						throw new Exception("invalid cron expression:" + exp);
					}
					
					int lower = Integer.parseInt(rangeM.group("lower"));
					int upper = Integer.parseInt(rangeM.group("upper"));
					
					// 최소값이 최대값 보다 크면 예외 발생
					if(lower >= upper) {
						throw new Exception("invalid range(lower, upper): (" + lower + ", " + upper + ")");
					}
					
					// 최소값과 최대값이 시간단위 보다 크면 예외 발생
					if(lower > unit.getUpper() || upper > unit.getUpper()) {
						throw new Exception("invalid range(lower, upper): (" + lower + ", " + upper + ")");
					}
					
					// 최소값과 최대값 사이의 모든 값을 시간 목록에 추가함
					ArrayList<Integer> timeList = new ArrayList<>();
					for(int index = lower; index <= upper; index++) {
						timeList.add(index);
					}
					
					return TypeUtil.toIntArray(timeList);
				}
			},
			
			REPEAT("\\*(\\/[0-9]+)?") {		// 반복 형태 : */10
				
				@Override
				protected int[] makeTimeListByExpType(String exp, CronTimeUnit unit) throws Exception {
					
					// 반복 주기(divider)를 문자열 내에서 찾음
					Pattern repeatP = Pattern.compile("\\*(\\/(?<divider>[0-9]+))?");
					Matcher repeatM = repeatP.matcher(exp);
					
					if(repeatM.matches() == false) {
						throw new Exception("invalid cron expression:" + exp);
					}
					
					// 반복 주기 변수
					// 설정 되어 있지 않은 경우 1로 설정
					int divider = 1;
					
					String dividerStr = repeatM.group("divider");
					if(dividerStr != null) {	
						divider = Integer.parseInt(dividerStr);
					}
					
					// 반복 주기에 해당되는 값을 시간목록에 추가함
					// 반복 주기로 나누어서 나머지가 0이 되면 대상
					ArrayList<Integer> timeList = new ArrayList<>();
					for(int index = unit.getLower(); index <= unit.getUpper(); index++) {
						if(index % divider == 0) {
							timeList.add(index);
						}
					}
					
					return TypeUtil.toIntArray(timeList);
				}
				
			};
			
			/** 시간 단위별 표현에 대한 정규표현식 패턴 객체 */
			private Pattern timeP;
			
			/**
			 * 주어진 표현식을 사용하여 시간 목록을 만듦<br>
			 * ex) FIXED_LIST 타입에 0,10,20이 입력되면<br>
			 *     int[]{0, 10, 20} 만들어 반환함
			 * 
			 * @param exp 시간별 크론 표현식
			 * @param unit 시간 종류(분/시/일/월/요일)
			 * @return 시간 목록 배열
			 */
			protected abstract int[] makeTimeListByExpType(String exp, CronTimeUnit unit) throws Exception;
			
			/**
			 * 생성자
			 * 
			 * @param timePStr
			 */
			TimeExpType(String timePStr) {
				this.timeP = Pattern.compile(timePStr);
			}
			
			/**
			 * 크론 시간 표현 방식별로 
			 * 
			 * @param exp
			 * @param unit
			 * @return
			 */
			static int[] makeTimeList(String exp, CronTimeUnit unit) throws Exception {
				
				// 적합한 시간 단위별 표현 종류를 찾음
				TimeExpType type = null;
				for(TimeExpType candidateType: TimeExpType.values()) {
					if(candidateType.match(exp) == true) {
						type = candidateType;
						break;
					}
				}
				
				// 없을 경우, 예외 발생
				if(type == null) {
					throw new Exception("invalid cron expression:" + exp);
				}
				
				// 시간 단위별 표현에서 시간 목록을 반환
				return type.makeTimeListByExpType(exp, unit);
			}
			
			/**
			 * 주어진 시간 표현이 현재 표현방법 코드와 일치하는지 여부 반환<br>
			 * 일치하면 true, 일치하지 않으면 false
			 * 
			 * @param timeExp 검사할 시간 표현
			 * @return 일치 여부 반환
			 */
			private boolean match(String timeExp) throws Exception {
				return this.timeP.matcher(timeExp).matches();
			}
		}
		
		/** 크론 표현의 정규 표현식 문자열 */
		private static String cronExpPStr;
		
		static {
			
			// 클래스 로딩시 크론 표현의 정규 표현식 문자열을 만듦
			String timePStr = "[0-9]+(\\,[0-9]+)*|[0-9]+\\-[0-9]+|\\*(\\/[0-9]+)?";
			CronExp.cronExpPStr = 
					"^((?<second>" + timePStr + ") )?"
					+ "(?<min>" + timePStr + ") "
					+ "(?<hour>" + timePStr + ") "
					+ "(?<day>" + timePStr + ") "
					+ "(?<month>" + timePStr + ") "
					+ "(?<dayOfWeek>" + timePStr + ")$";
		}
		
		/** 크론 시간 표현 원본 */
		@Getter
		private String cronExp;
		/** 초 목록 */
		private int[] seconds;
		/** 분 목록 */
		private int[] mins;
		/** 시간 목록 */
		private int[] hours;
		/** 날짜 목록 */
		private int[] days;
		/** 월 목록 */
		private int[] months;
		/** 요일 목록 : 0-7, 0과 7은 일요일, 1은 월요일, 6은 토요일 */
		private int[] daysOfWeek;
		
		/**
		 * 크론 표현식 객체 생성 메소드
		 * 
		 * @param cronExp 크론 표현식
		 * @return 크론 표현식 객체
		 */
		public static CronExp create(String cronExp) throws Exception {
			return new CronExp(cronExp);
		}
		
		/**
		 * 생성자
		 * 
		 * @param cronExp 크론 표현식
		 */
		private CronExp(String cronExp) throws Exception {
			
			this.cronExp = cronExp;
			
			Pattern cronExpP = Pattern.compile(CronExp.cronExpPStr);
			Matcher cronExpM = cronExpP.matcher(cronExp);
			
			if(cronExpM.matches() == false) {
				throw new Exception("invalid cron expression: " + cronExp);
			}

			// 크론 표현식에 설정된 유효 시간을 설정함
			String secondStr = cronExpM.group("second");
			if(secondStr == null) {
				secondStr = "0";
			}
			
			this.seconds = TimeExpType.makeTimeList(secondStr, CronTimeUnit.SECOND);
			this.mins = TimeExpType.makeTimeList(cronExpM.group("min"), CronTimeUnit.MIN);
			this.hours = TimeExpType.makeTimeList(cronExpM.group("hour"), CronTimeUnit.HOUR);
			this.days = TimeExpType.makeTimeList(cronExpM.group("day"), CronTimeUnit.DAY);
			this.months = TimeExpType.makeTimeList(cronExpM.group("month"), CronTimeUnit.MONTH);
			this.daysOfWeek = TimeExpType.makeTimeList(cronExpM.group("dayOfWeek"), CronTimeUnit.WEEK);
		}
		
		/**
		 * 현재 시간에서 가장 가까운 다음 실행 시간 반환
		 * 
		 * @return 가장 가까운 다음 실행 시간(단위: ms)
		 */
		public long getNextTimeInMillis() {
			
			return getNextTimeInMillis(System.currentTimeMillis());
		}
		
		/**
		 * 기준 시간(baseTime)에서 가장 가까운 다음 실행 시간 반환
		 * 
		 * @param baseTime 기준 시간(단위: ms)
		 * @return 가장 가까운 다음 실행 시간(단위: ms)
		 */
		public long getNextTimeInMillis(long baseTime) {
			
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(baseTime);
			
			return getNextTimeInMillis(cal);
		}
		
		/**
		 * 기준 시간(baseTime)에서 가장 가까운 다음 실행 시간 반환
		 * 
		 * @param baseTime 기준 시간
		 * @return 가장 가까운 다음 실행 시간(단위: ms)
		 */
		public long getNextTimeInMillis(Calendar baseTime) {
			
			// 시간 단위 별(초/분/시/일/월/년도, 요일 대신 년도) 현재 시간을 가져옴
			int second = baseTime.get(Calendar.SECOND);
			int min = baseTime.get(Calendar.MINUTE);
			int hour = baseTime.get(Calendar.HOUR_OF_DAY);
			int day = baseTime.get(Calendar.DAY_OF_MONTH);
			int month = baseTime.get(Calendar.MONTH) + 1;
			int year = baseTime.get(Calendar.YEAR);
			
			// 초의 다음 시간을 가져옴
			NextTime secNext = this.getNextTime(second, true, this.seconds);
			second = secNext.getTime();
			
			// 분의 다음 시간을 가져옴
			NextTime minNext = this.getNextTime(min, secNext.isRolled(), this.mins);
			
			// 분이 변경되면 초는 목록의 첫번째 분으로 설정
			if(min != minNext.getTime()) {
				
				min = minNext.getTime();
				second = this.seconds[0];
			}
			
			// 시의 다음 시간을 가져옴
			NextTime hourNext = this.getNextTime(hour, minNext.isRolled(), this.hours);
			
			// 시가 변경되면 분/초는 목록의 첫번째 분으로 설정
			if(hour != hourNext.getTime()) {
				
				hour = hourNext.getTime();
				min = this.mins[0];
				second = this.seconds[0];
			}
			
			// 날짜 및 월의 다음 시간을 가져옴
			NextTime dayNext = null;
			NextTime monthNext = null;
			
			// 다음 날짜가 존재하지 않는 날짜이면, 존재하는 날짜가 나올때 까지 while 문 반복
			// ex) 2023.02.30이면 다시 다음 날짜를 가져와 검사
			//
			// 또한, 다음 날짜가 설정된 요일 목록에 없는 경우에도 다음 날짜를 다시 가져오도록 함 
			boolean isHourRolled = hourNext.isRolled();
			while(true) {
				
				dayNext = this.getNextTime(day, isHourRolled, this.days);
				
				// 일이 변경되면 시,분은 목록의 첫번째 시,분으로 설정
				if(day != dayNext.getTime()) {
					
					day = dayNext.getTime();
					hour = this.hours[0];
					min = this.mins[0];
					second = this.seconds[0];
				}
				
				monthNext = this.getNextTime(month, dayNext.isRolled(), this.months);
				
				// 월이 변경되면 일,시,분은 목록의 첫번째 일,시,분으로 설정
				if(month != monthNext.getTime()) {
					
					month = monthNext.getTime();
					day = this.days[0];
					hour = this.hours[0];
					min = this.mins[0];
					second = this.seconds[0];
				}
				
				// 월이 한바퀴 돌아(roll) 월 목록의 처음으로 이동하면
				// 년도를 하나 올림
				if(monthNext.isRolled() == true) {
					
					year++;
					
					month = this.months[0];
					day = this.days[0];
					hour = this.hours[0];
					min = this.mins[0];
					second = this.seconds[0];
				}
				
				// 다음 날짜가 존재하는 날짜이고 요일 목록에 있는 날짜이면 중단
				if(DateUtil.isValidDate(year, month, day) == true && isValidDayOfWeek(year, month, day) == true) {
					break;
				} else {
					// 다음 날짜로 이동
					isHourRolled = true;
				}
			}
			
			// 다음 날짜를 설정하고 long 값으로 반환
			Calendar nextTime = new GregorianCalendar();
			
			nextTime.set(Calendar.YEAR, year);
			nextTime.set(Calendar.MONTH, month-1);
			nextTime.set(Calendar.DAY_OF_MONTH, day);
			nextTime.set(Calendar.HOUR_OF_DAY, hour);
			nextTime.set(Calendar.MINUTE, min);
			nextTime.set(Calendar.SECOND, second);
			nextTime.set(Calendar.MILLISECOND, 0);
			
			return nextTime.getTimeInMillis();
		}
		
		/**
		 * 시간 단위 별(분/시/일/월/요일) 시간 목록 중<br>
		 * 현재 시간(cur)가 가장 가까운 다음 시간 및 시간 목록의 처음으로 이동(roll)하였는지 여부 반환<br>
		 * ex)<br>
		 * 만일 timeList=[1,3,8] 이고, 현재 시간이 7 이면<br>
		 * 가까운 다음 시간은 8이고, roll 여부는 false 임<br>
		 * <br>
		 * 만일 timeList=[1,3,8] 이고, 현재 시간이 9 이면<br>
		 * 가까운 다음 시간은 1이고, roll 여부는 true 임<br>
		 * 
		 * @param cur 현재 시간 
		 * @param isRolled 이전 시간 단위에서 roll이 있었는지 여부
		 * @param timeList 시간 목록
		 * @return 가장 가까운 시간과 시간 목록의 처음으로 이동(roll)하였는지 여부
		 */
		private NextTime getNextTime(int cur, boolean isRolled, int[] timeList) {
			
			// 시간 목록을 순회하면서, 시간 목록의 시간이 현재 시간과 같거나 큰 경우 처리함
			for(int index = 0; index < timeList.length; index++) {
				
				int time = timeList[index];
				
				if(cur == time) {
					
					if(isRolled == true) {
						if(index + 1 == timeList.length) {
							
							// 시간 목록의 시간과 현재 시간이 같고,
							// 시간 목록에서 가장 가까운 시간이 시간 목록의 가장 마지막에 있고,
							// 이전 시간 단위에서 roll이 있었던 경우
							// ex)
							// timeList=[1,3,8], cur의 시간 값이 8이고, 이전 시간에서 roll 이 true 인 경우
							// 1, true를 반환함
							return new NextTime(timeList[0], true);
							
						} else {
							
							// 시간 목록의 시간과 현재 시간이 같고,
							// 시간 목록에서 가장 가까운 시간이 시간 목록의 가장 마지막이 아니고
							// 이전 시간 단위에서 roll이 있었던 경우
							// ex)
							// timeList=[1,3,8], cur의 시간 값이 3이고, 이전 시간에서 roll 이 true 인 경우
							// 8, false를 반환함
							return new NextTime(timeList[index + 1], false);
						}
					} else {
						
						// 시간 목록의 시간과 현재 시간이 일치하면서,
						// 이전 시간 단위에서 roll이 없었던 경우
						// ex)
						// timeList=[1,3,8], cur의 시간 값이 8이고, 이전 시간에서 roll 이 false 인 경우
						// 8, false를 반환함
						return new NextTime(timeList[index], false);
					}
					
				} else if(cur < time) {
					
					// 시간 목록의 시간이 현재 시간보다 큰 경우
					// ex)
					// timeList=[1,3,8], cur의 시간 값이 6일 경우,
					//   -> 이때는, 이전 시간 roll여부와 상관 없이, time 값(즉 timeList[index])을 설정함
					//   -> 만일 cur 시간 값이 9일 경우, 1,3,8 중 9보다 큰값이 없기 때문에
					//      for 문을 빠져 나가게 됨
					// 8, false를 반환함
					return new NextTime(timeList[index], false);
				}
			}
			
			// 시간 목록의 시간이 현재 시간보다 같거나 큰 경우가 없는 경우
			// ex)
			// timeList=[1,3,8], cur의 시간 값이 9일 경우,
			// 1, true를 반환함
			return new NextTime(timeList[0], true);
		}
		
		/**
		 * 다음 시간 클래스
		 * 
		 * @author jmsohn
		 */
		@Getter
		private class NextTime {
			
			/** 다음 시간 */
			private int time;
			/** roll(목록의 마지막에서 처음으로 이동) 여부 */
			private boolean rolled;
			
			/**
			 * 생성자
			 * 
			 * @param time 다음 시간
			 * @param rolled roll 여부
			 */
			NextTime(int time, boolean rolled) {
				this.time = time;
				this.rolled = rolled;
			}
		}
		
		/**
		 * 주어진 날짜가 설정된 요일 목록에 있는지 여부 반환<br>
		 * 있으면 true, 없으면 false 
		 * 
		 * @param year 년도
		 * @param month 월(1~12)
		 * @param day 날짜
		 * @return 요일 목록에 있는지 여부
		 */
		private boolean isValidDayOfWeek(int year, int month, int day) {
			
			// 주어진 날짜로 생성
			Calendar cal = new GregorianCalendar(year, month-1, day);
			
			// Calendar의 요일은 1-일요일 ~ 7-토요일임
			// cron 표현식에서는 0-일요일, 1-월요일 ~ 6-토요일, 7-일요일이기 떄문에 맞쳐주기 위해 1을 뺌
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			// 주어진 날짜의 요일이 대상 목록에 있는지 확인
			for(int index = 0; index < this.daysOfWeek.length; index++) {
				
				if(dayOfWeek == this.daysOfWeek[index]) {
					return true;
				}
				
				if(dayOfWeek == 0 && this.daysOfWeek[index] == 7) {
					return true;
				}
			}
			
			return false;
		}
		
		/**
		 * 현재 크론 표현식 객체의 정보를 문자열로 반환
		 */
		@Override
		public String toString() {
			
			StringBuilder builder = new StringBuilder();
			
			builder
				.append(this.cronExp).append("\n")
				.append(StringUtil.join(",", this.seconds)).append("\n")
				.append(StringUtil.join(",", this.mins)).append("\n")
				.append(StringUtil.join(",", this.hours)).append("\n")
				.append(StringUtil.join(",", this.days)).append("\n")
				.append(StringUtil.join(",", this.months)).append("\n")
				.append(StringUtil.join(",", this.daysOfWeek));
			
			return builder.toString();
		}
	} // End of CronExp Class
	
	/**
	 * 크론잡 인터페이스
	 * 
	 * @author jmsohn
	 */
	@FunctionalInterface
	public static interface Job {
		
		/**
		 * 크론잡 수행 메소드
		 * 
		 * @param startTime 시작 시간
		 * @param nextTime 다음 시작 시간 
		 */
		void run(long startTime, long nextTime);
	}
	
	/**
	 * 타임아웃 처리를 위한 데드라인과 잡 관련 클래스
	 * 
	 * @author jmsohn
	 */
	static class Timer {
		
		/** 데드라인 */
		long deadline;
		
		/** 실행 중인 잡 */
		Future<?> job;
	}
}
