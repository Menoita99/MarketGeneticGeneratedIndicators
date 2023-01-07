package pt.fcul.masters.stvgp.problems;

import static java.util.Objects.requireNonNull;
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
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.util.ISeq;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.market.MarketSimulator.MarketSimulatorBuilder;
import pt.fcul.masters.market.Transaction;
import pt.fcul.masters.stvgp.StvgpChromosome;
import pt.fcul.masters.stvgp.StvgpGene;
import pt.fcul.masters.stvgp.StvgpProgram;
import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.stvgp.op.StvgpOp;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Slicer;

public class ProfitSeekingStvgp implements StvgpProblem{

	private Table<StvgpType> table;
	
	private int depth;
	private ISeq<StvgpOp> booleanOperations;
	private ISeq<StvgpOp> vectorOperations;
	private ISeq<StvgpOp> booleanTerminals;
	private ISeq<StvgpOp> vectorTerminals;
	Predicate<? super StvgpChromosome> validator;
	
	// this is only here so I do'nt need to call this code over and over again
	private MarketSimulatorBuilder<StvgpType> market;
	
	private Map<Integer,Integer> generationSlices = new ConcurrentHashMap<>();

	public ProfitSeekingStvgp(Table<StvgpType> table, int depth, ISeq<StvgpOp> booleanOperations,
			ISeq<StvgpOp> vectorOperations, ISeq<StvgpOp> booleanTerminals, ISeq<StvgpOp> vectorTerminals,
			Predicate<? super StvgpChromosome> validator,boolean compoundMode) {
		this.table = table;
		this.depth = depth;
		this.booleanOperations = booleanOperations;
		this.vectorOperations = vectorOperations;
		this.booleanTerminals = booleanTerminals;
		this.vectorTerminals = vectorTerminals;
		this.validator = validator;
		
		requireNonNull(booleanOperations);
		if(booleanOperations.isEmpty())
			throw new IllegalArgumentException();
		for (StvgpOp stvgpOp : booleanOperations)
			if(!(stvgpOp.arity() > 0 && stvgpOp.outputType().isBooleanType()))
				throw new IllegalArgumentException();
		
		
		requireNonNull(vectorOperations);
		if(vectorOperations.isEmpty())
			throw new IllegalArgumentException();
		for (StvgpOp stvgpOp : vectorOperations)
			if(!(stvgpOp.arity() > 0 && stvgpOp.outputType().isVectorType()))
				throw new IllegalArgumentException();
		
		requireNonNull(booleanOperations);
		if(booleanTerminals.isEmpty())
			throw new IllegalArgumentException();
		for (StvgpOp stvgpOp : booleanTerminals)
			if(!(stvgpOp.arity() == 0 && stvgpOp.outputType().isBooleanType()))
				throw new IllegalArgumentException();
		
		requireNonNull(vectorTerminals);
		if(vectorTerminals.isEmpty())
			throw new IllegalArgumentException();
		for (StvgpOp stvgpOp : vectorTerminals)
			if(!(stvgpOp.arity() == 0 && stvgpOp.outputType().isVectorType()))
				throw new IllegalArgumentException();
		
		this.table.setTrainValidationRatio(.5);
		this.table.calculateSplitPoint();
		
		this.market = MarketSimulator.<StvgpType>builder(table)
				.penalizerRate(0.1)
				.compoundMode(compoundMode)
				.stoploss(0.05)
//				.takeprofit(0.1)
				;
	}

	@Override
	public Function<Tree<StvgpOp, ?>, Double> fitness() {
		return ((agent) -> {
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
				
			MarketSimulator<StvgpType> ms = market.build();
			return ms.simulateMarket(args -> StvgpProgram.eval(agent, args).getAsBooleanType() ? MarketAction.BUY : MarketAction.SELL, true, null);
		});
	}

	@Override
	public Codec<Tree<StvgpOp, ?>, StvgpGene> codec() {
		return Codec.of(
				Genotype.of(StvgpChromosome.of(
						depth, 
						validator,
						booleanOperations,
						vectorOperations,
						booleanTerminals,
						vectorTerminals)
					),
				Genotype::gene);
	}

	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<StvgpOp, ?> agent, boolean useTrainSet) {
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

		MarketSimulator<StvgpType> ms = market.trainSlice(table.getTrainSet()).build();
		double trainValidationRatio = table.getTrainValidationRatio();
		table.setTrainValidationRatio(.5);
		table.calculateSplitPoint();
		
		List<Double> transactions = output.get(TRANSACTION);
		double money = ms.simulateMarket((args) -> StvgpProgram.eval(agent, args).getAsBooleanType() ? MarketAction.BUY : MarketAction.SELL, useTrainSet,  market -> {
//					output.get(PROFIT_PERCENTAGE).add(market.getCurrentRow().get(market.getCurrentRow().size()-1).last());
					output.get(ValidationMetric.NORMALIZATION_CLOSE).add(market.getCurrentRow().get(market.getTable().columnIndexOf("closeNorm")).getAsVectorType().last());
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
	public ISeq<StvgpOp> operations() {
		return ISeq.concat(booleanOperations, vectorOperations);
	}

	@Override
	public ISeq<StvgpOp> terminals() {
		return ISeq.concat(booleanTerminals, vectorTerminals);
	}

	@Override
	public Table<StvgpType> getTable() {
		return table;
	}

	@Override
	public EngineConfiguration<StvgpGene, Double> getConf() {
		return EngineConfiguration.unUsed();
	}
}
