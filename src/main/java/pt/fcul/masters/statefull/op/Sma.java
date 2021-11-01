package pt.fcul.masters.statefull.op;

import io.jenetics.prog.op.Op;
import pt.fcul.master.utils.LimitedList;

public class Sma implements Op<Double> {
	
	private final LimitedList<Double> history;
	private final int period;
	
	public Sma(int period) {
		this.period = period;
		this.history = new LimitedList<>(period);
	}
	
	@Override
	public synchronized Double apply(Double[] t) {
		history.add(t[0]);
		return history.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
	}

	@Override
	public String name() {
		return "Sma["+period+"]";
	}

	@Override
	public int arity() {
		return 1;
	}
	
    @Override
    public Op<Double> get() {
        return new Sma(period);
    }
    
    @Override
    public String toString() {
    	return name();
    }
}
