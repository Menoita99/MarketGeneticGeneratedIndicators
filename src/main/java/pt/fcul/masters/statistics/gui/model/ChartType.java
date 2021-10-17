package pt.fcul.masters.statistics.gui.model;

public enum ChartType {
	LINE("LineChart");
	
	private String jsTypeString;

	private ChartType(String type){
		this.jsTypeString = type;
	}
	
	public String getJsTypeString() {
		return jsTypeString;
	}
}
