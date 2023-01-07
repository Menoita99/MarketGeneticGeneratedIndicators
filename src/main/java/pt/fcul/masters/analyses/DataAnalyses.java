package pt.fcul.masters.analyses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.plotter.file.FileWriter;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import pt.fcul.masters.data.normalizer.DynamicStandartNormalizer;
import pt.fcul.masters.data.normalizer.StandartNormalizer;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.utils.ShiftList;

public class DataAnalyses {

	private static final LocalDateTime FROM = LocalDateTime.of(2015, 1, 1, 0, 0);
	private static final LocalDateTime TO = LocalDateTime.of(2022, 04, 14, 0, 0);
	private static final TimeFrame TIMEFRAME = TimeFrame.D;
	private static final Market MARKET = Market.AAPL;


	public static void main(String[] args) throws IOException {
//		plotData();
		normalize(List.of(10D,13D,15D,17D,14D,12D,16D,15D));
	}

	public static void normalize(List<Double> vector) {
		new StandartNormalizer().apply(vector).forEach(System.out::println);
	}


	public static void plotData(){
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetimeAndBefore(MARKET, TIMEFRAME, FROM, TO);
		candles = candles.subList(0, candles.size()/2);
		int window = 25*6;
		DynamicStandartNormalizer normalizer = new DynamicStandartNormalizer(window);
		List<Double> close = candles.stream().map(Candlestick::getClose).toList();
		Serie<Integer,Double> data = new Serie<>();
		Serie<Integer,Double> dataNorm = new Serie<>();
		
		
		System.out.println("Before normalization "+close.size());
		for (int i = 0; i < close.size(); i++) {
			data.add(i, close.get(i));
			if(i<8)
				System.out.println(close.get(i));
		}
		
		Plotter.builder().lineChart(data,MARKET.toString()).build().plot();
		
		close = normalizer.apply(close);
		System.out.println("After normalization "+close.size());

		for (int i = 0; i < close.size(); i++) {
			dataNorm.add(i, close.get(i));
		}
		dataNorm.cleanIf((index,value)-> index < window);
		Plotter.builder().lineChart(dataNorm,MARKET.toString()+" windowed normalized").build().plot();
	}


	public static void writeDataForAsPythonList() throws IOException {
		Market[] markets = {
				Market.COTY,
				Market.FORD,
				Market.KO,
				Market.MSFT,
				Market.PSI20,
				Market.QCOM,
				Market.REGN,
				Market.SBUX,
				Market.SPY,
				Market.TWTR,
				Market.ETH_USD,
		};
		FileWriter fw = new FileWriter(new File("C:\\Users\\Owner\\Desktop\\txt.txt"));
		for(Market m : markets) {
			List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetimeAndBefore(m, TIMEFRAME, FROM, TO);
			//	candles = candles.subList(candles.size()-8858, candles.size());
			//		DynamicStandartNormalizer normalizer = new DynamicStandartNormalizer(25*6);
			List<Double> close = candles.stream().map(Candlestick::getClose).toList();
			//		close = normalizer.apply(close);
			//		Serie<Integer,Double> data = new Serie<>();
			//		
			//		for (int i = 0; i < close.size(); i++) {
			//			data.add(i, close.get(i));
			//		}
			fw.append("data_"+m+" = [");
			close.forEach( p -> fw.append(p+","));
			fw.append("]");
		}
		fw.close();
		//		Plotter.builder().lineChart(data,MARKET.toString()).build().plot();
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
