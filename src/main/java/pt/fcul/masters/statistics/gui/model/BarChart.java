package pt.fcul.masters.statistics.gui.model;

import java.util.List;

public class BarChart<X, Y>extends BasicChart<X, Y>{

	public BarChart(List<Serie<X, Y>> series, String name) {
		super(series, name);
	}
	
	public BarChart(String name) {
		super(name);
	}
	
	@Override
	public ChartType getType() {
		return ChartType.BAR;
	}
}
