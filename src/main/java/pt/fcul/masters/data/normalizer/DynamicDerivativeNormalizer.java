package pt.fcul.masters.data.normalizer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.plotter.gui.Plotter;

import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.table.DoubleTable;

public class DynamicDerivativeNormalizer implements Normalizer {

	private DerivativeNormalizer normalizer = new DerivativeNormalizer();
	private int period;
	
	
	public DynamicDerivativeNormalizer(int period) {
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
	
	

	


	public static void main(String[] args) {
		DynamicDerivativeNormalizer dn = new DynamicDerivativeNormalizer(1200);
		DoubleTable memory = new DoubleTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2020, 6, 1, 0, 0));
		
		List<Double> column = new ArrayList<>();
		memory.foreach(row -> column.add(row.get(memory.columnIndexOf("close"))));
		
		List<Double> normalizedcolumn = dn.apply(column);
		memory.addColumn(normalizedcolumn,"normalizedClose");
		
		memory.toCsv("C:\\Users\\Owner\\Desktop\\Normalization\\DynamicDerivativeNormalizer\\dynamicDerivativeNormalizer.csv");
		Plotter.builder().lineChart(column, "close").build().plot();
		Plotter.builder().lineChart(normalizedcolumn, "normalizedclose").build().plot();
	}

}
