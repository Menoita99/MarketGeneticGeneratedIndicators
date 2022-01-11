package pt.fcul.masters.data.normalizer;

import java.util.LinkedList;
import java.util.List;

public class DynamicStepNormalizer implements Normalizer{

	private int period;
	private StepNormalizer normalizer = new StepNormalizer();
	

	public DynamicStepNormalizer(int period) {
		this.period = period;
	}


	@Override
	public List<Double> apply(List<Double> data) {
		List<Double> normalized =  new LinkedList<>();

		normalized.addAll(normalizer.apply(data.subList(0, Math.min(period, data.size()))));

		for (int i = period; i < data.size(); i++) {
			List<Double> list = normalizer.apply(data.subList(i-period+1, i+1));
			normalized.add(list.get(list.size()-1));
		}
		
		return normalized;
	}
}
