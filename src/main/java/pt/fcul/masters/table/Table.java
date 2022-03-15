package pt.fcul.masters.table;

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

import io.jenetics.util.RandomRegistry;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@Data
public class Table<T> {
//TODO implements Collection<Double>{

	private List<List<T>> hBuffer = new ArrayList<>();
	protected List<String> columns = new ArrayList<>();
	
	protected Pair<Integer, Integer> trainSet;
	protected Pair<Integer, Integer> validationSet;
	
	private double trainValidationRatio = 0.8;
	
	protected Market market = Market.EUR_USD;
	protected TimeFrame timeframe = TimeFrame.M15;
	protected LocalDateTime datetime = LocalDateTime.of(2005, 1, 1, 0, 0);

	public Table() {
		super();
	}

	public void calculateSplitPoint() {
		int point = (int)(hBuffer.size() * trainValidationRatio);
		trainSet = new Pair<Integer, Integer>(0, point);
		validationSet = new Pair<Integer, Integer>(point, hBuffer.size());
	}

	@SuppressWarnings("unchecked")
	public synchronized int addRow(T... values) {
		if(values.length != columns.size())
			throw new IllegalArgumentException("Values given must be of equal width of buffer. buffer width: "+columns.size()+" values width: "+values.length);
		ArrayList<T> row = new ArrayList<>();
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
	public synchronized int createValueFrom(Function<List<T>, T> func, String columName) {
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
	 * @param columName
	 * @return
	 */
	public synchronized int createValueFrom(BiFunction<List<T>,Integer, T> func, String columName) {
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
	public synchronized int tranformValue(Function<List<T>, T> func, int index) {
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
	public synchronized int tranformValue(Function<List<T>, T> func, String column) {
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


	public int columnIndexOf(String value) {
		return columns.indexOf(value);
	}

	public void foreach(Consumer<? super List<T>> action) {
		hBuffer.stream().forEach(action);
	}

	public void trainDataForeach(Consumer<? super List<T>> action) {
		for (int i = trainSet.key(); i < trainSet.value(); i++)
			action.accept(hBuffer.get(i));
	}

	public void validationDataForeach(Consumer<? super List<T>> action) {
		for (int i = validationSet.key(); i < validationSet.value(); i++)
			action.accept(hBuffer.get(i));
	}

	public void toCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			hBuffer.stream().forEach(row -> 
				pw.println("\""+row.stream().map(data-> data.toString()).collect(Collectors.joining("\",\""))+"\""));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void validationToCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			for(int i = validationSet.key(); i< validationSet.value();i++)
				pw.println(hBuffer.get(i).stream().map(data-> data.toString()).collect(Collectors.joining(",")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void trainToCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			for(int i = trainSet.key(); i< trainSet.value();i++)
				pw.println(hBuffer.get(i).stream().map(data-> data.toString()).collect(Collectors.joining(",")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized List<T> getColumn(String columnName) {
		int columnIndex = columnIndexOf(columnName);
		List<T> column = new ArrayList<>();
		for (List<T> row : hBuffer) 
			column.add(row.get(columnIndex));
		return column;
	}

	public synchronized void addColumn(List<T> column, String columnName) {
		if(columns.contains(columnName))
			throw new IllegalArgumentException("Column name already exists");
		if(column.size() != hBuffer.size())
			throw new IllegalArgumentException("Column must be the same size of the table");
		
		columns.add(columnName);
		for (int i = 0; i < hBuffer.size(); i++)
			hBuffer.get(i).add(column.get(i));
	}

	public synchronized void removeRows(int start, int end) {
		if(start > end) 
			throw new IllegalArgumentException("Start index("+start+") is bigger than end index("+end+") ");
		
		for (int i = start; i < end; i++) 
			hBuffer.remove(start);
		calculateSplitPoint();
	}
	

	public synchronized void removeRow(int i) {
		hBuffer.remove(i);
		calculateSplitPoint();
	}

	public List<T> getRow(int i) {
		return hBuffer.get(i);
	}

	public void removeRows(Pair<Integer,Integer> range) {
		removeRows(range.key(),range.value());
	}
	
	/**
	 * Method used to implement the moving window
	 * This is used to avoid overfitting
	 */
	public Pair<Integer, Integer> randomTrainSet(int offset) {
		if(offset > trainSet.value() / 2)
			throw new IllegalArgumentException("Offset is to big, there is not enough data in the table to support such offset: table size "+ hBuffer.size()+ " offset "+offset);
		
		int randInt = RandomRegistry.random().nextInt(offset);
		return new Pair<>(randInt, trainSet.value() - (offset - randInt));
	}
	
}