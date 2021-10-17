package pt.fcul.masters.memory;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class MemoryManager {//TODO implements Collection<Number>{

	private static List<List<Number>> buffer;
	private static List<String> columns =  new LinkedList<>();

	private MemoryManager() {
		buffer = new LinkedList<>();
		fetch();
	}

	private void fetch() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @param func
	 * @param columName
	 * @return
	 */
	public synchronized int createValueFrom(Function<List<Number>, Number> func,String columName) {
		if(buffer.isEmpty())
			return -1;

		int bufferRowSize = buffer.get(0).size() + 1;
		for (int i = 0; i < buffer.size(); i++) {
			buffer.get(i).add(func.apply(buffer.get(i)));
			if(buffer.get(i).size() != bufferRowSize)
				throw new RuntimeException("Buffer row should be "+bufferRowSize+" and it is "+buffer.get(i).size()+" and index "+i);
		}
		columns.add(columName);
		return bufferRowSize;
	}

	/**
	 * 
	 * @param func
	 * @param index
	 * @return
	 */
	public synchronized int tranformValue(Function<List<Number>, Number> func, int index) {
		if(buffer.isEmpty())
			return -1;

		int bufferRowSize = buffer.get(0).size();
		for (int i = 0; i < buffer.size(); i++) {
			buffer.get(i).set(i,func.apply(buffer.get(i)));
			if(buffer.get(i).size() != bufferRowSize)
				throw new RuntimeException("Buffer row should be "+bufferRowSize+" and it is "+buffer.get(i).size()+" and index "+i);
		}
		return index;
	}
	
	/**
	 * 
	 * @param func
	 * @param index
	 * @return
	 */
	public synchronized int tranformValue(Function<List<Number>, Number> func, String column) {
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
		int bufferRowSize = buffer.get(0).size() -1;
		for (int i = 0; i < buffer.size(); i++) {
			buffer.get(i).remove(index);
			if(buffer.get(i).size() != bufferRowSize)
				throw new RuntimeException("Buffer row should be "+bufferRowSize+" and it is "+buffer.get(i).size()+" and index "+i);
		}
		return bufferRowSize;
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
}
