package pt.fcul.masters.memory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.jenetics.prog.regression.Sample;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@Data
public class DoubleTable {
//TODO implements Collection<Double>{

	private List<List<Double>> hBuffer = new ArrayList<>();
	private List<String> columns = new ArrayList<>();
	
	private Pair<Integer, Integer> trainSet;
	private Pair<Integer, Integer> validationSet;
	
	private double trainValidationRatio = 0.8;
	
	private Market market = Market.EUR_USD;
	private TimeFrame timeframe = TimeFrame.M15;
	private LocalDateTime datetime = LocalDateTime.of(2005, 1, 1, 0, 0);
	
	public DoubleTable() {
		fetch();
	}

	
	
	
	public DoubleTable(Market market, TimeFrame timeframe) {
		this.market = market;
		this.timeframe = timeframe;
		fetch();
	}

	
	
	
	public DoubleTable(Market market, TimeFrame timeframe, LocalDateTime datetime) {
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		fetch();
	}

	
	
	
	public void calculateSplitPoint() {
		int point = (int)(hBuffer.size() * trainValidationRatio);
		trainSet = new Pair<Integer, Integer>(0, point);
		validationSet = new Pair<Integer, Integer>(point, hBuffer.size());
	}




	private void fetch() {
		columns = new ArrayList<>(List.of("open", "high", "low", "close", "volume"));
		List<Candlestick> candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(market, timeframe, datetime);
		candles.forEach(c -> addRow(c.getOpen(),c.getHigh(),c.getLow(),c.getClose(),c.getVolume()));
		
		calculateSplitPoint();
		
		System.out.println("Train set is from "+candles.get(trainSet.key()).getDatetime() + " " + candles.get(trainSet.value()-1).getDatetime() + " " + (trainSet.value() - trainSet.key()));
		System.out.println("Validation set is from "+candles.get(validationSet.key()).getDatetime() + " " + candles.get(validationSet.value()-1).getDatetime() + " " + (validationSet.value() - validationSet.key()));
	}

	
	
	
	public synchronized int addRow(Double... values) {
		if(values.length != columns.size())
			throw new IllegalArgumentException("Values given must be of equal width of buffer. buffer width: "+columns.size()+" values width: "+values.length);
		ArrayList<Double> row = new ArrayList<>();
		for (int i = 0; i < values.length; i++) 
			row.add(values[i]);
		hBuffer.add(row);
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
	
	
	
	
	public synchronized int createValueFrom(BiFunction<List<Double>,Integer, Double> func,String columName) {
		if(columns.contains(columName))
			throw new IllegalArgumentException("Column name already exists");
		
		if(hBuffer.isEmpty())
			return -1;

		columns.add(columName);
		int  rowSize = columns.size();
		for (int i = 0; i < hBuffer.size(); i++) {
			hBuffer.get(i).add(func.apply(hBuffer.get(i),i));
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
	
	
	
	
	
	public synchronized List<Sample<Double>> asDoubleTrainSamples(){
	    List<Sample<Double>> output = new ArrayList<>();
		for (int i = trainSet.key(); i < trainSet.value(); i++) {
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
	
	
	public void foreach(Consumer<? super List<Double>> action) {
		hBuffer.stream().forEach(action);
	}
	
	
	
	public void trainDataForeach(Consumer<? super List<Double>> action) {
		for (int i = trainSet.key(); i < trainSet.value(); i++)
			action.accept(hBuffer.get(i));
	}
	
	
	
	public void validationDataForeach(Consumer<? super List<Double>> action) {
		for (int i = validationSet.key(); i < validationSet.value(); i++)
			action.accept(hBuffer.get(i));
	}




	public void toCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			hBuffer.stream().forEach(row -> 
				pw.println(row.stream().map(data-> Double.toString(data)).collect(Collectors.joining(","))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void validationToCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			for(int i = validationSet.key(); i< validationSet.value();i++)
				pw.println(hBuffer.get(i).stream().map(data-> Double.toString(data)).collect(Collectors.joining(",")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void trainToCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			for(int i = trainSet.key(); i< trainSet.value();i++)
				pw.println(hBuffer.get(i).stream().map(data-> Double.toString(data)).collect(Collectors.joining(",")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public synchronized List<Double> getColumn(String columnName){
		int columnIndex = columnIndexOf(columnName);
		List<Double> column = new ArrayList<>();
		for (List<Double> row : hBuffer) 
			column.add(row.get(columnIndex));
		return column;
	}




	public synchronized void addColumn(List<Double> column, String columnName) {
		if(columns.contains(columnName))
			throw new IllegalArgumentException("Column name already exists");
		if(column.size() != hBuffer.size())
			throw new IllegalArgumentException("Column must be the same size of the table");
		
		columns.add(columnName);
		for (int i = 0; i < hBuffer.size(); i++)
			hBuffer.get(i).add(column.get(i));
	}
}