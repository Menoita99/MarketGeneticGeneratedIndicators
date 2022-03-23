package pt.fcul.masters.vgp.util;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import lombok.Value;

@Value
public class Vector {

	private final double arr[];
	
	private Vector(double[] arr) {
//		if(arr.length < 1)
//			throw new IllegalArgumentException("Size can't be less then 1 "+arr.length);
		this.arr = arr;
	}
	
	public static Vector of(double[] arr) {
		return new Vector(arr);
	}
	
	
	public static Vector of(Double[] arr) {
		return new Vector(ArrayUtils.toPrimitive(arr));
	}
	
	
	public static Vector of(Number value) {
		return new Vector(new double[] {value.doubleValue()});
	}
	
	
	public static Vector random(int size) {
		double[] v = new double[size];
		for (int i = 0; i < v.length; i++) {
			v[i] = (double)Math.random();
		}
		return new Vector(v);
	}
	

	public static Vector empty() {
		return new Vector(new double[0]);
	}
	
	/*
	 * 
	 */
	
	
	
	/**
	 * Adds two vectors.
	 * Vectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector add(Vector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		double[] v = new double[size];
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0] + vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i] + vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i] + vec.getArr()[i];
		
		return Vector.of(v);
	}
	
	
	
	/**
	 * Subtracts two vectors.
	 * Vectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector sub(Vector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		double[] v = new double[size];
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0] - vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i] - vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i] - vec.getArr()[i];
		
		return Vector.of(v);
	}	
	
	
	/**
	 * Dot product of two vectors.
	 * Vectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector dot(Vector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		double[] v = new double[size];
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0] * vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i] * vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i] * vec.getArr()[i];
		
		return Vector.of(v);
	}	
	
	
	/**
	 * Divides two vectors.
	 * Vectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector div(Vector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		double[] v = new double[size];
		
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = (double)arr[0] / (double)vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = (double)arr[i] / (double)vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = (double)arr[i] / (double)vec.getArr()[i];
		
		return Vector.of(v);
	}	
	
	
	/**
	 * Rest of the division of two vectors.
	 * Vectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector res(Vector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		double[] v = new double[size];
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0] % vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i] % vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i] % vec.getArr()[i];
		
		return Vector.of(v);
	}
	
	/*
	 * Cumulative
	 */
	
	
	/**
	 * Returns a vector with the cumulative sum of this vector
	 * [1,2.5,4.3,0.7] = [1,3.5,7.8, 8.5]
	 */
	public Vector cumSum() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1] + arr[i];
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector with the cumulative sum of this vector
	 * [1,2.5,4.3,0.7] = [1,-1.5,-5.8,-6.5]
	 */
	public Vector cumSub() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1] - arr[i];
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector with the cumulative product of this vector
	 * [1,2.5,4.3,0.7] = [1,2.5,10.75, 7.525]
	 */
	public Vector cumProd() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1] * arr[i];
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector with the cumulative division of this vector
	 * [1,2.5,4.3,0.7] = [1,0.4,~0.093,~0.133]
	 */
	public Vector cumDiv() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = (double)v[i-1] / (double)arr[i];
		return Vector.of(v);
	}

	
	
	/**
	 * Returns a vector with the cumulative division rest of this vector
	 * [1,2.5,4.3,0.7] = [1,1,1,.3]
	 */
	public Vector cumRes() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1] % arr[i];
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector with the cumulative mean of this vector
	 * [1,2.5,4.3,0.7] = [1,1.75,2.6,2.125]
	 */
	public Vector cumMean() {
		double[] v = new double[arr.length];
		v[0] = arr[0];
		double sum = arr[0];
		for (int i = 1; i < arr.length; i++) {
			sum += arr[i];
			v[i] = sum/ (double)i;
		}
		return Vector.of(v);
	}
	
	
	//TODO DISTANCE?
	
	/*
	 * ElementWise
	 */

	
	
	
	/**
	 * Returns a vector where its elements have undergone the abs operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector abs() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.abs(arr[i]);
		return Vector.of(v);
	}	
	
	
	/**
	 * Returns a vector where its elements have undergone the atan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector atan() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.atan(arr[i]);
		return Vector.of(v);
	}	
	
	
	/**
	 * Returns a vector where its elements have undergone the asin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector asin() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.asin(arr[i]);
		return Vector.of(v);
	}	
	
	
	/**
	 * Returns a vector where its elements have undergone the acos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector acos() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.acos(arr[i]);
		return Vector.of(v);
	}
	
	
	
	/**
	 * Returns a vector where its elements have undergone the cos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector cos() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.cos(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the sin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector sin() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.sin(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the tan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector tan() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.tan(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the log operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector log() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.log((double)arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the log operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector sqrt() {
		double[] v = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (double) Math.sqrt(arr[i]);
		return Vector.of(v);
	}
	
	/*
	 * Reductors
	 */

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public Vector maxValue() {
		double max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max < arr[i])
				max = arr[i];
		return Vector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the index of the max value present in this array
	 */
	public Vector indexMaxValue() {
		double max = arr[0];
		int maxIndex = 0;
		for (int i = 1; i < arr.length; i++)
			if(max < arr[i]) {
				max = arr[i];
				maxIndex = i;
			}
		return Vector.of(maxIndex);
	}

	/**
	 * Returns a vector of 1 element with the min value present in this array
	 */
	public Vector minValue() {
		double min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min > arr[i])
				min = arr[i];
		return Vector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the index of the min value present in this array
	 */
	public Vector indexMinValue() {
		double min = arr[0];
		int minIndex = 0;
		for (int i = 1; i < arr.length; i++)
			if(min > arr[i]) {
				min = arr[i];
				minIndex = i;
			}
		return Vector.of(minIndex);
	}
	

	/**
	 * Returns a vector of 1 element with the product of all elements present in the vector
	 */
	public Vector prod() {
		double sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum *= arr[i];
		return Vector.of(sum);
	}
	

	/**
	 * Returns a vector of 1 element with the sum of all elements present in the vector
	 */
	public Vector sum() {
		double sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum += arr[i];
		return Vector.of(sum);
	}
	

	/**
	 * Returns a vector of 1 element with the mean of all elements present in the vector
	 */
	public Vector mean() {
		double sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum += arr[i];
		return Vector.of(sum/(double)arr.length);
	}
	

	/**
	 * Returns The L1 norm that is calculated as the sum of the absolute values of the vector.
	 * ||v||1 = |a1| + |a2| + |a3|
	 * 
	 * [1,2,3] = [6]  
	 */
	public Vector l1Norm() {
		double l1 = 0;
		for (int i = 1; i < arr.length; i++)
				l1 += Math.abs(arr[i]);
		return Vector.of(l1);
	}

	

	/**
	 * Returns The L2 norm that is calculated as the square root of the sum of the squared vector
	 * ||v||2 = sqrt(a1^2 + a2^2 + a3^2)
	 * 
	 * [1,2,3] = [3.7]   
	 */
	public Vector l2Norm() {
		double l2 = 0;
		for (int i = 1; i < arr.length; i++)
				l2 += Math.pow(arr[i],2);
		return Vector.of(Math.sqrt((double)l2));
	}
	

	public Vector neg() {
		double[] arrNeg =new double[arr.length];
		for (int i = 0; i < arrNeg.length; i++)
			arrNeg[i] = -arr[i];
		return Vector.of(arrNeg);
	}
	
	/*
	 * Utils
	 */
	
	/**
	 * Checks if the element has the same length of this array or if it has only one element, 
	 * if not throws an illegal argument exception
	 */
	private void checkSize(Vector toAdd) {
		if(toAdd.getArr().length != arr.length && toAdd.getArr().length == 0)
			throw new IllegalArgumentException("Wrong sized vectors: "+toAdd.getArr().length+"|"+arr.length);
	}
	
	public double asMeanScalar() {
		return mean().getArr()[0];
	}
	
	
	public double last() {
		return arr[arr.length-1];
	}
	
	
	public double first() {
		return arr[0];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(arr);
	}

}
