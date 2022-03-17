package pt.fcul.masters.vgp.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.ISeq;
import lombok.Data;
import lombok.extern.java.Log;
import pt.fcul.master.market.MarketAction;
import pt.fcul.master.market.MarketSimulator;
import pt.fcul.master.market.Transaction;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.vgp.util.Vector;

@Data
@Log
public class ProfitSeekingVGP implements GpProblem<Vector> {


	private Table<Vector> table;
	private ISeq<Op<Vector>> terminals;
	private ISeq<Op<Vector>> operations;
	private int depth;
	private Predicate<? super ProgramChromosome<Vector>> validator;

	private boolean compoundMode;

	// this is only here so I do'nt need to call this code over and over again
	private List<Vector> closeColumn;

	private static final double INITIAL_INVESTMENT = 10_000;
	private static final double TRANSACTION_FEE = 0.00;
	private static final double LEVERAGE = 1;
	private static final double PENALIZE = 0.1;





	/**
	 * 
	 * @param table table with the data
	 * @param terminals data
	 * @param operations operations
	 * @param depth initial tree depth
	 * @param validator 
	 * @param compoundMode
	 */
	public ProfitSeekingVGP(Table<Vector> table, 
			ISeq<Op<Vector>> terminals, 
			ISeq<Op<Vector>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<Vector>> validator,
					boolean compoundMode) {
		this.table = table;
		this.terminals = terminals;
		this.operations = operations;
		this.depth = depth;
		this.validator = validator;
		this.compoundMode = compoundMode;
		this.closeColumn = table.getColumn("close");
		log.info("Iniciatized problem");

	}


	@Override
	public Function<Tree<Op<Vector>, ?> , Double> fitness() {
		//return (agent) -> this.simulateMarket(agent, true, null);
		return (agent) -> this.simulateMarketWithSimulator(agent, true, null);
	}

	@Override
	public Codec<Tree<Op<Vector>, ?> , ProgramGene<Vector>> codec() {
		return  Codec.of(
				Genotype.of(
						ProgramChromosome.of(
								depth,
								validator,
								operations,
								terminals
								)
						),
				Genotype::gene);
	}

	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<Vector>, ?>  agent, boolean useTrainSet) {
		//		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		//		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
		//				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
		//				ValidationMetric.MONEY, new LinkedList<>(),
		//				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		//		
		//		double fitness = simulateMarket(agent, useTrainSet, (agentOutput, money)->{
		//			output.get(ValidationMetric.AGENT_OUTPUT).add((double)agentOutput.asMeanScalar());
		//			output.get(ValidationMetric.MONEY).add(money-INITIAL_INVESTMENT);
		//			output.get(ValidationMetric.CONFIDENCE).add((double)agentOutput.asMeanScalar());
		//		});
		//		output.get(ValidationMetric.FITNESS).add(fitness);

		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.PRICE, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.TRANSACTION, new LinkedList<>()));

		MarketSimulator<Vector> ms = MarketSimulator.<Vector>builder(table).penalizerRate(0.1).build();
		double money = ms.simulateMarket((args) -> 
		MarketAction.asSignal(Program.eval(agent, args).asMeanScalar()), useTrainSet, 
		market -> {
			output.get(ValidationMetric.MONEY).add(market.getCurrentMoney());
			output.get(ValidationMetric.PRICE).add(market.getCurrentPrice());
			Transaction currentTransaction = market.getCurrentTransaction();
			output.get(ValidationMetric.TRANSACTION).add(currentTransaction == null || currentTransaction.isClose() ? 0D : currentTransaction.getType() == MarketAction.BUY ? 1D : -1D);
		});
		output.get(ValidationMetric.FITNESS).add(money);
		return output;
	}

	@Override
	public ISeq<Op<Vector>> operations() {
		return this.operations;
	}

	@Override
	public ISeq<Op<Vector>> terminals() {
		return this.terminals;
	}

	@Override
	public Table<Vector> getTable() {
		return this.table;
	}

	@Override
	public EngineConfiguration getConf() {
		return new EngineConfiguration();
	}


	public Double simulateMarketWithSimulator(Tree<Op<Vector>, ?>  agent, boolean useTrainData,Consumer<MarketSimulator<Vector>> interceptor) {
		MarketSimulator<Vector> ms = MarketSimulator.<Vector>builder(table).penalizerRate(0.1).build();
		double money = ms.simulateMarket((args) -> MarketAction.asSignal(Program.eval(agent, args).asMeanScalar()), useTrainData, interceptor);
		return money;
	}


	public Double simulateMarket(Tree<Op<Vector>, ?>  agent, boolean useTrainData, BiConsumer<Vector, Double> interceptor) {
		Pair<Integer, Integer> data = useTrainData ? table.randomTrainSet((int)(table.getTrainSet().value() * 0.05)) : table.getValidationSet();

		List<Double> gainsPercentage = new LinkedList<>();
		List<Double> lossesPrecentage = new LinkedList<>();
		List<Double> gains = new LinkedList<>();
		List<Double> losses  = new LinkedList<>();

		double money = INITIAL_INVESTMENT;
		MarketAction currentMarketAction = MarketAction.NOOP;
		double timewithoutaction = 0;
		double shares = 0;
		Vector intention = Vector.empty();

		for(int i = data.key() ; i < data.value() ; i ++) {
			List<Vector> row = getTable().getRow(i);
			double currentPrice = row.get(table.columnIndexOf("close")).last();

			intention = Program.eval(agent, row.toArray(new Vector[row.size()]));
			MarketAction action = MarketAction.asSignal(intention.asMeanScalar());

			if(action != currentMarketAction && action != MarketAction.NOOP) {

				if(currentMarketAction != MarketAction.NOOP) { //means that there is a transaction to close
					double capitalization = capitalize(money, currentMarketAction, shares, currentPrice,timewithoutaction);
					money = closeTrasanction(gainsPercentage, lossesPrecentage, gains, losses, money, capitalization);
				}	

				currentMarketAction = action;
				timewithoutaction = 0;
				shares = (compoundMode ? money : INITIAL_INVESTMENT) * LEVERAGE / currentPrice;
			}else 
				timewithoutaction++;


			if(interceptor != null)
				interceptor.accept(intention, money);
		}

		double lastPrice = table.getRow(data.value()-1).get(table.columnIndexOf("close")).last(); 

		double capitalization = capitalize(money, currentMarketAction, shares, lastPrice,timewithoutaction);
		money = closeTrasanction(gainsPercentage, lossesPrecentage, gains, losses, money, capitalization);

		if(interceptor != null)
			interceptor.accept(intention, money);

		return profitFactor(gains, losses);
	}


	private double closeTrasanction(List<Double> gainsPercentage, List<Double> lossesPrecentage, List<Double> gains,List<Double> losses, double money, double capitalization) {

		if(compoundMode) {
			if(capitalization - money  > 0) {
				gains.add(capitalization - money);
				gainsPercentage.add((capitalization/money)-1);
			}else {
				losses.add(money - capitalization);
				lossesPrecentage.add((capitalization/money)-1);
			}

			return capitalization;
		}else {

			if(capitalization - INITIAL_INVESTMENT  > 0) {
				gains.add(capitalization - INITIAL_INVESTMENT);
				gainsPercentage.add((capitalization/INITIAL_INVESTMENT)-1);
			}else {
				losses.add(INITIAL_INVESTMENT - capitalization);
				lossesPrecentage.add((capitalization/INITIAL_INVESTMENT)-1);
			}

			return money + capitalization - INITIAL_INVESTMENT;
		}
	}


	private double capitalize(double money, MarketAction currentMarketAction, double shares, double currentPrice, double timewithoutaction) {
		if(currentMarketAction == MarketAction.BUY) {
			return shares * currentPrice * (1D - TRANSACTION_FEE) - (timewithoutaction * PENALIZE);
		}else if (currentMarketAction == MarketAction.SELL) {
			return 2 * (compoundMode ? money : INITIAL_INVESTMENT) - shares * currentPrice * (1D - TRANSACTION_FEE) - (timewithoutaction * PENALIZE);
		}
		return money - (timewithoutaction * PENALIZE);
	}


	/**
	 * 
	 * https://www.axi.com/int/blog/education/measure-your-trading-performance
	 * 
	 * 3. Average win size vs average loss size
	 * 
	 * A ratio of the average profit per trade compared to the average loss per trade. For example, 
	 * if a trader can expect a profit of $1000 per trade and a loss of $500 when he is wrong, 
	 * the profit/loss ratio will be 2:1 ($1000 / $500).
	 * 
	 * 
	 * if used with the raw values values it gives the absolute profit ratio
	 * if used with the percentages values it gives the relative profit ratio
	 * 
	 * @param gains
	 * @return
	 */
	public double profitRatio(List<Double> gains,List<Double> losses) {
		OptionalDouble averageGains = gains.stream().mapToDouble(Double::doubleValue).average();
		OptionalDouble averageLosses = losses.stream().mapToDouble(Double::doubleValue).average();

		double meanGains = averageGains.isPresent() ? averageGains.getAsDouble() : 1;
		double meanLosses = averageLosses.isPresent() ? averageLosses.getAsDouble() : 1;

		return meanGains/meanLosses;
	}


	public double profitFactor(List<Double> gains, List<Double> losses) {
		double lossesSum = losses.stream().mapToDouble(Double::doubleValue).sum();
		return gains.stream().mapToDouble(Double::doubleValue).sum()/(lossesSum == 0 ? 1 : lossesSum);
	}



	public double sharpeRatio(double money,Pair<Integer,Integer> interval) {
		List<Vector> vectors = closeColumn.subList(interval.key(), interval.value());
		List<Double> points = new ArrayList<>();

		double sum = 0;
		for (int i = 0; i < vectors.size(); i++) {
			double close = vectors.get(i).last();
			points.add(close);
			sum += close;
		}

		double mean = sum / (double)vectors.size();

		double varianceSum = 0;
		for (int i = 0; i < points.size(); i++) {
			varianceSum += (points.get(i) - mean) * (points.get(i) - mean);
		}
		double variance = Math.sqrt(varianceSum/(double)points.size());

		return (money - INITIAL_INVESTMENT) / variance;
	}
}
