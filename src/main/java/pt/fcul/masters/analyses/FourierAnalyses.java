package pt.fcul.masters.analyses;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.utils.Fourier;
import pt.fcul.masters.utils.Pair;
import pt.fcul.masters.utils.Fourier.Complex;

public class FourierAnalyses {

	private static final LocalDateTime FROM = LocalDateTime.of(2015, 1, 1, 0, 0);
	private static final TimeFrame TIMEFRAME = TimeFrame.D;
	private static final Market MARKET = Market.USD_JPY;
	
	
	public static void main(String[] args){
//		double[] points = new double[] {100,100,100,-100,-100,-100,100,100,100,-100,-100,-100};
//		double[] points = new double[3];
//		for (int i = 0; i < points.length; i++)
//			points[i] = i;
		
		double[] points = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM).stream().mapToDouble(c -> c.getClose()).toArray();
		
		List<Double> original = new LinkedList<>();
		for (double d : points) 
			original.add(d);
		Plotter.builder().lineChart(original, "original").build().plot();
		
		Complex[] dft = Fourier.dft(points);
		
//		for (Complex c : dft) {
//			plotComplex(c);
//		}
		
		plotFourier(dft);
	}
	
	
	
	
	
	public static void plotComplex(Complex c) {
		double points = 100;
		double cicles = 1;
		
		Serie<Double,Double> complex = new Serie<>();
		for (double time = 0; time < 2 * Math.PI * cicles; time += (2 * Math.PI)/points) {
			Pair<Double, Double> point = c.pointAtTime(time, Math.PI/2);
			complex.add(time, point.value());
		}
		Plotter.builder().lineChart(complex,c.toString()).build().plot();
	}





	public static void plotFourier(Complex[] dft) {
		double points = dft.length;
		double cicles = 1;
		
		List<Float> data = new ArrayList<>();
		for (double time = 0; time < 2 * Math.PI * cicles; time += (2 * Math.PI)/points) {
			double y = 0;
//			for (Complex c : dft) 
//				y += c.pointAtTime(time, Math.PI/2).value();
			for (int i = 0; i < dft.length; i++) 
				if(i%5==0)
					y += dft[i].pointAtTime(time, Math.PI/2).value();
			
			data.add((float)y);
		}
		Plotter.builder().lineChart(data, "Fourier").build().plot();
		
		
		//...
		
//		List<Double> closeValues = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM).stream().map(c -> c.getClose()).toList();
//		List<Double> diff = new LinkedList<>();
//
//		for (int i = 0; i < dft.length; i++)
//			diff.add(Math.abs(closeValues.get(i)-data.get(i)));
//		
//		Plotter.builder().lineChart(diff, "diff").build().plot();
	}
}






