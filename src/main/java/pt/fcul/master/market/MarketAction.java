package pt.fcul.master.market;

public enum MarketAction {
	BUY,SELL,NOOP;
	
	public static MarketAction asSignal(double agentOutput) {
		if(Double.isNaN(agentOutput))
			return NOOP;
		return agentOutput > 0 ? BUY : agentOutput < 0 ? SELL : NOOP;
	}
}
