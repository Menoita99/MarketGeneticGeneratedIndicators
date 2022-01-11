package pt.fcul.masters.data.normalizer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.plotter.gui.Plotter;

import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.memory.MemoryManager;

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

	
	

	public static void main(String[] args) {
		StepNormalizer dn = new StepNormalizer();
		MemoryManager memory = new MemoryManager(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2020, 6, 1, 0, 0));
		
		List<Double> column = new ArrayList<>();
		memory.foreach(row -> column.add(row.get(memory.columnIndexOf("close"))));
		
		List<Double> normalizedcolumn = dn.apply(column);
		memory.addColumn(normalizedcolumn,"normalizedClose");
		
		memory.toCsv("C:\\Users\\Owner\\Desktop\\Normalization\\StepNormalizer\\stepnormalization.csv");
		Plotter.builder().lineChart(column, "close").build().plot();
		Plotter.builder().lineChart(normalizedcolumn, "normalizedclose").build().plot();
	}
}
