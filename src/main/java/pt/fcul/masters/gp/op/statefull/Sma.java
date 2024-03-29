package pt.fcul.masters.gp.op.statefull;

import io.jenetics.prog.op.Op;
import pt.fcul.masters.utils.ShiftList;

public class Sma implements Op<Double> {
	
	private final ShiftList<Double> history;
	private final int period;
	
	public Sma(int period) {
		this.period = period;
		this.history = new ShiftList<>(period);
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
