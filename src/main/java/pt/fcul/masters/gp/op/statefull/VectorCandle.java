package pt.fcul.masters.gp.op.statefull;

import pt.fcul.masters.utils.ShiftList;

public class VectorCandle{


	private ShiftList<Double> volumes = new ShiftList<>(10);
	private ShiftList<Double> values = new ShiftList<>(10);
	
	private static final int NORMAL = 0;
	private static final int MIDLE_VOLUME_VC = 1;
	private static final int HIGH_VOLUME_VC = 2;
	
	
	// The below math matches MT4 PVSRA indicator source
	// average volume from last 10 candles
//	av = sum(volume,10)/10
//  climax volume on the previous candle
//	value2 = volume*(high-low)
//  highest climax volume of the last 10 candles
//	hivalue2 = highest(value2,10) 
//  VA value determines the bar color. va = 0: normal. va = 2: climax.  va = 1: rising
//	va = iff(volume >= (av * 2) or value2 >= hivalue2, 2, iff(volume >= av * 1.5, 1, 0))

	
	public int getVectorCandle(double high, double low, double volume) {
		int output = NORMAL; 
		double value = volume*(high-low);
		
		if(volumes.isFull() && values.isFull()) {
			double average = volumes.stream().mapToDouble(d->d).average().getAsDouble();
			double higestValue = values.stream().mapToDouble(d->d).max().getAsDouble();
			
			if(volume >= (average*2) || value >= higestValue)
				output =  HIGH_VOLUME_VC;
			else if(volume >= average * 1.5)
				output =  MIDLE_VOLUME_VC;
		}

		volumes.add(volume);
		values.add(value);
		
		return output;
	}
}
