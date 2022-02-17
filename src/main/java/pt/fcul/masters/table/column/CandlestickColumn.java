package pt.fcul.masters.table.column;

import java.util.List;

import pt.fcul.master.utils.Pair;
import pt.fcul.masters.table.Table;

public interface CandlestickColumn {

	Double createValue(List<Double> row);

	String columnName();
	
	Pair<Integer,Integer> toRemove();
	
	default void addColumn(Table<Double> table) { 
		table.createValueFrom(this::createValue, columnName());
	}
	
}
