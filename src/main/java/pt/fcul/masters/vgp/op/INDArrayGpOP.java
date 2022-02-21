package pt.fcul.masters.vgp.op;

import java.io.Serializable;
import java.util.function.Function;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import io.jenetics.prog.op.Op;

/**
 * This class uses vector of float to type for the sake of optimization
 * 
 * @author Owner
 *
 */
public enum INDArrayGpOP implements Op<INDArray>, Serializable {
	
	/*
	 * +---------------------------------------------------------+
	 * |             Transform and Scalar operations             |
	 * +---------------------------------------------------------+
	 */

	/**
	 * Adds two arrays.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * E.g: 
	 * 
	 * [0,0,0] + [1,2,3] = [1,2,3]
	 * 
	 * [0,0,0] + [1] = [1,1,1]
	 * 
	 * [4,5,6] + [1] = [5,6,7]
	 * 			 [2]   [6,7,8]
	 * 			 [3]   [7,8,9]
	 * 
	 * Errors:
	 * 
	 * [0,0,0] + [1,2] 
	 * 
	 * [0,0,0] + [[1,2]
	 * 			  [1,2]] 
	 */
	ADD("V_ADD",2, v -> v[0].add(v[1])),

	/**
	 * Subtracts two arrays.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * E.g: 
	 * 
	 * [0,0,0] - [1,2,3] = [-1,-2,-3]
	 * 
	 * [0,0,0] - [1] = [-1,-1,-1]
	 * 
	 * [4,5,6] - [1] = [3,4,5]
	 * 			 [2]   [2,3,4]
	 * 			 [3]   [1,2,3]
	 * 
	 * [1] - [4,5,6] = [-3,-4,-5]
	 * [2]   		   [-2,-3,-4]
	 * [3]   		   [-1,-2,-3]
	 * Errors:
	 * 
	 * [0,0,0] - [1,2] 
	 * 
	 * [0,0,0] - [[1,2]
	 * 			  [1,2]] 
	 */
	SUB("V_SUB",2, v -> v[0].sub(v[1])),

	/**
	 * Multiplies two arrays.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * E.g: 
	 * 
	 * [1,2,3] * [1,2,3] = [1,4,9]
	 * 
	 * [1,2,3] * [3] = [1,6,9]
	 * 
	 * [4,5,6] * [1] = [ 4, 5, 6]
	 * 			 [2]   [ 8,10,12]
	 * 			 [3]   [12,15,18]
	 * 
	 * 
	 * Errors:
	 * 
	 * [0,0,0] * [1,2] 
	 * 
	 * [0,0,0] * [[1,2]
	 * 			  [1,2]] 
	 */
	MUL("V_MUL",2, v -> v[0].mul(v[1])),

	/**
	 * Divides two arrays.
	 * It does not change any of the arrays, it creates a new one representing the result.
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
	 * 
	 * [4,5,6] / [1] = [  4,  5,  6]
	 * 			 [2]   [  2,2.5,  3]
	 * 			 [3]   [1.3,1.6,  2]
	 * 
	 * Errors:
	 * 
	 * [0,0,0] / [1,2] 
	 * 
	 * [0,0,0] / [[1,2]
	 * 			  [1,2]] 
	 */
	DIV("V_DIV",2, v -> v[0].div(v[1])),

	/**
	 * Cumulative sum of a vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,4,10] = [1,3,7,17]
	 * 
	 */
	CUM_SUM("CUM_SUM",1, v -> v[0].cumsum(0)),
	
	
	//TODO SUB ARRAY?
	//TODO SUB Euclidean distance
	
	/*
	 * +---------------------------------------------------------+
	 * |                      Reductors                          |
	 * +---------------------------------------------------------+
	 */

	/**
	 * Divides two arrays.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * E.g: (11 x 4) + (3 x -2) + (-5 x-1) = 3
	 * 
	 * [1,3,-5] . [4,-2,-1] = [3]    
	 * 
	 * [1,3,-5] . [ 4] = [3] 
	 * 			  [-2]
	 * 			  [-1]
	 * 
	 * !Warning
	 * To be able to use dot product even when one of the arrays is a scalar 
	 * we change the operation to a simple multiplication in this case
	 * 
	 * Errors:
	 * 
	 * [0,0,0] . [1,2] 
	 * 
	 * [0,0,0] . [[1,2]
	 * 			  [1,2]] 
	 */
	DOT("V_DOT",2, v -> v[0].size(0) == 1 || v[1].size(0) == 1 ? v[0].mul(v[1]) : Transforms.dot(v[0],v[1])),
	
	/**
	 * Returns the index of the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [2]    
	 */
	IMAX("V_IMAX",1, v -> v[0].argMax(0)),
	
	/**
	 * Returns the max element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [3]    
	 */
	MAX("V_MAX",1, v -> Nd4j.create(new float[]{v[0].maxNumber().floatValue()})),
	
	/**
	 * Returns the min element of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-5]    
	 */
	MIN("V_MIN",1, v -> Nd4j.create(new float[]{v[0].minNumber().floatValue()})),
	
	/**
	 * Returns the sum of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-1]    
	 */
	SUM("V_SUM",1, v -> Nd4j.create(new float[]{v[0].sumNumber().floatValue()})),

	/**
	 * Returns the multiplication of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,-5] = [-15]    
	 */
	EMUL("V_EMUL",1, v -> Nd4j.create(new float[]{v[0].prodNumber().floatValue()})),

	/**
	 * Returns The L1 norm that is calculated as the sum of the absolute values of the vector.
	 * ||v||1 = |a1| + |a2| + |a3|
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,3] = [6]    
	 */
	L1_NORM("L1_NORM",1, v -> Nd4j.create(new float[]{v[0].norm1Number().floatValue()})),

	/**
	 * Returns The L2 norm that is calculated as the square root of the sum of the squared vector
	 * ||v||2 = sqrt(a1^2 + a2^2 + a3^2)
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,3] = [3.7]    
	 */
	L2_NORM("L2_NORM",1, v -> Nd4j.create(new float[]{v[0].norm2Number().floatValue()})),

	/**
	 * Returns the standart deviation of each value in a vector from the mean of the vector.
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,2,3] = [1]    
	 * [1,2,4] = [1.5]   
	 */
	STD_DEV("STD_DEV",1, v -> Nd4j.create(new float[]{v[0].stdNumber().floatValue()})),

	/**
	 * Returns the mean of all elements of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 * 
	 * [1,3,8] = [4]    
	 */
	MEAN("V_MEAN",1, v -> Nd4j.create(new float[]{v[0].meanNumber().floatValue()})),

	/**
	 * Applies the abs operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ABS("V_ABS",1, v -> Transforms.abs(v[0])),
	
	/**
	 * Applies the acos operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ACOS("V_ACOS",1, v -> Transforms.acos(v[0])),
	
	/**
	 * Applies the asin operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ASIN("V_ASIN",1, v -> Transforms.asin(v[0])),	
	
	/**
	 * Applies the atan operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	ATAN("V_ATAN",1, v -> Transforms.atan(v[0])),
	
	/**
	 * Applies the cos operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	COS("V_COS",1, v -> Transforms.cos(v[0])),
	
	/**
	 * Applies the sin operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	SIN("V_SIN",1, v -> Transforms.sin(v[0])),	
	
	/**
	 * Applies the tan operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	TAN("V_TAN",1, v -> Transforms.tan(v[0])),
	
	/**
	 * Applies the log operation to all element of the vector
	 * It does not change any of the arrays, it creates a new one representing the result.
	 */
	LOG("V_LOG",1, v -> Transforms.log(v[0])),
	
	;
	
	private final String name;
	private final int arity;
	private final Function<INDArray[], INDArray> function;

	INDArrayGpOP( String name, int arity, Function<INDArray[], INDArray> function) {
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
	public INDArray apply(INDArray[] t) {
		return function.apply(t);
	}

	
	@Override
	public String toString() {
		return name;
	}
}
