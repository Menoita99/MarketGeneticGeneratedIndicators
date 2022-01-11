package pt.fcul.masters.data.normalizer;

import java.util.List;

public interface Normalizer {
	
	
	List<Double> apply(List<Double> data);
	
	
	default double normalize(double value, double min, double max) {
		return (value - min)/(max - min);
	}
}
