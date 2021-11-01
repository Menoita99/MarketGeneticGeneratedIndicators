package pt.fcul.masters.statistics.gui.model;

public enum ChartType {
	LINE("line"),
	BAR("bar");
	
	private String jsTypeString;

	private ChartType(String type){
		this.jsTypeString = type;
	}
	
	public String getJsTypeString() {
		return jsTypeString;
	}
}
