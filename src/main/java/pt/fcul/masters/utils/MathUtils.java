package pt.fcul.masters.utils;

public class MathUtils {

	private MathUtils() {};
	
	/**
	 * @param previousValue the previous value
	 * @param currentValue the current value
	 * @return returns a value between -1 and 1 that is the percentage difference between the 2 given values.
	 */
	public static double toPercentage(double previousValue, double currentValue) {
		return currentValue / previousValue - 1D;
	}
	
	public static void main(String[] args) {
		System.out.println(toPercentage(0, -100));
	}
}
