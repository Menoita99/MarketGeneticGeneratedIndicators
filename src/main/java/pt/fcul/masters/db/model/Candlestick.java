package pt.fcul.masters.db.model;

import java.time.LocalDateTime;

import io.jenetics.facilejdbc.RowParser;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Candlestick {
	
	private long id;
	private double high;
	private double close;
	private double open;
	private double low;
	private double volume;
	private LocalDateTime datetime;
	
	
	public static RowParser<Candlestick> getQueryParser(){
		return (row, conn) -> Candlestick.builder()
			    .id(row.getLong("id"))
			    .high(row.getDouble("high"))
			    .close(row.getDouble("close"))
			    .open(row.getDouble("open"))
			    .low(row.getDouble("low"))
			    .volume(row.getDouble("volume"))
			    .datetime(LocalDateTime.parse(row.getString("datetime").replaceAll(" ", "T")))
			    .build();
	}
}
