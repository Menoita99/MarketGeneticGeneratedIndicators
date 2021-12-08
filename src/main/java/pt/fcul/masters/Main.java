package pt.fcul.masters;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
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

public class Main {


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


	private static final Regression<Double> REGRESSION = Regression.of(
			Regression.codecOf(allowedOps(), inputVariables(), 5),
			Error.of(LossFunction::mse),
			samples());

	public static void main(final String[] args) {
		Engine<ProgramGene<Double>, Double> engine = Engine.builder(REGRESSION)
				.minimizing()
				.alterers(
						new SingleNodeCrossover<>(0.7),
						new Mutator<>())
				.executor(executor)
				.maximalPhenotypeAge(70)
				.populationSize(10)
				.build();

		EvolutionResult<ProgramGene<Double>, Double> result = engine.stream()
				.limit(Limits.byFitnessThreshold(0.01))
				.limit(Limits.bySteadyFitness(100))
				.peek(e -> System.out.println(e.generation()+" "+e.bestFitness()))
				.peek(e -> {
				System.out.println("-----------------------");
				e.population().forEach(k ->System.out.println(new MathExpr(k.genotype().gene())))	;
			}).collect(EvolutionResult.toBestEvolutionResult());

		ProgramGene<Double> program = result.bestPhenotype().genotype().gene();
		TreeNode<Op<Double>> tree = program.toTreeNode();
		MathExpr.rewrite(tree);
		
		System.out.println("Generations: " + result.totalGenerations());
		System.out.println("Function:    " + new MathExpr(tree));
		System.out.println("Error:       " + REGRESSION.error(tree));
		executor.shutdown();
		
		Serie<Integer, Double> function = new Serie<>("goal");
		Serie<Integer, Double> evolFucn = new Serie<>("evolFunc");
		
		for (int i = 0; i < 100; i++) {
			function.add(i, goalFunction(i));
			System.out.println( result.bestPhenotype().genotype().gene().eval((double)i));
			evolFucn.add(i, result.bestPhenotype().genotype().gene().eval((double)i));
		}
		
		Plotter.builder().lineChart("Result",function,evolFucn).build().plot();
	}



	/**
	 * The function we want to determine.
	 */
	public static double goalFunction(double i) {
		return 4*Math.pow(i,3)/i - 3*Math.pow(i, 2) + i;//( value / (value -1) - 1) * 100;
	}

	public static List<Sample<Double>> samples(){
//		Random r = new Random();
		List<Sample<Double>> samples = new LinkedList<>();
		for (int i =1; i < 1000; i++) {
			double rd = i;//r.nextDouble() * Math.PI * 2 - Math.PI;
			samples.add(Sample.ofDouble(rd, goalFunction(rd)));
			System.out.println(goalFunction(rd));
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
		return ISeq.of(MathOp.ADD, MathOp.SUB, MathOp.MUL,MathOp.DIV);//,StateOp.PERCETANGE.getOp()) ;
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
				EphemeralConst.of(() -> (double)RandomRegistry.random().nextInt(10)));
	}
}

