package pt.fcul.masters.statefull.op;

import java.util.Random;

import io.jenetics.prog.op.Op;

public class Percentage implements Op<Double>{

	
	private int myrandom = new Random().nextInt(1000);
	
	private double lastNumber;
	
	@Override
	public String name() {
		return "%";
	}

	@Override
	public int arity() {
		return 1;
	}
	
	@Override
	public synchronized Double apply(Double[] t) {
		double value = lastNumber != 0 ? value = (t[0] / lastNumber - 1) * 100 : t[0] > 0 ? 100 : t[0] < 0 ? -100 : 0;
		lastNumber = t[0];
		return value;
	}
	
	@Override
	public String toString() {
		return "(%["+ lastNumber+","+ myrandom + ")";
	}
	
	
    @Override
    public Op<Double> get() {
        return new Percentage();
    }
}