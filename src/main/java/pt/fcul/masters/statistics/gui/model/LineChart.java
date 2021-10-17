package pt.fcul.masters.statistics.gui.model;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class LineChart<X, Y> implements Chart<X, Y>{

	private List<Serie<X, Y>> series = new LinkedList<>();
	private String name;
	
	public LineChart(List<Serie<X, Y>> series, String name) {
		this.series = series;
		this.name = name;
	}
	
	public LineChart(String name) {
		this.name = name;
	}

	@Override
	public List<Serie<X, Y>> getSeries() {
		return series;
	}

	@Override
	public ChartType getType() {
		return ChartType.LINE;
	}

	@Override
	public String xAxisType() {
		if(!series.isEmpty() && !series.get(0).getData().isEmpty()) {
			X x = series.get(0).getData().get(0).x();
			if(x instanceof String) return "category";
			if(x instanceof Number) return "numeric";
			if(x instanceof LocalDateTime) return "datetime";
		}
		return "category";
	}
}
