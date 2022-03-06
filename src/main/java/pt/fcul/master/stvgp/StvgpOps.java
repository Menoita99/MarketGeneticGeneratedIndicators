package pt.fcul.master.stvgp;

import java.util.function.Function;

import pt.fcul.masters.vgp.util.Vector;

public enum StvgpOps implements StvgpOp{
	
	MEAN_GT(2,"MEAN_GT",v -> StvgpType.of(v[0].getAsVectorType().asMeanScalar() > v[1].getAsVectorType().asMeanScalar()), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.bool()),

	
	ADD(2,"ADD",v -> StvgpType.of(v[0].getAsVectorType().add(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	SUB(2,"SUB",v -> StvgpType.of(v[0].getAsVectorType().sub(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	/**
	 * Boolean Operator
	 */
	AND(2,"AND",v -> StvgpType.of(v[0].getAsBooleanType() && v[1].getAsBooleanType()), new StvgpType[] {StvgpType.bool(),StvgpType.bool()}, StvgpType.bool()),
	
	/**
	 * Boolean Operator
	 */
	OR(2,"OR",v -> StvgpType.of(v[0].getAsBooleanType() || v[1].getAsBooleanType()), new StvgpType[] {StvgpType.bool(),StvgpType.bool()}, StvgpType.bool()),
	
	/**
	 * Boolean Operator
	 */
	XOR(2,"XOR",v -> StvgpType.of(v[0].getAsBooleanType() ^ v[1].getAsBooleanType()), new StvgpType[] {StvgpType.bool(),StvgpType.bool()}, StvgpType.bool()),
	
	/**
	 * Boolean terminal
	 */
	TRUE(0,"TRUE",v -> new StvgpType(true), new StvgpType[0] , StvgpType.bool()),
	
	/**
	 * Boolean terminal
	 */
	FALSE(0,"FALSE",v -> new StvgpType(false), new StvgpType[0] , StvgpType.bool());
	
	
	
	
	private int arity;
	private Function<StvgpType[], StvgpType> function;
	private StvgpType[] arityType;
	private StvgpType outputype;
	private String name;

	StvgpOps(int arity,String name,Function<StvgpType[], StvgpType> function,StvgpType[] arityType,StvgpType outputype){
		this.arity = arity;
		this.name = name;
		this.function = function;
		this.arityType = arityType;
		this.outputype = outputype;
	}

	@Override
	public int arity() {
		return arity;
	}

	@Override
	public StvgpType apply(StvgpType[] args) {
		return function.apply(args);
	}

	@Override
	public StvgpType[] arityType() {
		return arityType;
	}

	@Override
	public StvgpType outputType() {
		return outputype;
	}
	
	@Override
	public String toString() {
		return name;
	}

	static StvgpOp randomVector(int size) {
		return new StvgpOp() {
			
			private final StvgpType randVect = StvgpType.of(Vector.random(size));
			
			@Override
			public StvgpType apply(StvgpType[] t) {
				return randVect;
			}
			
			@Override
			public String name() {
				return randVect.toString();
			}
			
			@Override
			public int arity() {
				return 0;
			}
			
			@Override
			public StvgpType outputType() {
				return StvgpType.vector();
			}
			
			@Override
			public StvgpType[] arityType() {
				return new StvgpType[0];
			}
		};
	}

}
