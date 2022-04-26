package pt.fcul.masters.db.model;

import java.util.List;

public enum Market {
	
	EUR_USD,
	USD_JPY,
	GBP_USD,
	USD_CAD,
	AUD_USD,
	
	SPCE,
	AMZN,
	BABA,
	SNE,
	MSFT,
	NKE,
	FB,
	GM,
	UBER,
	JNJ,
	WMT,
	DIS,
	NFLX,
	NVDA,
	AAPL,
	ZM,
	AMD,
	BA,
	TSLA,
	FIT,
	TWTR, 
	SBUX;
	
	
	public static List<Market> getForexPairs(){
		return List.of(	
				EUR_USD,
				USD_JPY,
				GBP_USD,
				USD_CAD, 
				AUD_USD);
	}
	
	public static List<Market> getStocks(){
		return List.of(SPCE,
				AMZN,
				BABA,
				SNE,
				MSFT,
				NKE,
				FB,
				GM,
				UBER,
				JNJ,
				WMT,
				DIS,
				NFLX,
				NVDA,
				AAPL,
				ZM,
				AMD,
				BA,
				TSLA,
				FIT,
				TWTR);
	}
}
