package pt.fcul.masters.statefull.op;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import io.jenetics.prog.op.Op;
import pt.fcul.master.utils.LimitedList;

public class Rsi implements Op<Double> {


	private LimitedList<Double> gains = new LimitedList<>(14);
	private LimitedList<Double> losses = new LimitedList<>(14);
	private double lastPrice;
	
	private final int random = new Random().nextInt(9999);
	
	
	/**
	 * https://www.tradingview.com/support/solutions/43000502338-relative-strength-index-rsi/
	 * https://www.investopedia.com/terms/r/rsi.asp
	 */
	@Override
	public synchronized Double apply(Double[] t) {
		double currentPrice = t[0];
		if(lastPrice <= 0)
			lastPrice = currentPrice;
		double percentage = (BigDecimal.valueOf(currentPrice).divide(BigDecimal.valueOf(lastPrice),MathContext.DECIMAL64).doubleValue() - 1) * 100;
		gains.add(percentage > 0 ? percentage : 0);
		losses.add(percentage > 0 ? 0 : -percentage);
		double avGain = gains.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		double avLosses = losses.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		double rs = avLosses == 0 ? 1 : BigDecimal.valueOf(avGain).divide(BigDecimal.valueOf(avLosses),MathContext.DECIMAL64).doubleValue();
		lastPrice = currentPrice;
		return 100 - (100 / (1 + rs));
	}
	
    @Override
    public Op<Double> get() {
        return new Rsi();
    }

	@Override
	public String name() {
		return String.format("rsi[%s]", random);
	}

	@Override
	public int arity() {
		return 1;
	}

}
