package pt.fcul.masters.analyses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.plotter.file.Csv;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

public class VolumeAnalyses {
	
	
	private static final LocalDateTime FROM = LocalDateTime.of(2005, 1, 1, 0, 0);
	private static final TimeFrame TIMEFRAME = TimeFrame.M15;
	private static final Market MARKET = Market.EUR_USD;
	private static final int INTERVALS = 100;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);
		candles.sort((o1,o2)-> Double.compare(o1.getVolume(), o2.getVolume()));
		
		Serie<Integer,Double> serie = new Serie<>("Volume distribution");
		for (int i = 0; i < candles.size(); i++)
			serie.add(i, candles.get(i).getVolume());

		Csv.printSameXSeries(new File("C:\\Users\\Owner\\Desktop\\sortedVolume.csv"),serie);
		Plotter.builder().lineChart(serie, serie.getName()).build().plot();
	}

	public static void frequency(List<Candlestick> candles) {
		double min = CandlestickFetcher.getMinVolumeByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);
		double max = 2000D;//CandlestickFetcher.getMaxVolumeByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);
		
		double interval = Math.ceil((max - min) / (double)INTERVALS);
		int[] zones = new int[INTERVALS];
		
		for (Candlestick candlestick : candles) {
			double value = candlestick.getVolume() - min;
			int zone = Math.min(99,(int) (value / interval));
			zones[zone] += 1;
		}
		
		Serie<String,Integer> serie = new Serie<>("Volume distribution");
		for (int i = 0; i < zones.length; i++) {
			String z = "["+(int)Math.floor(min + i * interval)+","+ (int)Math.floor(min + i * interval + interval)+"[";
			serie.add(z, zones[i]);
		}
		Plotter.builder().barChart(serie, serie.getName()).build().plot();
	}

}
