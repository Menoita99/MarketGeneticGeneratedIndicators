package pt.fcul.masters;

import java.util.stream.Stream;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.MathRewriteAlterer;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.prog.regression.Error;
import io.jenetics.prog.regression.LossFunction;
import io.jenetics.prog.regression.Regression;
import io.jenetics.prog.regression.Sample;

/**
 * Symbolic regression involves finding a mathematical expression, in symbolic
 * form, that provides a good, best, or perfect fit between a given finite
 * sampling of values of the independent variables and the associated values of
 * the dependent variables. --- John R. Koza, Genetic Programming I
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmst√∂tter</a>
 * @version 5.0
 * @since 3.9
 */
public class SymbolicRegression {

	// Definition of the allowed operations.
	private static final ISeq<Op<Double>> OPS =
		ISeq.of(MathOp.ADD, MathOp.SUB, MathOp.MUL);

	// Definition of the terminals.
	private static final ISeq<Op<Double>> TMS = ISeq.of(
		Var.of("x", 0),Var.of("y", 0),Var.of("z", 0),
		EphemeralConst.of(() -> (double)RandomRegistry.random().nextInt(10))
	);

	private static final Regression<Double> REGRESSION = Regression.of(
		Regression.codecOf(OPS, TMS, 5, t -> t.gene().size() < 30),
		Error.of(LossFunction::mse),
		// Lookup table for 4*x^3 - 3*x^2 + x
		Sample.ofDouble(-1.0,1, -8.0000),
		Sample.ofDouble(-0.9,1, -6.2460),
		Sample.ofDouble(-0.8,1, -4.7680),
		Sample.ofDouble(-0.7,2, -3.5420),
		Sample.ofDouble(-0.6,2, -2.5440),
		Sample.ofDouble(-0.5,2, -1.7500),
		Sample.ofDouble(-0.4,2, -1.1360),
		Sample.ofDouble(-0.3,2, -0.6780),
		Sample.ofDouble(-0.2,2, -0.3520),
		Sample.ofDouble(-0.1,2, -0.1340),
		Sample.ofDouble(0.0,2, 0.0000),
		Sample.ofDouble(0.1,2, 0.0740),
		Sample.ofDouble(0.2,2, 0.1120),
		Sample.ofDouble(0.3,2, 0.1380),
		Sample.ofDouble(0.4,2, 0.1760),
		Sample.ofDouble(0.5,2, 0.2500),
		Sample.ofDouble(0.6,2, 0.3840),
		Sample.ofDouble(0.7,2, 0.6020),
		Sample.ofDouble(0.8,2, 0.9280),
		Sample.ofDouble(0.9,2, 1.3860),
		Sample.ofDouble(1.0,2, 2.0000)
	);

	
	/**
	 * 1 RSI
	 * 2 dados 70/30
	 * gr·fico com 2 linhas treino e teste
	 */
	
	public static void main(final String[] args) {
		Engine<ProgramGene<Double>, Double> engine = Engine
			.builder(REGRESSION)
			.minimizing()
			.selector(
					new TournamentSelector<>()
					)
			.alterers(
				new SingleNodeCrossover<>(0.1),
				new Mutator<>(),
				new MathRewriteAlterer<>(0.5))
			.populationSize(500)
			.build();
		
		EvolutionStatistics<Double,?> statistics = EvolutionStatistics.ofNumber();


		Stream<EvolutionResult<ProgramGene<Double>, Double>> peek = engine.stream()
			.limit(Limits.byFitnessThreshold(0.01))
			.limit(100)
			.peek(statistics);
		EvolutionResult<ProgramGene<Double>, Double> result = peek
			.peek(p -> {
				TreeNode<Op<Double>> treeNode = p.bestPhenotype().genotype().gene().toTreeNode();
				System.out.println(p.generation()+" fitness: "+ REGRESSION.error(treeNode));	
			})
			.collect(EvolutionResult.toBestEvolutionResult());

		ProgramGene<Double> program = result.bestPhenotype()
			.genotype()
			.gene();

		final TreeNode<Op<Double>> tree = program.toTreeNode();
		MathExpr.rewrite(tree);
		System.out.println("Statistics:  " + statistics);
		System.out.println("Generations: " + result.totalGenerations());
		System.out.println("Function:    " + new MathExpr(tree));
		System.out.println("Error:       " + REGRESSION.error(tree));
	}
}
