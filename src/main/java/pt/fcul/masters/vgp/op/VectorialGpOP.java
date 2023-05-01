package pt.fcul.masters.vgp.op;

import java.io.Serializable;
import java.util.function.Function;

import io.jenetics.prog.op.Op;
import pt.fcul.masters.vgp.util.Vector;

public enum VectorialGpOP implements Op<Vector>, Serializable {
	
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
	 * Return a vector with the rest of divisions between two vectors.
	 * It does not change any of the vectors, it creates a new one representing the result.
	 */
	RES("V_RES",2, v -> v[0].res(v[1])),

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
	
	
	STD_VAR("STD_VAR",1,v -> v[0].standardDeviation()),
	
	
	/**
	 * Returns the index of the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [1]    
	 */
	IMAX("V_IMAX",1, v -> v[0].indexMaxValue()),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX("V_MAX",1, v ->v[0].maxValue()),
	
	/**
	 * Returns the min element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-5]    
	 */
	MIN("V_MIN",1, v -> v[0].minValue()),
	
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
	 * Returns The L1 norm that is calculated as the sum of the absolute values of the vector.
	 * ||v||1 = |a1| + |a2| + |a3|
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,3] = [6]    
	 */
	L1_NORM("L1_NORM",1, v -> v[0].l1Norm()),

	/**
	 * Returns The L2 norm that is calculated as the square root of the sum of the squared vector
	 * ||v||2 = sqrt(a1^2 + a2^2 + a3^2)
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,3] = [3.7]    
	 */
	L2_NORM("L2_NORM",1,v -> v[0].l2Norm()),

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
	 * Negates all elements of this vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	NEG("V_NEG",1, v ->  v[0].neg()), 
	
	
	
	/**
	 * Returns 1 if the vectors mean is bigger then the other, otherwise returns -1
	 */
	GT_THEN("GT_THEN" ,2, v -> v[0].asMeanScalar() > v[1].asMeanScalar() ? Vector.of(1) : Vector.of(0))
	;

	public static void main(String[] args) {
		System.out.println(Vector.of(new double[] {-1,0,1,2,3,} ).log());
	}
	
	private final String name;
	private final int arity;
	private final Function<Vector[], Vector> function;

	VectorialGpOP( String name, int arity, Function<Vector[], Vector> function) {
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
	public Vector apply(Vector[] t) {
		return function.apply(t);
	}

	
	@Override
	public String toString() {
		return name;
	}
}
