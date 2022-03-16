package pt.fcul.master.stvgp.problems;

import static java.util.Objects.*;

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
	/**
	 * ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR, StvgpOps.MEAN_GT, StvgpOps.CUM_MEAN_GT,
								StvgpOps.IF_ELSE, StvgpOps.NOT, StvgpOps.SUM_GT),
						ISeq.of(StvgpOps.ADD, StvgpOps.SUB, StvgpOps.ABS, StvgpOps.ACOS, StvgpOps.ASIN, StvgpOps.ATAN,
								StvgpOps.COS, StvgpOps.CUM_SUM, StvgpOps.DIV, StvgpOps.DOT, StvgpOps.L1_NORM,
								StvgpOps.L2_NORM, StvgpOps.LOG, StvgpOps.MAX, StvgpOps.MIN, StvgpOps.PROD, StvgpOps.RES,
								StvgpOps.SIN, StvgpOps.SUM, StvgpOps.TAN, StvgpOps.VECT_IF_ELSE),
						ISeq.of(StvgpOps.TRUE, StvgpOps.FALSE),
						ISeq.of(StvgpEphemeralConst.of(() -> StvgpType.of(Vector.random(VECTOR_SIZE))))
	 */
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
		return Map.of();
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
	public EngineConfiguration getConf() {
		return new EngineConfiguration();
	}
}
