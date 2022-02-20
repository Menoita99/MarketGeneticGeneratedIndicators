package pt.fcul.masters.table.column;

import java.util.List;

import pt.fcul.master.utils.Pair;
import pt.fcul.masters.gp.op.statefull.Rsi;

public class RsiColumn implements CandlestickColumn {

	private Rsi rsi = new Rsi();
	private int column;
	
	public RsiColumn(int column) {
		this.column = column;
	}
	
	@Override
	public Double createValue(List<Double> row) {
		return rsi.apply(new Double[] {row.get(column)});
	}

	@Override
	public String columnName() {
		return "rsi";
	}

	@Override
	public Pair<Integer, Integer> toRemove() {
		return new Pair<Integer, Integer>(0, 14);
	}

}
