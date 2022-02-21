package pt.fcul.masters.table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import lombok.Getter;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@Getter
public class INDArrayTable extends Table<INDArray> {
	
	private int vectorSize;

	public INDArrayTable(int vectorSize) {
		super();
		this.vectorSize = vectorSize;
		fetch();
	}

	public INDArrayTable(Market market, TimeFrame timeframe,int vectorSize) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.vectorSize = vectorSize;
		fetch();
	}
	
	
	
	public INDArrayTable(Market market, TimeFrame timeframe, LocalDateTime datetime,int vectorSize) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		this.vectorSize = vectorSize;
		fetch();
	}
	
	
	private void fetch() {
		columns = new ArrayList<>(List.of("open", "high", "low", "close", "volume"));
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(market, timeframe, datetime);
		
		for(int i = vectorSize; i < candles.size(); i++) {
			float[] open = new float[vectorSize];
			float[] high = new float[vectorSize];
			float[] low = new float[vectorSize];
			float[] close = new float[vectorSize];
			float[] volume = new float[vectorSize];
			for (int j = i - vectorSize; j < i; j++) {
				open[j - (i - vectorSize)] = (float)candles.get(j).getOpen();
				high[j - (i - vectorSize)] = (float)candles.get(j).getHigh();
				low[j - (i - vectorSize)] = (float)candles.get(j).getLow();
				close[j - (i - vectorSize)] = (float)candles.get(j).getClose();
				volume[j - (i - vectorSize)] = (float)candles.get(j).getVolume();
			}
			addRow(Nd4j.create(open),Nd4j.create(high),Nd4j.create(low),Nd4j.create(close),Nd4j.create(volume));
		}
		
		calculateSplitPoint();
		
		System.out.println("Train set is from "+candles.get(trainSet.key()).getDatetime() + " " + candles.get(trainSet.value()-1).getDatetime() + " " + (trainSet.value() - trainSet.key()));
		System.out.println("Validation set is from "+candles.get(validationSet.key()).getDatetime() + " " + candles.get(validationSet.value()-1).getDatetime() + " " + (validationSet.value() - validationSet.key()));
	}
}
