package pt.fcul.masters.vgp.util;


import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;

import lombok.Value;

@Value
public class ComplexVector {

	private final Complex arr[];

	private ComplexVector(Complex[] arr) {
		//		if(arr.length < 1)
		//			throw new IllegalArgumentException("Size can't be less then 1 "+arr.length);
		this.arr = arr;
	}


	public static ComplexVector of(Complex c) {
		return new ComplexVector(new Complex[] {c});
	}

	public static ComplexVector of(Number[] arr) {
		Complex[] complexArr = new Complex[arr.length];

		for (int i = 0; i < arr.length; i++)
			complexArr[i] = new Complex(arr[i].doubleValue());

		return new ComplexVector(complexArr);
	}

	public static ComplexVector of(Complex[] arr) {
		return new ComplexVector(arr);
	}


	public static ComplexVector of(Number value) {
		return new ComplexVector(new Complex[] {new Complex(value.doubleValue())});
	}

	/**
	 * Returns a vector where the real and imaginary parts are random and between 0 and 1
	 * @param size
	 * @return
	 */
	public static ComplexVector random(int size) {
		Complex[] v = new Complex[size];

		for (int i = 0; i < v.length; i++)
			v[i] = new Complex(Math.random(),Math.random());

		return new ComplexVector(v);
	}


	public static ComplexVector empty() {
		return new ComplexVector(new Complex[0]);
	}

	/*
	 * 
	 */



	/**
	 * Adds two vectors.
	 * ComplexVectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector add(ComplexVector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		Complex[] v = new Complex[size];

		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0].add(vec.getArr()[i]);
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i].add(vec.getArr()[0]);
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i].add(vec.getArr()[i]);

		return ComplexVector.of(v);
	}



	/**
	 * Subtracts two vectors.
	 * ComplexVectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector sub(ComplexVector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		Complex[] v = new Complex[size];

		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0].subtract(vec.getArr()[i]);
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i].subtract(vec.getArr()[0]);
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i].subtract(vec.getArr()[i]);

		return ComplexVector.of(v);
	}	


	/**
	 * Dot product of two vectors.
	 * ComplexVectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector dot(ComplexVector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		Complex[] v = new Complex[size];

		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0].multiply(vec.getArr()[i]);
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i].multiply(vec.getArr()[0]);
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i].multiply(vec.getArr()[i]);

		return ComplexVector.of(v);
	}	


	/**
	 * Dot product of two vectors.
	 * ComplexVectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector pow(ComplexVector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		Complex[] v = new Complex[size];

		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0].pow(vec.getArr()[i]);
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i].pow(vec.getArr()[0]);
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i].pow(vec.getArr()[i]);

		return ComplexVector.of(v);
	}

	/**
	 * Divides two vectors.
	 * ComplexVectors must be equal sized or at least one of the vectors must have the size equals one
	 * 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector div(ComplexVector vec) {
		checkSize(vec);
		int size = Math.max(vec.getArr().length, arr.length);
		Complex[] v = new Complex[size];


		if(arr.length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[0].divide(vec.getArr()[i]);
		else if(vec.getArr().length == 1)
			for (int i = 0; i < size; i++)
				v[i] = arr[i].divide(vec.getArr()[0]);
		else
			for (int i = 0; i < size; i++)
				v[i] = arr[i].divide(vec.getArr()[i]);

		return ComplexVector.of(v);
	}	



	/*
	 * Cumulative
	 */


	/**
	 * Returns a vector with the cumulative sum of this vector
	 * [1,2.5,4.3,0.7] = [1,3.5,7.8, 8.5]
	 */
	public ComplexVector cumSum() {
		Complex[] v = new Complex[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1].add(arr[i]);
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector with the cumulative sum of this vector
	 * [1,2.5,4.3,0.7] = [1,-1.5,-5.8,-6.5]
	 */
	public ComplexVector cumSub() {
		Complex[] v = new Complex[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1].subtract(arr[i]);
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector with the cumulative product of this vector
	 * [1,2.5,4.3,0.7] = [1,2.5,10.75, 7.525]
	 */
	public ComplexVector cumProd() {
		Complex[] v = new Complex[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1].multiply(arr[i]);
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector with the cumulative division of this vector
	 * [1,2.5,4.3,0.7] = [1,0.4,~0.093,~0.133]
	 */
	public ComplexVector cumDiv() {
		Complex[] v = new Complex[arr.length];
		v[0] = arr[0];
		for (int i = 1; i < arr.length; i++)
			v[i] = v[i-1].multiply(arr[i]);
		return ComplexVector.of(v);
	}



	/**
	 * Returns a vector with the cumulative mean of this vector
	 * [1,2.5,4.3,0.7] = [1,1.75,2.6,2.125]
	 */
	public ComplexVector cumMean() {
		Complex[] v = new Complex[arr.length];
		v[0] = arr[0];
		Complex sum = arr[0];
		for (int i = 1; i < arr.length; i++) {
			sum = sum.add(arr[i]);
			v[i] = sum.divide(Complex.valueOf(i));
		}
		return ComplexVector.of(v);
	}


	/*
	 * ElementWise
	 */


	/**
	 * Get the multiplicative identity of the field. 
	 * The multiplicative identity is the element e1 of the field such thatfor all elements a of the field, the equalities a × e1 =e1 × a = a hold. 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector zeroField() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].getField().getZero();
		return ComplexVector.of(v);
	}

	/**
	 * Get the multiplicative identity of the field. 
	 * The multiplicative identity is the element e1 of the field such thatfor all elements a of the field, the equalities a × e1 =e1 × a = a hold. 
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector oneField() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].getField().getOne();
		return ComplexVector.of(v);
	}

	/**
	 * Returns a vector where its elements have undergone the abs operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector reciprocal() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].reciprocal();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the abs operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector exp() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].exp();
		return ComplexVector.of(v);
	}
	

	/**
	 * Returns a vector where its elements have undergone the abs operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector conjugate() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].conjugate();
		return ComplexVector.of(v);
	}
	

	/**
	 * Returns a vector where its elements have undergone the abs operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector abs() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = Complex.valueOf(arr[i].abs());
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the atan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector atan() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].atan();
		return ComplexVector.of(v);
	}	


	/**
	 * Returns a vector where its elements have undergone the asin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector asin() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = (arr[i].asin());
		return ComplexVector.of(v);
	}	


	/**
	 * Returns a vector where its elements have undergone the acos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector acos() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].acos();
		return ComplexVector.of(v);
	}



	/**
	 * Returns a vector where its elements have undergone the cos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector cos() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].cos();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the sin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector sin() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].sin();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the tan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector tan() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].tan();
		return ComplexVector.of(v);
	}



	/**
	 * Returns a vector where its elements have undergone the cos operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector cosh() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].cosh();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the sin operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector sinh() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].sinh();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the tan operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector tanh() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].tanh();
		return ComplexVector.of(v);
	}

	/**
	 * Returns a vector where its elements have undergone the log operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector log() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].log();
		return ComplexVector.of(v);
	}


	/**
	 * Returns a vector where its elements have undergone the square root operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector sqrt() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].sqrt();
		return ComplexVector.of(v);
	}




	/**
	 * Returns a vector where its elements have undergone the <code>1 - this<sup>2</sup></code> operation
	 * This operation creates new vector rather then changing array values;
	 */
	public ComplexVector sqrt1z() {
		Complex[] v = new Complex[arr.length];
		for (int i = 0; i < arr.length; i++)
			v[i] = arr[i].sqrt1z();
		return ComplexVector.of(v);
	}

	/*
	 * Reductors
	 */

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public ComplexVector maxAbs() {
		Complex max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max.abs() < arr[i].abs())
				max = arr[i];
		return ComplexVector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public ComplexVector maxReal() {
		Complex max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max.getReal() < arr[i].getReal())
				max = arr[i];
		return ComplexVector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public ComplexVector maxImaginary() {
		Complex max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max.getImaginary() < arr[i].getImaginary())
				max = arr[i];
		return ComplexVector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the max value present in this array
	 */
	public ComplexVector maxPhi() {
		Complex max = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(max.getArgument() < arr[i].getArgument())
				max = arr[i];
		return ComplexVector.of(max);
	}

	/**
	 * Returns a vector of 1 element with the min value present in this array
	 */
	public ComplexVector minAbs() {
		Complex min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min.abs() > arr[i].abs())
				min = arr[i];
		return ComplexVector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the min value present in this array
	 */
	public ComplexVector minReal() {
		Complex min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min.getReal() > arr[i].getReal())
				min = arr[i];
		return ComplexVector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the min value present in this array
	 */
	public ComplexVector minImaginary() {
		Complex min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min.getImaginary() > arr[i].getImaginary())
				min = arr[i];
		return ComplexVector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the min value present in this array
	 */
	public ComplexVector minPhi() {
		Complex min = arr[0];
		for (int i = 1; i < arr.length; i++)
			if(min.getArgument() > arr[i].getArgument())
				min = arr[i];
		return ComplexVector.of(min);
	}

	/**
	 * Returns a vector of 1 element with the product of all elements present in the vector
	 */
	public ComplexVector prod() {
		Complex sum = arr[0];
		for (int i = 1; i < arr.length; i++)
			sum = sum.multiply(arr[i]);
		return ComplexVector.of(sum);
	}


	/**
	 * Returns a vector of 1 element with the sum of all elements present in the vector
	 */
	public ComplexVector sum() {
		Complex sum = arr[0];
		for (int i = 1; i < arr.length; i++)
			sum = sum.add(arr[i]);
		return ComplexVector.of(sum);
	}


	/**
	 * Returns a vector of 1 element with the mean of all elements present in the vector
	 */
	public ComplexVector mean() {
		Complex sum = arr[0];
		for (int i = 1; i < arr.length; i++)
			sum = sum.add(arr[i]);
		return ComplexVector.of(sum.divide(Complex.valueOf(arr.length)));
	}


	//	/**
	//	 * Returns The L1 norm that is calculated as the sum of the absolute values of the vector.
	//	 * ||v||1 = |a1| + |a2| + |a3|
	//	 * 
	//	 * [1,2,3] = [6]  
	//	 */
	//	public ComplexVector l1Norm() {
	//		Complex l1 = 0;
	//		for (int i = 1; i < arr.length; i++)
	//				l1 += Math.abs(arr[i]);
	//		return ComplexVector.of(l1);
	//	}
	//
	//	
	//
	//	/**
	//	 * Returns The L2 norm that is calculated as the square root of the sum of the squared vector
	//	 * ||v||2 = sqrt(a1^2 + a2^2 + a3^2)
	//	 * 
	//	 * [1,2,3] = [3.7]   
	//	 */
	//	public ComplexVector l2Norm() {
	//		Complex l2 = 0;
	//		for (int i = 1; i < arr.length; i++)
	//				l2 += Math.pow(arr[i],2);
	//		return ComplexVector.of(Math.sqrt((Complex)l2));
	//	}
	//	
	//
	public ComplexVector neg() {
		Complex[] arrNeg =new Complex[arr.length];
		for (int i = 0; i < arrNeg.length; i++)
			arrNeg[i] = arr[i].negate();
		return ComplexVector.of(arrNeg);
	}

	/*
	 * Utils
	 */

	/**
	 * Checks if the element has the same length of this array or if it has only one element, 
	 * if not throws an illegal argument exception
	 */
	private void checkSize(ComplexVector toAdd) {
		if(toAdd.getArr().length != arr.length && toAdd.getArr().length == 0)
			throw new IllegalArgumentException("Wrong sized vectors: "+toAdd.getArr().length+"|"+arr.length);
	}

	public double realMean() {
		return mean().getArr()[0].getReal();
	}


	public Complex last() {
		return arr[arr.length-1];
	}


	public Complex first() {
		return arr[0];
	}

	@Override
	public String toString() {
		return Arrays.toString(arr);
	}
}
