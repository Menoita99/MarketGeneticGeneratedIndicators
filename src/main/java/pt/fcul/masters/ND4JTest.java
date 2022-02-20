package pt.fcul.masters;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


public class ND4JTest {
	
	public static void main(String[] args) {
		INDArray line = Nd4j.create(new float[] {1,2,4});
		INDArray column = Nd4j.create(new float[][] {{4},{-2},{-1}});
		System.out.println(line);
		System.out.println("-------------------");
		System.out.println(line.stdNumber());
		System.out.println("-------------------");
		System.out.println(line);
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
