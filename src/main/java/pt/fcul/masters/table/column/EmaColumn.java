package pt.fcul.masters.table.column;

import java.util.List;

import pt.fcul.master.utils.Pair;
import pt.fcul.masters.gp.op.statefull.Ema;

public class EmaColumn implements CandlestickColumn {
	
	private Ema ema;
	private int column;
	private int period;
	
	public EmaColumn(int column,int period) {
		this.column = column;
		this.period = period;
		this.ema = new Ema(period);
	}

	@Override
	public Double createValue(List<Double> row) {
		return ema.apply(new Double[]{row.get(column)});
	}

	@Override
	public String columnName() {
		return "Ema["+period+"]";
	}

	@Override
	public Pair<Integer, Integer> toRemove() {
		return new Pair<>(0, period);
	}
}
