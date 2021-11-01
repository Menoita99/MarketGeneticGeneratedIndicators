package pt.fcul.masters.statistics.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import netscape.javascript.JSObject;
import pt.fcul.masters.statistics.gui.bridges.DownloadBridge;
import pt.fcul.masters.statistics.gui.bridges.LogBridge;
import pt.fcul.masters.statistics.gui.model.BarChart;
import pt.fcul.masters.statistics.gui.model.Chart;
import pt.fcul.masters.statistics.gui.model.LineChart;
import pt.fcul.masters.statistics.gui.model.Serie;

@Data
@Builder
public class Plotter {

	private static final String CHART_PAGE = "/META-INF/plot/chart.html";
	@Builder.Default
	private String title = "Plotter - JavaFx";
	@Singular("chart")
	private List<Chart<?,?>> charts;





	public void plot() {
		try {
			Platform.startup(() -> showWindow());
		}catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(() -> showWindow());
		}
	}




	private void showWindow() {
		WebView browser = new WebView();
		WebEngine webEngine = browser.getEngine();
		webEngine.load(this.getClass().getResource(CHART_PAGE).toExternalForm());
		webEngine.setJavaScriptEnabled(true);

		DownloadBridge downloadBridge = new DownloadBridge(browser);

		webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				JSObject window = (JSObject) webEngine.executeScript("window");
				window.setMember("java", new LogBridge());
				window.setMember("downloadBridge", downloadBridge);
				webEngine.executeScript("console.log = function(message) { java.log(message); };");
				getChartScripts().forEach(webEngine::executeScript);
			}
		});

		buildScene(browser, downloadBridge);
	}





	private void buildScene(WebView browser, DownloadBridge downloadBridge) {
		MenuItem htmlMenuItem = new MenuItem("Download as HTML");
		htmlMenuItem.setOnAction(e -> {
			try {
				downloadBridge.dowloadAsHtml();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		MenuItem pngMenuItem = new MenuItem("Download as PNG");
		pngMenuItem.setOnAction(e ->{
			try {
				downloadBridge.dowloadAsPng();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		Menu menu = new Menu("File");
		menu.getItems().addAll(htmlMenuItem,pngMenuItem);

		MenuBar menuBar = new MenuBar(menu);
		VBox pane = new VBox();
		pane.getChildren().addAll(menuBar,browser);
		VBox.setVgrow(browser, Priority.ALWAYS);

		Stage stage = new Stage();
		stage.setTitle(title); 
		stage.setScene(new Scene(pane));
		stage.show();
	}





	private List<String> getChartScripts(){
		return charts.stream().map(chart -> {
			String sdata = toJSONArray(chart).toString().replaceAll("\"\\{", "{").replaceAll("\\}\"", "}");
			String script = "renderChart"+"('"+chart.getType().getJsTypeString()+"','"+chart.getName()+"','"+chart.xAxisType()+"',JSON.parse('"+sdata+"'));";
			System.out.println(script);
			return script;
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
			this.charts.add(new LineChart<>(List.of(new Serie<>(data)), title));
			return this;
		}

		public PlotterBuilder lineChart(Serie<?,?> serie,String title) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new LineChart<>(List.of(serie), title));
			return this;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public PlotterBuilder lineChart(String title, Serie<?,?>... serie) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new LineChart(Arrays.asList(serie), title));
			return this;
		}
		

		public PlotterBuilder barChart(Map<?,?> data,String title) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new BarChart<>(List.of(new Serie<>(data)), title));
			return this;
		}

		public PlotterBuilder barChart(Serie<?,?> serie,String title) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new BarChart<>(List.of(serie), title));
			return this;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public PlotterBuilder barChart(String title, Serie<?,?>... serie) {
			if(this.charts == null)
				this.charts = new ArrayList<>();
			this.charts.add(new BarChart(Arrays.asList(serie), title));
			return this;
		}
	}
}
