package pt.fcul.masters.market;

public enum MarketAction {
	SELL,NOOP,BUY;
	
	public static MarketAction asSignal(double agentOutput) {
		if(Double.isNaN(agentOutput) || (agentOutput >= -0.5 && agentOutput <= 0.5)) 
			return NOOP;
		return agentOutput > 0 ? BUY : agentOutput < 0 ? SELL : NOOP;
	}

	public static MarketAction ensemble(boolean buy, boolean sell) {
		if(buy == sell)
			return NOOP;
		return buy ? MarketAction.BUY : MarketAction.SELL;
	}
}
