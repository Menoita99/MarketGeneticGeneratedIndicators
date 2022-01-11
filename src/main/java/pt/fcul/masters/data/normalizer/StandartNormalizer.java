package pt.fcul.masters.data.normalizer;

import java.util.List;

public class StandartNormalizer implements Normalizer{

	@Override
	public List<Double> apply(List<Double> data) {
		double tmpMax = Double.NEGATIVE_INFINITY;
		double tmpmin = Double.POSITIVE_INFINITY;
		for (Double value : data) {
			if(value> tmpMax) tmpMax = value;
			if(value< tmpmin) tmpmin = value;
		}
		final double max = tmpMax;
		final double min = tmpmin;
		return data.stream().map(d -> normalize(d, min, max)).toList();
	}
}
