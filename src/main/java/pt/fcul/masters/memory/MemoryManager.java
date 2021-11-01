package pt.fcul.masters.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.jenetics.prog.regression.Sample;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@Data
public class MemoryManager {
//TODO implements Collection<Double>{

	private List<List<Double>> hBuffer = new ArrayList<>();
	private List<String> columns = new ArrayList<>();
	
	private Pair<Integer, Integer> testSet;
	private Pair<Integer, Integer> validationSet;
	
	private double testValidationRatio = 0.7;
	
	private Market market = Market.EUR_USD;
	private TimeFrame timeframe = TimeFrame.M15;
	private LocalDateTime datetime = LocalDateTime.of(2019, 1, 1, 0, 0);
	
	public MemoryManager() {
		fetch();
	}

	
	
	
	public MemoryManager(Market market, TimeFrame timeframe) {
		this.market = market;
		this.timeframe = timeframe;
		fetch();
	}

	
	
	
	public MemoryManager(Market market, TimeFrame timeframe, LocalDateTime datetime) {
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		fetch();
		calculateSplitPoint();
	}

	
	
	
	private void calculateSplitPoint() {
		int point = (int)(hBuffer.size() * testValidationRatio);
		testSet = new Pair<Integer, Integer>(0, point);
		validationSet = new Pair<Integer, Integer>(point, hBuffer.size());
	}




	private void fetch() {
		columns = new ArrayList<>(List.of("open", "high", "low", "close", "volume"));
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(market, timeframe, datetime);
		candles.forEach(c -> addRow(c.getOpen(),c.getHigh(),c.getLow(),c.getClose(),c.getVolume()));
	}

	
	
	
	public synchronized int addRow(Double... values) {
		if(values.length != columns.size())
			throw new IllegalArgumentException("Values given must be of equal width of buffer. buffer width: "+columns.size()+" values width: "+values.length);
		ArrayList<Double> row = new ArrayList<>();
		for (int i = 0; i < values.length; i++) 
			row.add(values[i]);
		hBuffer.add(row);
		calculateSplitPoint();
		return hBuffer.size();
	}

	
	
	
	/**
	 * 
	 * @param func
	 * @param columName
	 * @return
	 */
	//TODO receive map
	public synchronized int createValueFrom(Function<List<Double>, Double> func,String columName) {
		if(columns.contains(columName))
			throw new IllegalArgumentException("Column name already exists");
		
		if(hBuffer.isEmpty())
			return -1;

		columns.add(columName);
		int  rowSize = columns.size();
		for (int i = 0; i < hBuffer.size(); i++) {
			hBuffer.get(i).add(func.apply(hBuffer.get(i)));
			if(hBuffer.get(i).size() !=  rowSize)
				throw new RuntimeException("Buffer row should be "+ rowSize+" and it is "+hBuffer.get(i).size()+" and index "+i);
		}
		return rowSize;
	}

	
	
	
	/**
	 * 
	 * @param func
	 * @param index
	 * @return
	 */
	public synchronized int tranformValue(Function<List<Double>, Double> func, int index) {
		if(hBuffer.isEmpty())
			return -1;

		for (int i = 0; i < hBuffer.size(); i++) {
			hBuffer.get(i).set(i,func.apply(hBuffer.get(i)));
			if(hBuffer.get(i).size() !=  columns.size())
				throw new RuntimeException("Buffer row should be "+ columns.size()+" and it is "+hBuffer.get(i).size()+" and index "+i);
		}
		return index;
	}

	
	
	
	/**
	 * 
	 * @param func
	 * @param index
	 * @return
	 */
	public synchronized int tranformValue(Function<List<Double>, Double> func, String column) {
		int indexOf = columns.indexOf(column);
		if(indexOf < 0)
			throw new IllegalArgumentException("Column "+column+" does not exists");
		return tranformValue(func, indexOf);
	}

	
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public synchronized int removeColumn(int index) {
		for (int i = 0; i < hBuffer.size(); i++) {
			hBuffer.get(i).remove(index);
			if(hBuffer.get(i).size() !=  columns.size())
				throw new RuntimeException("Buffer row should be "+ columns.size()+" and it is "+hBuffer.get(i).size()+" and index "+i);
		}
		columns.remove(index);
		return  columns.size();
	}

	
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public synchronized int removeColumn(String column) {
		int indexOf = columns.indexOf(column);
		if(indexOf < 0)
			throw new IllegalArgumentException("Column "+column+" does not exists");
		return removeColumn(indexOf);
	}
	
	
	
	
	
	public synchronized List<Sample<Double>> asDoubleTestSamples(){
	    List<Sample<Double>> output = new ArrayList<>();
		for (int i = testSet.key(); i < testSet.value(); i++) {
			Double[] data = new Double[hBuffer.get(i).size()];
			for (int j = 0; j < hBuffer.get(i).size(); j++)
					data[j] = hBuffer.get(i).get(j);
			output.add(Sample.of(data));
		}
		return output;
	}
	
	
	
	
	
	public synchronized List<Sample<Double>> asDoubleValidationSamples(){
	    List<Sample<Double>> output = new ArrayList<>();
		for (int i = validationSet.key(); i < validationSet.value(); i++) {
			Double[] data = new Double[hBuffer.get(i).size()];
			for (int j = 0; j < hBuffer.get(i).size(); j++)
					data[j] = hBuffer.get(i).get(j);
			output.add(Sample.of(data));
		}
		return output;
	}




	public int columnIndexOf(String value) {
		return columns.indexOf(value);
	}
}
