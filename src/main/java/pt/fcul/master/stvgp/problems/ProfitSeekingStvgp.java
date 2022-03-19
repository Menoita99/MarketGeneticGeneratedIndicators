package pt.fcul.master.stvgp.problems;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.util.ISeq;
import pt.fcul.master.market.MarketAction;
import pt.fcul.master.market.MarketSimulator;
import pt.fcul.master.market.Transaction;
import pt.fcul.master.stvgp.StvgpChromosome;
import pt.fcul.master.stvgp.StvgpGene;
import pt.fcul.master.stvgp.StvgpProgram;
import pt.fcul.master.stvgp.StvgpType;
import pt.fcul.master.stvgp.op.StvgpOp;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;

public class ProfitSeekingStvgp implements StvgpProblem{

	private Table<StvgpType> table;
	
	private int depth;
	private ISeq<StvgpOp> booleanOperations;
	private ISeq<StvgpOp> vectorOperations;
	private ISeq<StvgpOp> booleanTerminals;
	private ISeq<StvgpOp> vectorTerminals;
	Predicate<? super StvgpChromosome> validator;
	
	

	public ProfitSeekingStvgp(Table<StvgpType> table, int depth, ISeq<StvgpOp> booleanOperations,
			ISeq<StvgpOp> vectorOperations, ISeq<StvgpOp> booleanTerminals, ISeq<StvgpOp> vectorTerminals,
			Predicate<? super StvgpChromosome> validator) {
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
	}

	@Override
	public Function<Tree<StvgpOp, ?>, Double> fitness() {
		return ((agent) -> {
			MarketSimulator<StvgpType> market = MarketSimulator.<StvgpType>builder(table).penalizerRate(0).build();
			return market.simulateMarket((args) -> StvgpProgram.eval(agent, args).getAsBooleanType() ? MarketAction.BUY : MarketAction.SELL, true, null);
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
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.PRICE, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.TRANSACTION, new LinkedList<>()));

		MarketSimulator<StvgpType> ms = MarketSimulator.<StvgpType>builder(table).penalizerRate(0.1).build();
		
		double money = ms.simulateMarket((args) -> StvgpProgram.eval(agent, args).getAsBooleanType() ? MarketAction.BUY : MarketAction.SELL, useTrainSet, 
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
