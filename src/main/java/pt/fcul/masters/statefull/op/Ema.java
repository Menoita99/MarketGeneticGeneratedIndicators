package pt.fcul.masters.statefull.op;


import io.jenetics.prog.op.Op;
import pt.fcul.master.utils.LimitedList;

public class Ema implements Op<Double> {
	
	private final LimitedList<Double> history;
	private final int period;
	private final double k;
	private double lastEmaValue;
	
	public Ema(int period) {
		this.period = period;
		this.history = new LimitedList<>(period);
		this.k = 2 /(period + 1 );
	}
	
	//close * weight + ema_previous * (1 - weight)
	@Override
	public synchronized Double apply(Double[] t) {
		history.add(t[0]);
		lastEmaValue = history.isFull() ? t[0] * k + lastEmaValue * (1 - k) : history.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		return lastEmaValue;
	}

	@Override
	public String name() {
		return "Ema["+period+"]";
	}

	@Override
	public int arity() {
		return 1;
	}
	
    @Override
    public Op<Double> get() {
        return new Ema(period);
    }
    
    @Override
    public String toString() {
    	return name();
    }
}
