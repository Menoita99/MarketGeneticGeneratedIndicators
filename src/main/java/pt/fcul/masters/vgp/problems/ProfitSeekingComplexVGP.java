package pt.fcul.masters.vgp.problems;

import static pt.fcul.masters.logger.ValidationMetric.FITNESS;
import static pt.fcul.masters.logger.ValidationMetric.MONEY;
import static pt.fcul.masters.logger.ValidationMetric.NORMALIZATION_CLOSE;
import static pt.fcul.masters.logger.ValidationMetric.OPEN_TRADES;
import static pt.fcul.masters.logger.ValidationMetric.PRICE;
import static pt.fcul.masters.logger.ValidationMetric.ROI;
import static pt.fcul.masters.logger.ValidationMetric.TRADED_TICKS;
import static pt.fcul.masters.logger.ValidationMetric.TRANSACTION;
import static pt.fcul.masters.logger.ValidationMetric.WIN_RATE;
import static pt.fcul.masters.utils.Constants.GENERATION;
import static pt.fcul.masters.utils.Constants.RAND;
import static pt.fcul.masters.utils.Constants.TRAIN_SLICES;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.market.MarketSimulator.MarketSimulatorBuilder;
import pt.fcul.masters.market.Transaction;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Slicer;
import pt.fcul.masters.vgp.util.ComplexVector;

@Data
@Log
public class ProfitSeekingComplexVGP  implements GpProblem<ComplexVector> {



	private Table<ComplexVector> table;
	private ISeq<Op<ComplexVector>> terminals;
	private ISeq<Op<ComplexVector>> operations;
	private int depth;
	private Predicate<? super ProgramChromosome<ComplexVector>> validator;


	// this is only here so I don't need to call this code over and over again
	private MarketSimulatorBuilder<ComplexVector> market;

	private Map<Integer,Integer> generationSlices = new ConcurrentHashMap<>();
	
	
	/**
	 * 
	 * @param table table with the data
	 * @param terminals data
	 * @param operations operations
	 * @param depth initial tree depth
	 * @param validator 
	 * @param compoundMode
	 */
	public ProfitSeekingComplexVGP(Table<ComplexVector> table, 
			ISeq<Op<ComplexVector>> terminals, 
			ISeq<Op<ComplexVector>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<ComplexVector>> validator,
			boolean compoundMode) {
		
		this.table = table;
		this.terminals = terminals;
		this.operations = operations;
		this.depth = depth;
		this.validator = validator;
		
		this.table.setTrainValidationRatio(0.5);
		this.table.calculateSplitPoint();
		
		this.market = MarketSimulator.<ComplexVector>builder(table)
				.penalizerRate(0.1)
				.compoundMode(compoundMode)
				.stoploss(0.05);
//				.takeprofit(0.1)

		log.info("Iniciatized problem");
	}


	
	
	@Override
	public Function<Tree<Op<ComplexVector>, ?> , Double> fitness() {
		return (agent) -> this.simulateMarketWithSimulator(agent, true, null);
	}


	
	
	public Double simulateMarketWithSimulator(Tree<Op<ComplexVector>, ?>  agent, boolean useTrainData,Consumer<MarketSimulator<ComplexVector>> interceptor) {
		int gen = GENERATION.get();

		generationSlices.computeIfAbsent(gen, g -> {
			int slice = RAND.nextInt(TRAIN_SLICES);
			System.out.println(g+" "+slice+" "+ Slicer.getSlice(table.getTrainSet(), TRAIN_SLICES, slice));
			return slice;
		});
		
		if(gen % 50 == 0)
			market.trainSlice(table.getTrainSet());//use all data
		else
			market.trainSlice(Slicer.getSlice(table.getTrainSet(), TRAIN_SLICES, generationSlices.get(gen)));
			
		MarketSimulator<ComplexVector> ms = market.build();
		return ms.simulateMarket((args) -> MarketAction.asSignal(Program.eval(agent, args).realMean()), useTrainData, interceptor);
	}


	
	
	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<ComplexVector>, ?>  agent, boolean useTrainSet) {

		Map<ValidationMetric,List<Double>> output = new EnumMap<>(ValidationMetric.class);
		output.putAll(Map.of(FITNESS, new LinkedList<>(),
				PRICE, new LinkedList<>(),
				MONEY, new LinkedList<>(),
				TRANSACTION, new LinkedList<>(),
				ROI, new LinkedList<>(),
				OPEN_TRADES, new LinkedList<>(),
				WIN_RATE, new LinkedList<>(),
				TRADED_TICKS, new LinkedList<>(List.of(0D)),
				NORMALIZATION_CLOSE, new LinkedList<>()));

		MarketSimulator<ComplexVector> ms = market.trainSlice(table.getTrainSet()).build();
		double trainValidationRatio = table.getTrainValidationRatio();
		table.setTrainValidationRatio(.5);
		table.calculateSplitPoint();
		
		List<Double> transactions = output.get(TRANSACTION);
		double money = ms.simulateMarket((args) -> 
			MarketAction.asSignal(Program.eval(agent, args).realMean()), useTrainSet, market -> {
//					output.get(PROFIT_PERCENTAGE).add(market.getCurrentRow().get(market.getCurrentRow().size()-1).last());
				    output.get(ValidationMetric.NORMALIZATION_CLOSE).add(market.getCurrentRow().get(market.getTable().columnIndexOf("closeNorm")).last().getReal());
					output.get(MONEY).add(market.getCurrentMoney());
					output.get(PRICE).add(market.getCurrentPrice());
					
					Transaction currentTransaction = market.getCurrentTransaction();
					transactions.add(currentTransaction == null || currentTransaction.isClose() ? 0D : currentTransaction.getType() == MarketAction.BUY ? 1D : -1D);
					
					output.put(OPEN_TRADES, List.of((double)market.getTransactions().size()));
					output.put(WIN_RATE, List.of(market.winRate()));
					output.put(TRADED_TICKS, List.of((currentTransaction == null || currentTransaction.isClose() ? output.get(TRADED_TICKS).get(0) + 0D : output.get(TRADED_TICKS).get(0) + 1D)));
				});
		
		output.get(FITNESS).add(money);
		table.setTrainValidationRatio(trainValidationRatio);
		table.calculateSplitPoint();
		
		double finalValueOfInvestment = output.get(MONEY).get(output.get(MONEY).size()-1);
		double initialValueOfInvestement = output.get(MONEY).get(0);
		output.put(ROI, List.of((finalValueOfInvestment - initialValueOfInvestement)/initialValueOfInvestement * 100));
		
		return output;
	}


	
	
	@Override
	public ISeq<Op<ComplexVector>> operations() {
		return this.operations;
	}


	
	
	@Override
	public ISeq<Op<ComplexVector>> terminals() {
		return this.terminals;
	}


	
	
	@Override
	public Table<ComplexVector> getTable() {
		return this.table;
	}


	
	
	@Override
	public Codec<Tree<Op<ComplexVector>, ?> , ProgramGene<ComplexVector>> codec() {
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

}
