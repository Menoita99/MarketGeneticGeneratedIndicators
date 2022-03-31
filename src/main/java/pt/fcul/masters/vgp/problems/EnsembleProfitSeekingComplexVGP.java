package pt.fcul.masters.vgp.problems;

import static pt.fcul.masters.utils.Constants.GENERATION;
import static pt.fcul.masters.utils.Constants.RAND;
import static pt.fcul.masters.utils.Constants.TRAIN_SLICES;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.market.Transaction;
import pt.fcul.masters.market.MarketSimulator.MarketSimulatorBuilder;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Slicer;
import pt.fcul.masters.vgp.util.ComplexVector;

@Log
public class EnsembleProfitSeekingComplexVGP implements Problem<ISeq<ProgramGene<ComplexVector>>, ProgramGene<ComplexVector>, Double>{


	private Table<ComplexVector> table;
	private ISeq<Op<ComplexVector>> terminals;
	private ISeq<Op<ComplexVector>> operations;
	private int depth;
	private Predicate<? super ProgramChromosome<ComplexVector>> validator;

	// this is only here so I do'nt need to call this code over and over again
	private MarketSimulatorBuilder<ComplexVector> market;

	private Map<Integer,Integer> generationSlices = new HashMap<>();
	
	
	/**
	 * 
	 * @param table table with the data
	 * @param terminals data
	 * @param operations operations
	 * @param depth initial tree depth
	 * @param validator 
	 * @param compoundMode
	 */
	public EnsembleProfitSeekingComplexVGP(Table<ComplexVector> table, 
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
		
		this.table.setTrainValidationRatio(0.8);
		this.table.calculateSplitPoint();
		
		this.market = MarketSimulator.<ComplexVector>builder(table).penalizerRate(0.1).compoundMode(compoundMode);

		log.info("Iniciatized problem");
	}

	
	
	@Override
	public Function<ISeq<ProgramGene<ComplexVector>>, Double> fitness() {
		return (agents) -> {
			int generation = GENERATION.get();

			if(!generationSlices.containsKey(generation))
				generationSlices.put(generation, RAND.nextInt(TRAIN_SLICES));
			
			MarketSimulator<ComplexVector> ms = market.trainSlice(Slicer.getSlice(table.getTrainSet(), TRAIN_SLICES, generationSlices.get(generation))).build();
			return ms.simulateMarket(args -> {
				ComplexVector buyResult = Program.eval(agents.get(0), args);
				ComplexVector sellResult = Program.eval(agents.get(1), args);
				return MarketAction.ensemble(buyResult.last().getReal() > 0, sellResult.last().getReal() < 0);
			} , true, null);
		};
	}
	
	
	
	public Map<ValidationMetric, List<Double>> validate(ISeq<ProgramGene<ComplexVector>>  agents, boolean useTrainSet) {
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.PRICE, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.TRANSACTION, new LinkedList<>()));

		MarketSimulator<ComplexVector> ms = market.build();
		double money = ms.simulateMarket((args) -> {
			ComplexVector buyResult = Program.eval(agents.get(0), args);
			ComplexVector sellResult = Program.eval(agents.get(1), args);
			return MarketAction.ensemble(buyResult.last().getReal() > 0, sellResult.last().getReal() < 0);
		}, useTrainSet, 
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
	public Codec<ISeq<ProgramGene<ComplexVector>>, ProgramGene<ComplexVector>> codec() {
		return Codec.of(
				Genotype.of(
						// Buy agent
						ProgramChromosome.of(
								depth,
								validator,
								operations,
								terminals
								)
						,
						// Sell Agent
						ProgramChromosome.of(
								depth,
								validator,
								operations,
								terminals
								)
						),
				geno -> ISeq.of(geno.get(0).gene(),geno.get(1).gene())
				);
	}

}
