package pt.fcul.masters.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MarketAction {
	SELL(-1),NOOP(0),BUY(1);
	
	private int value;
	
	public static MarketAction asSignal(double agentOutput) {
		if(Double.isNaN(agentOutput) || (agentOutput > -1 && agentOutput < 1)) 
			return NOOP;
		return agentOutput > 0 ? BUY : agentOutput < 0 ? SELL : NOOP;
	}

	public static MarketAction ensemble(boolean buy, boolean sell) {
		if(buy == sell)
			return NOOP;
		return buy ? MarketAction.BUY : MarketAction.SELL;
	}
}
