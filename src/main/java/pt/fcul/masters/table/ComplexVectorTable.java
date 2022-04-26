package pt.fcul.masters.table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.complex.Complex;

import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.db.CandlestickFetcher;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.vgp.util.ComplexVector;

public class ComplexVectorTable extends Table<ComplexVector> {

	private int vectorSize;
	private Normalizer normalizer;

	private ComplexVectorTable() {super();}

	public ComplexVectorTable(int vectorSize) {
		super();
		this.vectorSize = vectorSize;
		fetch();
	}

	public ComplexVectorTable(Market market, TimeFrame timeframe,int vectorSize) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.vectorSize = vectorSize;
		fetch();
	}



	public ComplexVectorTable(Market market, TimeFrame timeframe, LocalDateTime datetime,int vectorSize) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		this.vectorSize = vectorSize;
		fetch();
	}



	public ComplexVectorTable(Market market, TimeFrame timeframe, LocalDateTime datetime,int vectorSize, Normalizer normalizer) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = datetime;
		this.vectorSize = vectorSize;
		this.normalizer = normalizer;
		fetch();
	}


	public ComplexVectorTable(Market market, TimeFrame timeframe, LocalDateTime from,LocalDateTime to,int vectorSize, Normalizer normalizer) {
		super();
		this.market = market;
		this.timeframe = timeframe;
		this.datetime = from;
		this.to = to;
		this.vectorSize = vectorSize;
		this.normalizer = normalizer;
		fetch();
	}

	public static ComplexVectorTable fromCsv(Path csvPath) throws IOException {
		List<String> lines = Files.readAllLines(csvPath);
		ComplexVectorTable table = new ComplexVectorTable();

		List<String> columns = new LinkedList<>();
		Collections.addAll(columns, lines.get(0).split(","));
		table.setColumns(columns);

		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);

			String[] row = line.split("\",\"");
			ComplexVector[] vecRow = new ComplexVector[row.length];
			for (int k = 0; k < row.length; k++){
				String vector = row[k];
				String[] elements = vector.replaceAll("[^0-9\\.\\,]", "").split(",");

				double[] v = new double[elements.length];
				for (int j = 0; j < elements.length; j++) 
					v[j] = Float.parseFloat(elements[j]);

				vecRow[k] = ComplexVector.of(v);
			}
			table.addRow(vecRow);
		}		

		table.calculateSplitPoint();

		return table;
	}


	private void fetch() {
		columns = new ArrayList<>(List.of("open", "high", "low", "close", "volume"));
		if(normalizer != null)
			columns.addAll(List.of("openNorm", "highNorm", "lowNorm", "closeNorm", "volumeNorm"));

		List<Candlestick> candles;
		if(to == null)
			candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetime(market, timeframe, datetime);
		else
			candles = CandlestickFetcher.findAllByMarketTimeframeAfterDatetimeAndBefore(market, timeframe, datetime,to);
		List<Candlestick> candlesNorm = normalizer != null ? normalize(candles) : new LinkedList<>();

		for(int i = vectorSize; i < candles.size(); i++) {
			double[] open = new double[vectorSize];
			double[] high = new double[vectorSize];
			double[] low = new double[vectorSize];
			double[] close = new double[vectorSize];
			double[] volume = new double[vectorSize];

			double[] openNorm = new double[vectorSize];
			double[] highNorm = new double[vectorSize];
			double[] lowNorm = new double[vectorSize];
			double[] closeNorm = new double[vectorSize];
			double[] volumeNorm = new double[vectorSize];

			for (int j = i - vectorSize; j < i; j++) {
				open[j - (i - vectorSize)] = (double)candles.get(j).getOpen();
				high[j - (i - vectorSize)] = (double)candles.get(j).getHigh();
				low[j - (i - vectorSize)] = (double)candles.get(j).getLow();
				close[j - (i - vectorSize)] = (double)candles.get(j).getClose();
				volume[j - (i - vectorSize)] = (double)candles.get(j).getVolume();

				if(normalizer != null) {
					openNorm[j - (i - vectorSize)] = (double)candlesNorm.get(j).getOpen();
					highNorm[j - (i - vectorSize)] = (double)candlesNorm.get(j).getHigh();
					lowNorm[j - (i - vectorSize)] = (double)candlesNorm.get(j).getLow();
					closeNorm[j - (i - vectorSize)] = (double)candlesNorm.get(j).getClose();
					volumeNorm[j - (i - vectorSize)] = (double)candlesNorm.get(j).getVolume();
				}
			}
			
			if(i % 1000 == 0)
				System.out.println("Completed; "+ i + " of " + candles.size());

			if(normalizer != null)
				addRow(ComplexVector.of(open),ComplexVector.of(high),ComplexVector.of(low),ComplexVector.of(close),ComplexVector.of(volume),
						ComplexVector.of(openNorm),ComplexVector.of(highNorm),ComplexVector.of(lowNorm),ComplexVector.of(closeNorm),ComplexVector.of(volumeNorm));
			else
				addRow(ComplexVector.of(open),ComplexVector.of(high),ComplexVector.of(low),ComplexVector.of(close),ComplexVector.of(volume));
		}

		calculateSplitPoint();

		System.out.println("Train set is from "+candles.get(trainSet.key()).getDatetime() + " " + candles.get(trainSet.value()-1).getDatetime() + " " + (trainSet.value() - trainSet.key()));
		System.out.println("Validation set is from "+candles.get(validationSet.key()).getDatetime() + " " + candles.get(validationSet.value()-1).getDatetime() + " " + (validationSet.value() - validationSet.key()));
	}

	private List<Candlestick> normalize(List<Candlestick> candles) {
		List<Double> close = new LinkedList<>();
		List<Double> open = new LinkedList<>();
		List<Double> low = new LinkedList<>();
		List<Double> high = new LinkedList<>();
		List<Double> vol = new LinkedList<>();

		for (int i = 0; i < candles.size(); i++) {
			Candlestick c = candles.get(i); 
			close.add(c.getClose());
			open.add(c.getOpen());
			low.add(c.getLow());
			high.add(c.getHigh());
			vol.add(c.getVolume());
		}

		close = normalizer.apply(close);
		open = normalizer.apply(open);
		low = normalizer.apply(low);
		high = normalizer.apply(high);
		vol = normalizer.apply(vol);

		List<Candlestick> candlesNorm = new LinkedList<>();

		for (int i = 0; i < candles.size(); i++)
			candlesNorm.add(Candlestick.builder()
					.close(close.get(i))
					.open(open.get(i))
					.high(high.get(i))
					.low(low.get(i))
					.volume(vol.get(i))
					.datetime(candles.get(i).getDatetime())
					.build());

		return candlesNorm;
	}
	

	public void toCsv(String path) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.println(columns.stream().collect(Collectors.joining(",")));
			hBuffer.stream().forEach(row -> 
				pw.println("\""+row.stream().map(data-> {
					
					StringBuilder sb = new StringBuilder();
					Complex[] arr = data.getArr();
					for(int i = 0; i < arr.length; i++)
						sb.append(arr[i].getReal()+"").append((i+1) >= arr.length ? "" : ",");
						
					return sb.toString();
				
				}).collect(Collectors.joining("\",\""))+"\""));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}