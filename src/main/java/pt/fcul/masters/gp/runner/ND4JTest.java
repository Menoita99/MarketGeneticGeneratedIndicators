package pt.fcul.masters.gp.runner;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


public class ND4JTest {
	
	public static void main(String[] args) {
		
		INDArray line = Nd4j.create(new float[] {1,2,4,10});
		INDArray column = Nd4j.create(new float[][] {{4},{-2},{-1}});
		System.out.println(line.mul(column).add(line).sub(column).div(line));
//		System.out.println("-------------------");
//		System.out.println(Transforms.dot(line,Nd4j.ones(1)));
//		System.out.println("-------------------");
//		System.out.println(line);
	}

	public static void extracted() {
		INDArray ones = Nd4j.ones(3,1);
		System.out.println(ones);
		System.out.println("-------------------");
		System.out.println(Nd4j.create(new float[] {1,2,3}));
		System.out.println("-------------------");
		System.out.println(ones.mul(Nd4j.create(new float[] {1,2,3})));
		System.out.println("-------------------");
		System.out.println(Nd4j.create(new float[] {1,2,3}).mul(ones));
		System.out.println("-------------------");
		System.out.println(ones.mul(Nd4j.create(new float[] {1})));
		System.out.println("-------------------");
		System.out.println(ones.mul(Nd4j.create(new float[] {1})));
		System.out.println("-------------------");
		System.out.println(ones);
	}

}
