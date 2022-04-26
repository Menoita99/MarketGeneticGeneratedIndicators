package pt.fcul.masters.vgp.problems;

import static pt.fcul.masters.utils.Constants.GENERATION;
import static pt.fcul.masters.utils.Constants.RAND;
import static pt.fcul.masters.utils.Constants.TRAIN_SLICES;

import java.util.HashMap;
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


	// this is only here so I do'nt need to call this code over and over again
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
		
		this.market = MarketSimulator.<ComplexVector>builder(table).penalizerRate(0.1).compoundMode(compoundMode).stoploss(0.05).takeprofit(0.1);

		log.info("Iniciatized problem");
	}


	
	
	@Override
	public Function<Tree<Op<ComplexVector>, ?> , Double> fitness() {
		return (agent) -> this.simulateMarketWithSimulator(agent, true, null);
	}


	
	
	public Double simulateMarketWithSimulator(Tree<Op<ComplexVector>, ?>  agent, boolean useTrainData,Consumer<MarketSimulator<ComplexVector>> interceptor) {
		int gen = GENERATION.get();

		generationSlices.computeIfAbsent(gen, g -> RAND.nextInt(TRAIN_SLICES));
		if(gen % 10 == 0)
			market.trainSlice(table.getTrainSet());//use all data
		else
			market.trainSlice(Slicer.getSlice(table.getTrainSet(), TRAIN_SLICES, generationSlices.get(gen)));
			
		MarketSimulator<ComplexVector> ms = market.build();
		return ms.simulateMarket((args) -> MarketAction.asSignal(Program.eval(agent, args).realMean()), useTrainData, interceptor);
	}


	
	
	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<ComplexVector>, ?>  agent, boolean useTrainSet) {
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.PRICE, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.TRANSACTION, new LinkedList<>(),
				ValidationMetric.NORMALIZATION_CLOSE, new LinkedList<>(),
//				ValidationMetric.PROFIT_PERCENTAGE, new LinkedList<>(),
				ValidationMetric.NORMALIZATION_VOLUME, new LinkedList<>()));

		MarketSimulator<ComplexVector> ms = market.trainSlice(table.getTrainSet()).build();
		double money = ms.simulateMarket((args) -> 
		MarketAction.asSignal(Program.eval(agent, args).realMean()), useTrainSet, 
		market -> {
//			output.get(ValidationMetric.PROFIT_PERCENTAGE).add(market.getCurrentRow().get(market.getCurrentRow().size()-1).last().getReal());
			output.get(ValidationMetric.NORMALIZATION_CLOSE).add(market.getCurrentRow().get(market.getTable().columnIndexOf("closeNorm")).last().getReal());
			output.get(ValidationMetric.NORMALIZATION_VOLUME).add(market.getCurrentRow().get(market.getTable().columnIndexOf("volumeNorm")).last().getReal());
			output.get(ValidationMetric.MONEY).add(market.getCurrentMoney());
			output.get(ValidationMetric.PRICE).add(market.getCurrentPrice());
			Transaction currentTransaction = market.getCurrentTransaction();
			output.get(ValidationMetric.TRANSACTION).add(currentTransaction == null || currentTransaction.isClose() ? 0D : currentTransaction.getType() == MarketAction.BUY ? 1D : -1D);
		});
		output.get(ValidationMetric.FITNESS).add(money);
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
