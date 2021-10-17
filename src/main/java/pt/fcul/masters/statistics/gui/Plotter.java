package pt.fcul.masters.statistics.gui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import netscape.javascript.JSObject;
import pt.fcul.masters.statistics.gui.bridges.LogBridge;
import pt.fcul.masters.statistics.gui.model.Chart;
import pt.fcul.masters.statistics.gui.model.LineChart;
import pt.fcul.masters.statistics.gui.model.Serie;

@Builder
@Data
public class Plotter {

	private static final String CHART_PAGE = "/META-INF/plot/html/chart.html";
	@Builder.Default
	private String title = "Plotter - JavaFx";
	@Singular("chart")
	private List<Chart<?,?>> charts;


	public void plot() {
		Platform.startup(() -> {
			Stage stage = new Stage();

			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			webEngine.load(this.getClass().getResource(CHART_PAGE).toExternalForm());
			webEngine.setJavaScriptEnabled(true);


			webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					JSObject window = (JSObject) webEngine.executeScript("window");
					window.setMember("java", new LogBridge());
					webEngine.executeScript("console.log = function(message) { java.log(message); };");
					getChartScripts().forEach(webEngine::executeScript);
				}
			});

			Scene scene = new Scene(browser);
			//	stage.setMaximized(true);
			stage.setTitle(title); 
			stage.setScene(scene);
			stage.show();
		});
	}


	private List<String> getChartScripts(){
		return charts.stream().map(chart -> {
			String sdata = toJSONArray(chart).toString();
			sdata = "\"[{"+sdata.subSequence(3, sdata.length()-3)+"}]\"";
			return "render"+chart.getType().getJsTypeString()+"('"+chart.getName()+"','"+chart.xAxisType()+"',JSON.parse("+sdata+"));";
		}).toList();
	}


	private JSONArray toJSONArray(Chart<?, ?> chart) {
		JSONArray jSeries = new JSONArray();
		chart.getSeries().forEach(s -> jSeries.put(s.toJSONObject()));
		return jSeries;
	}


	
	
	public static class PlotterBuilder {

		public PlotterBuilder lineChart(Map<?,?> data,String title) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new LineChart<>(List.of(new Serie<>(data)), "teste"));
			return this;
		}

		public PlotterBuilder lineChart(Serie<?,?> serie,String title) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new LineChart<>(List.of(serie), "teste"));
			return this;
		}
	}
}
