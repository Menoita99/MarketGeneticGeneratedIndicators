package pt.fcul.masters.op.gp.statefull;

import io.jenetics.prog.op.Op;

public class SimpleReplicableOperation implements Op<Double> {

	private final Ema ema200 = new Ema(200);
	private final Ema ema100 = new Ema(100);
	
	
	@Override
	public synchronized Double apply(Double[] t) {
		return ema200.apply(t) / ema100.apply(t);
	}

	@Override
	public String name() {
		return "SimpleReplicableOperation";
	}

	@Override
	public int arity() {
		return 1;
	}
	
    @Override
    public Op<Double> get() {
        return new SimpleReplicableOperation();
    }
}
