package pt.fcul.masters.vgp.util;

import java.util.Arrays;

import lombok.Data;

@Data
public class Vector {

	private final float arr[];
	
	private Vector(float[] arr) {
//		if(arr.length < 1)
//			throw new IllegalArgumentException("Size can't be less then 1 "+arr.length);
		this.arr = arr;
	}
	
	public static Vector of(float[] arr) {
		return new Vector(arr);
	}
	
	
	public static Vector of(Number value) {
		return new Vector(new float[] {value.floatValue()});
	}
	
	
	public static Vector random(int size) {
		float[] v = new float[size];
		for (int i = 0; i < v.length; i++) {
			v[i] = (float)Math.random();
		}
		return new Vector(v);
	}
	

	public static Vector empty() {
		return new Vector(new float[0]);
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
		float[] v = new float[size];
		
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
		float[] v = new float[size];
		
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
		float[] v = new float[size];
		
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
		float[] v = new float[size];
		
		
		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = (Float)arr[0] / (Float)vec.getArr()[i];
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = (Float)arr[i] / (Float)vec.getArr()[0];
		else
			for (int i = 0; i < size; i++)
				v[i] = (Float)arr[i] / (Float)vec.getArr()[i];
		
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
		float[] v = new float[size];
		
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
		float[] v = new float[arr.length];
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
		float[] v = new float[arr.length];
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
		float[] v = new float[arr.length];
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
		float[] v = new float[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = (Float)v[i-1] / (Float)arr[i];
		return Vector.of(v);
	}

	
	
	/**
	 * Returns a vector with the cumulative division rest of this vector
	 * [1,2.5,4.3,0.7] = [1,1,1,.3]
	 */
	public Vector cumRes() {
		float[] v = new float[arr.length];
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
		float[] v = new float[arr.length];
		v[0] = arr[0];
		float sum = arr[0];
		for (int i = 1; i < arr.length; i++) {
			sum += arr[i];
			v[i] = sum/ (float)i;
		}
		return Vector.of(v);
	}
	
	//TODO DISTANCE?
	
	/*
	 * ElementWise
	 */
	
	
	/**
	 * Returns a vector where its elements have undergone the cos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector cos() {
		float[] v = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (float) Math.cos(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the sin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector sin() {
		float[] v = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (float) Math.sin(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the tan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector tan() {
		float[] v = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (float) Math.tan(arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the log operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector log() {
		float[] v = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (float) Math.log((Float)arr[i]);
		return Vector.of(v);
	}
	
	
	/**
	 * Returns a vector where its elements have undergone the log operation
	 * This operation creates new vector rather then changing array values;
	 */
	public Vector sqrt() {
		float[] v = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (float) Math.sqrt(arr[i]);
		return Vector.of(v);
	}
	
	/*
	 * Reductors
	 */

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public Vector maxValue() {
		float max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max < arr[i])
				max = arr[i];
		return Vector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the index of the max value present in this array
	 */
	public Vector indexMaxValue() {
		float max = arr[0];
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
		float min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min > arr[i])
				min = arr[i];
		return Vector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the index of the min value present in this array
	 */
	public Vector indexMinValue() {
		float min = arr[0];
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
		float sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum *= arr[i];
		return Vector.of(sum);
	}
	

	/**
	 * Returns a vector of 1 element with the sum of all elements present in the vector
	 */
	public Vector sum() {
		float sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum += arr[i];
		return Vector.of(sum);
	}
	

	/**
	 * Returns a vector of 1 element with the mean of all elements present in the vector
	 */
	public Vector mean() {
		float sum = arr[0];
		for (int i = 1; i < arr.length; i++)
				sum += arr[i];
		return Vector.of(sum/(float)arr.length);
	}
	

	/**
	 * Returns The L1 norm that is calculated as the sum of the absolute values of the vector.
	 * ||v||1 = |a1| + |a2| + |a3|
	 * 
	 * [1,2,3] = [6]  
	 */
	public Vector l1Norm() {
		float l1 = 0;
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
		float l2 = 0;
		for (int i = 1; i < arr.length; i++)
				l2 += Math.pow(arr[i],2);
		return Vector.of(Math.sqrt((Float)l2));
	}
	

	public Vector neg() {
		float[] arrNeg =new float[arr.length];
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
	
	public float asMeanScalar() {
		return mean().getArr()[0];
	}
	
	
	public float last() {
		return arr[arr.length-1];
	}
	
	
	public float first() {
		return arr[0];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(arr);
	}
}
