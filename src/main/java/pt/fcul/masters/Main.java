package pt.fcul.masters;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
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
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import pt.fcul.masters.statistics.gui.Plotter;

public class Main {


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


	private static final Regression<Double> REGRESSION = Regression.of(
			Regression.codecOf(allowedOps(), inputVariables(), 5, t -> t.gene().size() < 30),
			Error.of(LossFunction::mse),
			samples());

	public static void main(final String[] args) {
		Engine<ProgramGene<Double>, Double> engine = Engine.builder(REGRESSION)
				.minimizing()
				.alterers(
						new SingleNodeCrossover<>(0.1),
						new Mutator<>())
				.executor(executor)
				.maximalPhenotypeAge(70)
				.populationSize(200)
				.build();

		EvolutionResult<ProgramGene<Double>, Double> result = engine.stream()
				.limit(Limits.byFitnessThreshold(0.01))
				.limit(Limits.bySteadyFitness(1000))
				.peek(EvolutionStatistics.ofNumber())
				.collect(EvolutionResult.toBestEvolutionResult());

		ProgramGene<Double> program = result.bestPhenotype().genotype().gene();
		TreeNode<Op<Double>> tree = program.toTreeNode();
		MathExpr.rewrite(tree);
		
		System.out.println("Generations: " + result.totalGenerations());
		System.out.println("Function:    " + new MathExpr(tree));
		System.out.println("Error:       " + REGRESSION.error(tree));
		executor.shutdown();
		
		LinkedHashMap<Integer, Integer> data = new LinkedHashMap<>(Map.of(0,1,1,5,2,6,3,8,4,9,5,9,6,8,7,1,8,7,9,10));
		Plotter.builder().lineChart(data,"test").build().plot();
	}	



	/**
	 * The function we want to determine.
	 */
	public static double goalFunction(double value) {
		return Math.cos(value)/Math.sin(value);
	}

	public static List<Sample<Double>> samples(){
		Random r = new Random();
		List<Sample<Double>> samples = new LinkedList<>();
		for (int i = 0; i < 1000; i++) {
			double rd = r.nextDouble() * Math.PI * 2 - Math.PI;
			samples.add(Sample.ofDouble(rd, goalFunction(rd)));
		}
		return samples;
	}

	/**
	 * Page 137 of manual-6.3.0 Jenetics
	 * First, you have to define the set of atomic
	 * mathematical operations, the GP is working
	 * with. These operations influence
	 * the search space and is a kind of a priori knowledge put into the GP
	 * @return 
	 */
	public static ISeq<Op<Double>> allowedOps() {
		return ISeq.of(MathOp.ADD, MathOp.SUB, MathOp.MUL,MathOp.DIV) ;
	}

	/**
	 *  Page 137 of manual-6.3.0 Jenetics
	 *  As a second step you have to define the terminal operations. Terminals are either
	 *	constants or variables. The number of variables defines the domain dimension of
	 *	the fitness function.
	 * @return 
	 */
	public static ISeq<Op<Double>> inputVariables() {
		return ISeq.of(
				Var.of("x", 0),
				EphemeralConst.of(() -> (double)RandomRegistry.random().nextInt(10))
				);
	}
}

