package pt.fcul.masters.table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.jenetics.prog.regression.Sample;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@ToString
@Getter
@Setter
public class DoubleTable extends Table<Double> {
	

	public DoubleTable() {
		super();
		fetch();
	}

	public DoubleTable(Market market, TimeFrame timeframe) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		fetch();
	}
	
	
	
	public DoubleTable(Market market, TimeFrame timeframe, LocalDateTime datetime) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		fetch();
	}
	
	
	public DoubleTable(Market market, TimeFrame timeframe, LocalDateTime from, LocalDateTime to) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = from;
		this.to = to;
		fetch();
	}

	private void fetch() {
		columns = new ArrayList<>(List.of("open", "high", "low", "close", "volume"));
		List<Candlestick> candles = to == null ? CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(market, timeframe, datetime) : 
												 CandlestickFetcher.findAllByMarketTimeframeAfterDatetimeAndBefore(market, timeframe, datetime,to);
		candles.forEach(c -> addRow(c.getOpen(),c.getHigh(),c.getLow(),c.getClose(),c.getVolume()));
		
		calculateSplitPoint();
		
		System.out.println("Train set is from "+candles.get(trainSet.key()).getDatetime() + " " + candles.get(trainSet.value()-1).getDatetime() + " " + (trainSet.value() - trainSet.key()));
		System.out.println("Validation set is from "+candles.get(validationSet.key()).getDatetime() + " " + candles.get(validationSet.value()-1).getDatetime() + " " + (validationSet.value() - validationSet.key()));
	}
	

	public synchronized List<Sample<Double>> asDoubleTrainSamples() {
	    List<Sample<Double>> output = new ArrayList<>();
		for (int i = trainSet.key(); i < trainSet.value(); i++) {
			Double[] data = new Double[getHBuffer().get(i).size()];
			for (int j = 0; j < getHBuffer().get(i).size(); j++)
					data[j] = getHBuffer().get(i).get(j);
			output.add(Sample.of(data));
		}
		return output;
	}
	
	

	public synchronized List<Sample<Double>> asDoubleValidationSamples() {
	    List<Sample<Double>> output = new ArrayList<>();
		for (int i = validationSet.key(); i < validationSet.value(); i++) {
			Double[] data = new Double[getHBuffer().get(i).size()];
			for (int j = 0; j < getHBuffer().get(i).size(); j++)
					data[j] = getHBuffer().get(i).get(j);
			output.add(Sample.of(data));
		}
		return output;
	}
}
