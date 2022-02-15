package pt.fcul.masters.op.vgp;

import java.io.Serializable;
import java.util.function.Function;

import io.jenetics.prog.op.Op;

public enum VectorialGpOP implements Op<Double[]>, Serializable {

	;
	
	
	
	

	private final String name;
	private final int arity;
	private final Function<Double[][], Double[]> function;

	VectorialGpOP( String name, int arity, Function<Double[][], Double[]> function) {
		assert name != null;
		assert arity >= 0;
		assert function != null;

		this.name = name;
		this.function = function;
		this.arity = arity;
	}

	@Override
	public int arity() {
		return arity;
	}

	@Override
	public Double[] apply(Double[][] t) {
		return function.apply(t);
	}

	
	@Override
	public String toString() {
		return name;
	}
}
