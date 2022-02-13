package pt.fcul.masters.op.gp.statefull;


import java.io.Serializable;
import java.security.SecureRandom;

import io.jenetics.prog.op.Op;
import pt.fcul.master.utils.ShiftList;

public class Ema implements Op<Double> , Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private ShiftList<Double> history;
	private final int period;
	private final double k;
	private double lastEmaValue;
	
	public Ema() {
		this.period = 5 + new SecureRandom().nextInt(195);
		this.history = new ShiftList<>(period);
		this.k = 2 /((double)period + 1 );
	}
	
	public Ema(int period) {
		this.period = period;
		this.history = new ShiftList<>(period);
		this.k = 2 /((double)period + 1 );
	}
	
	//close * weight + ema_previous * (1 - weight)
	@Override
	public synchronized Double apply(Double[] t) {
		if(history != null && history.isFull())
			history = null; // clean ram (garbage collector will clear)
		if(history != null)
			history.add(t[0]);
		lastEmaValue = history == null || history.isFull() ? t[0] * k + lastEmaValue * (1 - k) : history.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
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
