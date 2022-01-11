package pt.fcul.masters.data.normalizer;

import java.util.ArrayList;
import java.util.List;

public class DerivativeNormalizer implements Normalizer {

	@Override
	public List<Double> apply(List<Double> data) {
		List<Double> output = new ArrayList<>();
		List<Double> list = new ArrayList<>(data);
		list.sort(Double::compare);
		
		for (Double value : data) 
			output.add((double)list.lastIndexOf(value)/(double)list.size());
		
		return output;
	}
}
