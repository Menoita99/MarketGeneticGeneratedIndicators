package pt.fcul.masters.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.RowParser;
import lombok.Data;
import pt.fcul.masters.db.model.Candlestick;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;

@Data
public class CandlestickFetcher {

	private static CandlestickFetcher instance = null;
	private Properties properties = new Properties();
	private HikariConfig config = new HikariConfig();
	private HikariDataSource ds;

	private CandlestickFetcher() {
		try {
			properties.load(new FileInputStream(new File("src/main/resources/application.properties")));

			config.setJdbcUrl(properties.getProperty("db.url"));
			config.setUsername(properties.getProperty("db.username"));
			config.setPassword(properties.getProperty("db.password"));
			config.setDriverClassName(properties.getProperty("db.driver"));
			//TODO put this in properties
			config.addDataSourceProperty( "cachePrepStmts" , "true" );
			config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
			config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

			ds = new HikariDataSource( config );
		} catch (IOException e) {
			e.printStackTrace();
		}
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
							where market = :market and timeframe = :timeframe;
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
							where market = :market and timeframe = :timeframe and datetime > :datetime;
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
}
