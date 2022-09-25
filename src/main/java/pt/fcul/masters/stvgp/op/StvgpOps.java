package pt.fcul.masters.stvgp.op;

import java.util.function.Function;

import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.vgp.util.Vector;

public enum StvgpOps implements StvgpOp{
	
	
	/*
	 * Relational Operators
	 */

	CUM_MEAN_GT(2,"CUM_MEAN_GT",v -> StvgpType.of(v[0].getAsVectorType().cumMean().asMeanScalar() > v[1].getAsVectorType().sum().asMeanScalar()), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.bool()),
	
	SUM_GT(2,"SUM_GT",v -> StvgpType.of(v[0].getAsVectorType().sum().asMeanScalar() > v[1].getAsVectorType().sum().asMeanScalar()), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.bool()),
	
	MEAN_GT(2,"MEAN_GT",v -> StvgpType.of(v[0].getAsVectorType().asMeanScalar() > v[1].getAsVectorType().asMeanScalar()), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.bool()),

	VECT_IF_ELSE(3,"VEC_IF_ELSE",v -> v[0].getAsBooleanType() ? v[1] : v[2], new StvgpType[] {StvgpType.bool(),StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),

	IF_ELSE(3,"IF_ELSE",v -> v[0].getAsBooleanType() ? v[1] : v[2], new StvgpType[] {StvgpType.bool(),StvgpType.bool(),StvgpType.bool()}, StvgpType.bool()),

	/*
	 * Vectorial operators
	 */
	
	
	ADD(2,"ADD",v -> StvgpType.of(v[0].getAsVectorType().add(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	SUB(2,"SUB",v -> StvgpType.of(v[0].getAsVectorType().sub(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	DOT(2,"DOT",v -> StvgpType.of(v[0].getAsVectorType().dot(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	DIV(2,"SUB",v -> StvgpType.of(v[0].getAsVectorType().div(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	RES(2,"ADD",v -> StvgpType.of(v[0].getAsVectorType().res(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
	CUM_SUM(2,"SUB",v -> StvgpType.of(v[0].getAsVectorType().sub(v[1].getAsVectorType())), new StvgpType[] {StvgpType.vector(),StvgpType.vector()}, StvgpType.vector()),
	
	
//	IMAX(1,"IMAX",v -> StvgpType.of(v[0].getAsVectorType().indexMaxValue()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	MAX(1,"MAX",v -> StvgpType.of(v[0].getAsVectorType().maxValue()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),

	
	STD_VAR(1,"STD_VAR",v -> StvgpType.of(v[0].getAsVectorType().standardDeviation()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	MIN(1,"MIN",v -> StvgpType.of(v[0].getAsVectorType().minValue()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	SUM(1,"SUM",v -> StvgpType.of(v[0].getAsVectorType().sum()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	PROD(1,"PROD",v -> StvgpType.of(v[0].getAsVectorType().prod()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	L1_NORM(1,"L1_NORM",v -> StvgpType.of(v[0].getAsVectorType().l1Norm()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	L2_NORM(1,"L2_NORM",v -> StvgpType.of(v[0].getAsVectorType().l2Norm()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	ABS(1,"ABS",v -> StvgpType.of(v[0].getAsVectorType().abs()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	ACOS(1,"ACOS",v -> StvgpType.of(v[0].getAsVectorType().acos()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	ASIN(1,"ASIN",v -> StvgpType.of(v[0].getAsVectorType().asin()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	ATAN(1,"ATAN",v -> StvgpType.of(v[0].getAsVectorType().atan()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	COS(1,"COS",v -> StvgpType.of(v[0].getAsVectorType().cos()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	SIN(1,"SIN",v -> StvgpType.of(v[0].getAsVectorType().sin()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	TAN(1,"ASIN",v -> StvgpType.of(v[0].getAsVectorType().tan()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	LOG(1,"LOG",v -> StvgpType.of(v[0].getAsVectorType().log()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),

	
	NEG(1,"NEG",v -> StvgpType.of(v[0].getAsVectorType().neg()), new StvgpType[] {StvgpType.vector()}, StvgpType.vector()),
	
	
	/*
	 * Boolean operators
	 */
	
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
	 * Boolean Operator
	 */
	NOT(1,"NOT",v -> StvgpType.of(!v[0].getAsBooleanType()), new StvgpType[] {StvgpType.bool()}, StvgpType.bool()),
	
	/*
	 * Constants
	 */
	
	/**
	 * Boolean terminal
	 */
	TRUE(0,"TRUE",v -> new StvgpType(true), new StvgpType[0] , StvgpType.bool()),
	
	/**
	 * Boolean terminal
	 */
	FALSE(0,"FALSE",v -> new StvgpType(false), new StvgpType[0] , StvgpType.bool()), 
	
	/**
	 * Vectorial terminal
	 */
	ONE(0,"ONE",v -> StvgpType.of(Vector.of(1)), new StvgpType[0] , StvgpType.vector()),
	
	/**
	 * Vectorial terminal
	 */
	ZERO(0,"ZERO",v -> StvgpType.of(Vector.of(0)), new StvgpType[0] , StvgpType.vector()),
	
	/**
	 * Vectorial terminal
	 */
	MINUS_ONE(0,"MINUS_ONE",v -> StvgpType.of(Vector.of(-1)), new StvgpType[0] , StvgpType.vector());
	
	
	
	
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
}
