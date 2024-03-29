package pt.fcul.masters.table.column;

import java.util.List;

import io.jenetics.prog.op.Op;
import pt.fcul.masters.gp.op.statefull.Ema;
import pt.fcul.masters.utils.Pair;

public class EmaDiffColumn implements CandlestickColumn{

	private Op<Double> ema;
	private int column;
	private int period;
	
	public EmaDiffColumn(int period,int column) {
		this.period = period;
		this.column = column;
		this.ema = new Ema(period);
	}
	
	@Override
	public Double createValue(List<Double> row) {
		return ema.apply(new Double[] {row.get(column)});
	}

	@Override
	public String columnName() {
		return "EmaDiff"+period;
	}

	@Override
	public Pair<Integer, Integer> toRemove() {
		return new Pair<Integer, Integer>(0, period);
	}
}
