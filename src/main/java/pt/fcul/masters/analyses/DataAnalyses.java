package pt.fcul.masters.analyses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.utils.ShiftList;

public class DataAnalyses {

	private static final LocalDateTime FROM = LocalDateTime.of(2012, 1, 1, 0, 0);
	private static final TimeFrame TIMEFRAME = TimeFrame.D;
	private static final Market MARKET = Market.TWTR;
	
	
	public static void main(String[] args) {
		plotData();
	}



	public static void plotData() {
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);
	//	candles = candles.subList(candles.size()-8858, candles.size());
		//Normalizer normalizer = new DynamicStandartNormalizer(25*6);
		List<Double> close = candles.stream().map(Candlestick::getClose).toList();
		//close = normalizer.apply(close);
		Serie<Integer,Double> data = new Serie<>();
		
		for (int i = 0; i < close.size(); i++) {
			data.add(i, close.get(i));
		}
		Plotter.builder().lineChart(data,MARKET.toString()).build().plot();
	}
	 
	
	
	
	
	public static void saveCandlestickToCsv() {
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);
		int featureSize = 1000;
		
		try(PrintWriter pw = new PrintWriter(new File("C:\\Users\\Owner\\Desktop\\allValues_"+TIMEFRAME+".csv"))){
			candles.forEach(e -> pw.println(e.getClose()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ShiftList<Double> data = new ShiftList<>(featureSize);
		try(PrintWriter pw = new PrintWriter(new File("C:\\Users\\Owner\\Desktop\\values_1kdata_binaryCategorization_"+TIMEFRAME+".csv"))){
			for (int i = 0; i < candles.size()-1; i++) {
				data.add(candles.get(i).getClose());
				if(data.isFull()) {
					String row = data.stream().map(d->d.toString()).collect(Collectors.joining(","));
					double diff = data.getLast() - candles.get(i+1).getClose();
					row += ","+(diff < 0 ? -1 : diff > 0 ? 1 : 0);
					pw.println(row);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
