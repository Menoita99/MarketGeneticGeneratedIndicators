package pt.fcul.masters.analyses;

import java.time.LocalDateTime;
import java.util.List;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

public class DataAnalyses {

	private static final LocalDateTime FROM = LocalDateTime.of(2005, 1, 1, 0, 0);
	private static final TimeFrame TIMEFRAME = TimeFrame.D;
	private static final Market MARKET = Market.EUR_USD;
	
	
	public static void main(String[] args) {
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(MARKET, TIMEFRAME, FROM);

		Serie<LocalDateTime,Double> data = new Serie<>();
		for (Candlestick candlestick : candles) 
			data.add(candlestick.getDatetime(), candlestick.getClose());
		
		Plotter.builder().lineChart(data,"Eur/Usd").build().plot();
	}
}
