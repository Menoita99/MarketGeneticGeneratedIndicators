package pt.fcul.masters.data.normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class StepNormalizer implements Normalizer{

	@Override
	public List<Double> apply(List<Double> data) {
		List<Double> output = new ArrayList<>();
		
		ArrayList<Double> list = new ArrayList<>(new HashSet<>(data));
		list.sort(Double::compare);
		double stepsize = 1/(double)list.size();
		
		Double[] array = list.toArray(new Double[list.size()]);
		
		for (Double value : data) 
			output.add(stepsize * Arrays.binarySearch(array, value)); //use array binary search for optimization
		
		return output;
	}
}
