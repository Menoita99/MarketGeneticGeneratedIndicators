package pt.fcul.masters.vgp.op;

import java.io.Serializable;
import java.util.function.Function;

import io.jenetics.prog.op.Op;
import pt.fcul.masters.vgp.util.ComplexVector;

public enum ComplexVectorialGpOP  implements Op<ComplexVector>, Serializable {
	
	/**
	 * Adds two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 * 
	 * [0,0,0] + [1,2,3] = [1,2,3]
	 * 
	 * [0,0,0] + [1] = [1,1,1]
	 * 
	 */
	ADD("V_ADD",2, v -> v[0].add(v[1])),

	/**
	 * Subtracts two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 * 
	 * [0,0,0] - [1,2,3] = [-1,-2,-3]
	 * 
	 * [0,0,0] - [1] = [-1,-1,-1]
	 * 
	 */
	SUB("V_SUB",2, v -> v[0].sub(v[1])),

	/**
	 * Makes the dot product of two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 * 
	 * [1,2,3] . [1,2,3] = [1,4,9]
	 * 
	 * [1,2,3] . [3] = [1,6,9]
	 * 
	 */
	DOT("V_DOT",2, v -> v[0].dot(v[1])),

	/**
	 * Divides two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 * 
	 * 0/0 = Nan
	 * 1/0 = Infinity
	 * 0/1 + 0
	 * 
	 * E.g: 
	 * 
	 * [1,2,3] / [1,2,3] = [1,1,1]
	 * 
	 * [1,2,3] / [2] = [0.5,1,1.5]
	 */
	DIV("V_DIV",2, v -> v[0].div(v[1])),
	

	/**
	 * Divides two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 * 
	 * 0/0 = Nan
	 * 1/0 = Infinity
	 * 0/1 + 0
	 * 
	 * E.g: 
	 * 
	 * [1,2,3] / [1,2,3] = [1,1,1]
	 * 
	 * [1,2,3] / [2] = [0.5,1,1.5]
	 */
	POW("V_POW",2, v -> v[0].pow(v[1])),


	/**
	 * Cumulative sum of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_SUM("CUM_SUM",1, v -> v[0].cumSum()),

	/**
	 * Cumulative sub of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_SUB("CUM_SUB",1, v -> v[0].cumSub()),

	/**
	 * Cumulative mean of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_MEAN("CUM_MEAN",1, v -> v[0].cumMean()),

	/**
	 * Cumulative PROD of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_PROD("CUM_PROD",1, v -> v[0].cumProd()),

	/**
	 * Cumulative DIV of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_DIV("CUM_DIV",1, v -> v[0].cumDiv()),
	
	
	
	//TODO SUB ARRAY?
	//TODO SUB Euclidean distance
	//TODO add processing signal operations
	
	/*
	 * +---------------------------------------------------------+
	 * |                      Reductors                          |
	 * +---------------------------------------------------------+
	 */
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX_ABS("MAX_ABS",1, v ->v[0].maxAbs()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX_PHI("MAX_PHI",1, v ->v[0].maxPhi()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX_REAL("MAX_REAL",1, v ->v[0].maxReal()),	
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX_IMG("MAX_IMG",1, v ->v[0].maxImaginary()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MIN_ABS("MIN_ABS",1, v ->v[0].minAbs()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MIN_PHI("MIN_PHI",1, v ->v[0].minPhi()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MIN_REAL("MIN_REAL",1, v ->v[0].minReal()),	
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MIN_IMG("MIN_IMG",1, v ->v[0].minImaginary()),
	
	
	/**
	 * Returns the sum of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-1]    
	 */
	SUM("V_SUM",1, v -> v[0].sum()),

	/**
	 * Returns the multiplication of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-15]    
	 */
	PROD("V_PROD",1, v -> v[0].prod()),
	
	/**
	 * Returns the last element of the vector
	 */
	LAST("LAST",1, v-> ComplexVector.of(v[0].last())),
	
	/**
	 * Returns the first element of the vector
	 */
	FIRST("FIRST",1, v-> ComplexVector.of(v[0].first())),

	/**
	 * Returns the mean of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,8] = [4]    
	 */
	MEAN("V_MEAN",1, v ->  v[0].mean()),

	/**
	 * Applies the abs operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ABS("V_ABS",1, v -> v[0].abs()),
	
	/**
	 * Applies the acos operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	COSH("V_COSH",1, v -> v[0].cosh()),
	
	/**
	 * Applies the asin operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	SINH("V_SINH",1, v -> v[0].sinh()),	
	
	/**
	 * Applies the atan operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	TANH("V_TANH",1, v -> v[0].tanh()),
	
	/**
	 * Applies the acos operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ACOS("V_ACOS",1, v -> v[0].acos()),
	
	/**
	 * Applies the asin operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ASIN("V_ASIN",1, v -> v[0].asin()),	
	
	/**
	 * Applies the atan operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ATAN("V_ATAN",1, v -> v[0].atan()),
	
	/**
	 * Applies the cos operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	COS("V_COS",1, v ->  v[0].cos()),
	
	/**
	 * Applies the sin operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	SIN("V_SIN",1, v ->  v[0].sin()),	
	
	/**
	 * Applies the tan operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	TAN("V_TAN",1, v ->  v[0].tan()),
	
	/**
	 * Applies the log operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	LOG("V_LOG",1, v ->  v[0].log()),
	
	/**
	 * Applies the natural exponential operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	EXP("V_EXP",1, v ->  v[0].exp()),
	
	/**
	 * Applies the sqrt operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	SQRT("V_SQRT",1, v ->  v[0].sqrt()),
	
	/**
	 * Applies the sqrt1z operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	SQRT1Z("V_SQRT1Z",1, v ->  v[0].sqrt1z()),
	
	/**
	 * Applies the reciprocal operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	RECIPROCAL("V_RECIPROCAL",1, v ->  v[0].reciprocal()),
	
	/**
	 * Applies the conjugate operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	CONJUGATE("V_CONJUGATE",1, v ->  v[0].conjugate()),
	
	/**
	 * Applies the ZERO_FIELD operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ZERO_FIELD("V_ZERO_FIELD",1, v ->  v[0].zeroField()),
	
	/**
	 * Applies the ONE_FIELD operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ONE_FIELD("V_ONE_FIELD",1, v ->  v[0].oneField()),
	
	/**
	 * Negates all elements of this vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	NEG("V_NEG",1, v ->  v[0].neg()),
	
	
	/**
	 * Returns 1 if the vectors real mean is bigger then the other, otherwise returns -1
	 */
	GT_THEN_REAL("GT_THEN_REAL" ,2, v -> v[0].realMean() > v[1].realMean() ? ComplexVector.of(1) : ComplexVector.of(-1))
	;

	
	private final String name;
	private final int arity;
	private final Function<ComplexVector[], ComplexVector> function;

	ComplexVectorialGpOP( String name, int arity, Function<ComplexVector[], ComplexVector> function) {
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
	public ComplexVector apply(ComplexVector[] t) {
		return function.apply(t);
	}

	
	@Override
	public String toString() {
		return name;
	}
}
