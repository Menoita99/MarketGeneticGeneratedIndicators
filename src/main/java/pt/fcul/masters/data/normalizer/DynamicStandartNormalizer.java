package pt.fcul.masters.data.normalizer;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class DynamicStandartNormalizer implements Normalizer{

	
	private int period;
	private double periodMax = Double.NEGATIVE_INFINITY;
	private double periodMin = Double.POSITIVE_INFINITY;
	

	public DynamicStandartNormalizer(int period) {
		this.period = period;
	}
	
	@Override
	public List<Double> apply(List<Double> data){
		List<Double> normalized =  new LinkedList<>();
		
		setMaxAndMin(data,0,period);
		
		for (int i = 0; i < data.size(); i++) {
			if(i >=period) 
				setMaxAndMin(data,i-period+1,i+1);
			normalized.add(normalize(data.get(i),periodMin,periodMax));
		}
		return normalized;
	}

	private void setMaxAndMin(List<Double> data,int start, int end) {
		periodMax = Double.NEGATIVE_INFINITY;
		periodMin = Double.POSITIVE_INFINITY;
		for (int i = start; i < end && i < data.size(); i++) {
			if(data.get(i) > periodMax) periodMax = data.get(i);
			if(data.get(i) < periodMin) periodMin = data.get(i);
		}
	}
}
