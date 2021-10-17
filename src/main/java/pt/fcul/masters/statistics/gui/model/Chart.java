package pt.fcul.masters.statistics.gui.model;

import java.util.List;

public interface Chart<X, Y> {

	List<Serie<X,Y>> getSeries();
	
	ChartType getType();
	
	String getName();
	
	String xAxisType();
}
