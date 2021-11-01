package pt.fcul.masters.statistics.gui.model;

import java.util.List;

public class LineChart<X, Y> extends BasicChart<X, Y>{

	public LineChart(List<Serie<X, Y>> series, String name) {
		super(series, name);
	}
	
	public LineChart(String name) {
		super(name);
	}
	
	@Override
	public ChartType getType() {
		return ChartType.LINE;
	}
}
