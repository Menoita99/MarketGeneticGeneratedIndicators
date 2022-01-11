package pt.fcul.masters.data.normalizer;

import java.util.List;

public interface Normalizer {
	
	
	List<Double> apply(List<Double> data);
	
	
	default double normalize(double value, double min, double max) {
		return (value - min)/(max - min);
	}
	

//	public static void main(String[] args) {
//		StepNormalizer dn = new StepNormalizer();
//		MemoryManager memory = new MemoryManager(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2020, 6, 1, 0, 0));
//		
//		List<Double> column = new ArrayList<>();
//		memory.foreach(row -> column.add(row.get(memory.columnIndexOf("close"))));
//		
//		List<Double> normalizedcolumn = dn.apply(column);
//		memory.addColumn(normalizedcolumn,"normalizedclose");
//		
//		memory.toCsv("C:\\Users\\Owner\\Desktop\\Normalization\\DerivativeNormalizer\\derivativenormalization.csv");
//		Plotter.builder().lineChart(column, "volume").build().plot();
//		Plotter.builder().lineChart(normalizedcolumn, "normalizedclose").build().plot();
//	}
}
