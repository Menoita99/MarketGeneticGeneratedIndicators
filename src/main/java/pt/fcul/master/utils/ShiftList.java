package pt.fcul.master.utils;

import java.util.Collection;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShiftList<T> extends LinkedList<T>{

	private static final long serialVersionUID = 1L;
	
	private final int maxSize;
	
	public ShiftList(int size) {
		this.maxSize = size;
	}
	
	@Override
	public void add(int index, T element) {
		// Must remove first because index can be 0 and in that case it would remove the wrong element
		if(size()+1 > maxSize)
			removeFirst();
		super.add(index, element);
	}
	
	@Override
	public boolean add(T e) {
		boolean output = super.add(e);
		if(size() > maxSize)
			removeFirst();
		return output;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		c.forEach(this::add);
		return true;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		for (T t : c)
			add(index,t);
		return true;
	}
	
	@Override
	public void addFirst(T e) {
		add(0,e);
	}
	
	@Override
	public void addLast(T e) {
		add(e);
	}
	
	
	public boolean isFull() {
		return size() >= maxSize;
	}

	public boolean isNotEmpty() {
		return size() > 0;
	}
	
	public void setLast(T e) {
		removeLast();
		add(e);
	}
	
	/**
	 * it will replace the last element
	 * @param e element
	 */
	public void replaceLast(T e) {
		if(isNotEmpty())
			set(size()-1, e);
		else 
			throw new IllegalStateException("Can't replace element becuase the list is empty");
	}
}