package pt.fcul.masters.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.RowParser;
import lombok.Data;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.utils.SystemProperties;

@Data
public class CandlestickFetcher {

	private static CandlestickFetcher instance = null;
	private HikariConfig config = new HikariConfig();
	private HikariDataSource ds;

	private CandlestickFetcher() {
			ds = new HikariDataSource( SystemProperties.getDbConfig());
	}

	public static CandlestickFetcher instance() {
		if(instance == null)
			instance = new CandlestickFetcher();
		return instance;
	}


	public static List<Candlestick> findAllByMarketTimeframe(Market market,TimeFrame timeframe){
		try(Connection conn = instance().getDs().getConnection()){
			return Query.of(
					"""
							SELECT id, open, high, low, close, volume, datetime FROM forexdb.candlestick 
							where market = :market and timeframe = :timeframe order by datetime asc;
					""")
					.on(Map.of("market",market.toString(),"timeframe",timeframe.toString()))
					.as(Candlestick.getQueryParser().list(),conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return List.of();
	}



	public static List<Candlestick> findAllByMarketTimeframeAfterDatetime(Market market,TimeFrame timeframe,LocalDateTime datetime){
		try(Connection conn = instance().getDs().getConnection()){
			return Query.of(
					"""
							SELECT id, open, high, low, close, volume, datetime FROM forexdb.candlestick 
							where market = :market and timeframe = :timeframe and datetime > :datetime order by datetime asc ;
					""")
					.on(Map.of("market",market.toString(),"timeframe",timeframe.toString(),"datetime",datetime.toString()))
					.as(Candlestick.getQueryParser().list(),conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return List.of();
	}
	



	public static double getMaxVolumeByMarketTimeframeAfterDatetime(Market market,TimeFrame timeframe,LocalDateTime datetime){
		try(Connection conn = instance().getDs().getConnection()){
			RowParser<Double> resultSetParser = (row, connection) -> row.getDouble(1);
			return Query.of(
					"""
							SELECT max(volume) FROM forexdb.candlestick 
							where market = :market and timeframe = :timeframe and datetime > :datetime;
					""")
					.on(Map.of("market",market.toString(),"timeframe",timeframe.toString(),"datetime",datetime.toString()))
					.as(resultSetParser.single(),conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	public static double getMinVolumeByMarketTimeframeAfterDatetime(Market market,TimeFrame timeframe,LocalDateTime datetime){
		try(Connection conn = instance().getDs().getConnection()){
			RowParser<Double> resultSetParser = (row, connection) -> row.getDouble(1);
			return Query.of(
					"""
							SELECT min(volume) FROM forexdb.candlestick 
							where market = :market and timeframe = :timeframe and datetime > :datetime;
					""")
					.on(Map.of("market",market.toString(),"timeframe",timeframe.toString(),"datetime",datetime.toString()))
					.as(resultSetParser.single(),conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static List<Candlestick> findAllByMarketTimeframeAfterDatetimeAndBefore(Market market, TimeFrame timeframe,LocalDateTime from, LocalDateTime to) {
		try(Connection conn = instance().getDs().getConnection()){
			return Query.of(
					"""
							SELECT id, open, high, low, close, volume, datetime FROM forexdb.candlestick 
							where market = :market and timeframe = :timeframe and datetime > :from  and datetime < :to order by datetime asc ;
					""")
					.on(Map.of("market",market.toString(),"timeframe",timeframe.toString(),"from",from.toString(),"to",to.toString()))
					.as(Candlestick.getQueryParser().list(),conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return List.of();
	}
}
