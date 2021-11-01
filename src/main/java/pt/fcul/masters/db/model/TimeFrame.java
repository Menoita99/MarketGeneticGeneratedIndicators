package pt.fcul.masters.db.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;


public enum TimeFrame {

	S5(5),
	S10(10),
	S15(15),
	S30(30),
	M1(60),
	M2(2*60),
	M4(4*60),
	M5(5*60),
	M10(10*60),
	M15(15*60),
	M30(30*60),
	H1(60*60),
	H2(2*60*60),
	H3(3*60*60),
	H4(4*60*60),
	H6(6*60*60),
	H8(8*60*60),
	H12(12*60*60),
	D(24*60*60),
	W(7*24*60*60);
	

	private long seconds;

	TimeFrame(long seconds) {
		this.seconds = seconds;
	}
	
	/**
	 * Convert seconds to other unit
	 * e.g
	 * Timeframe.D.getIn(TimeUnit.HOUR) 
	 * would return 24
	 */
	public long getIn(TimeUnit unit) {
		return unit.convert(seconds,TimeUnit.SECONDS);
	}

	public long getSeconds() {
		return seconds;
	} 

	public LocalDateTime convert(LocalDateTime datetime) {
		return datetime.minusSeconds(datetime.toEpochSecond(ZoneOffset.UTC) % seconds).withNano(0);
	}

	/**
	 * This method return a converted date time in the past or in the future given a 
	 * start point and the number of time frame dates we want to skip
	 * 
	 * A positive i will give a date time in the past
	 * A negative i will give a date time in the future
	 * 
	 * 
	 */
	public LocalDateTime getSkipedConvertedDatetime(LocalDateTime datetime, int skip) {
		return datetime.minusSeconds((datetime.toEpochSecond(ZoneOffset.UTC) % seconds) + skip*seconds).withNano(0);
	}
	
	
	public static void main(String[] args) {
		LocalDateTime datetime = LocalDateTime.now();
		System.out.println("S5  | " + S5.convert(datetime ));
		System.out.println("S10 | " + S10.convert(datetime ));
		System.out.println("S15 | " + S15.convert(datetime ));
		System.out.println("S30 | " + S30.convert(datetime ));
		System.out.println("M1  | " + M1.convert(datetime ));
		System.out.println("M2  | " + M2.convert(datetime ));
		System.out.println("M10 | " + M10.convert(datetime ));
		System.out.println("M15 | " + M15.convert(datetime ));
		System.out.println("M30 | " + M30.convert(datetime ));
		System.out.println("M4  | " + M4.convert(datetime ));
		System.out.println("M5  | " + M5.convert(datetime ));
		System.out.println("H1  | " + H1.convert(datetime ));
		System.out.println("H2  | " + H2.convert(datetime ));
		System.out.println("H3  | " + H3.convert(datetime ));
		System.out.println("H4  | " + H4.convert(datetime ));
		System.out.println("H6  | " + H6.convert(datetime ));
		System.out.println("H8  | " + H8.convert(datetime ));
		System.out.println("H12 | " + H12.convert(datetime ));
		System.out.println("D   | " + D.convert(datetime ));
		System.out.println("W   | " + W.convert(datetime ));
		System.out.println("=================================");
		System.out.println("S5  | " + S5.getSkipedConvertedDatetime(datetime,1));
		System.out.println("S10 | " + S10.getSkipedConvertedDatetime(datetime,1));
		System.out.println("S15 | " + S15.getSkipedConvertedDatetime(datetime,1));
		System.out.println("S30 | " + S30.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M1  | " + M1.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M2  | " + M2.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M10 | " + M10.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M15 | " + M15.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M30 | " + M30.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M4  | " + M4.getSkipedConvertedDatetime(datetime,1));
		System.out.println("M5  | " + M5.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H1  | " + H1.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H2  | " + H2.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H3  | " + H3.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H4  | " + H4.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H6  | " + H6.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H8  | " + H8.getSkipedConvertedDatetime(datetime,1));
		System.out.println("H12 | " + H12.getSkipedConvertedDatetime(datetime,1));
		System.out.println("D   | " + D.getSkipedConvertedDatetime(datetime,1));
		System.out.println("W   | " + W.getSkipedConvertedDatetime(datetime,1));
	}
}
