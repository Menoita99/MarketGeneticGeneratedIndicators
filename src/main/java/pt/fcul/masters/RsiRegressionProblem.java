package pt.fcul.masters;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
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
import io.jenetics.prog.regression.Sample;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.memory.MemoryManager;
import pt.fcul.masters.problems.ValidatedRegression;
import pt.fcul.masters.statefull.op.Ema;
import pt.fcul.masters.statefull.op.Percentage;
import pt.fcul.masters.statefull.op.Rsi;
import pt.fcul.masters.statistics.gui.Plotter;
import pt.fcul.masters.statistics.gui.model.Serie;

public class RsiRegressionProblem {


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private static MemoryManager memory = new MemoryManager(Market.EUR_USD,TimeFrame.H1);





	public static void main(String[] args) {
		Rsi rsi = new Rsi();
//		memory.removeColumn("volume");
		memory.createValueFrom((list) -> rsi.apply(new Double[] {list.get(memory.columnIndexOf("close"))}), "rsi");

		List<Sample<Double>> samples = samples();
		List<Sample<Double>> validatedSamples = validatedSamples();

		System.out.println(samples.size());
		System.out.println(validatedSamples.size());

		ValidatedRegression<Double> regression = ValidatedRegression.ofLists(
				ValidatedRegression.codecOf(allowedOps(), inputVariables(), 5, t -> t.gene().size() < 30),
				Error.of(LossFunction::mse),
				samples,
				validatedSamples);

		Engine<ProgramGene<Double>, Double> engine = Engine.builder(regression)
				.minimizing()
				.offspringSelector(new TournamentSelector<>(5))
				.survivorsFraction(0.02)
				.survivorsSelector(new TournamentSelector<>(5))
				.alterers(
						new SingleNodeCrossover<>(1),
						new Mutator<>(0.01))
				.executor(executor)
				.populationSize(1000)
				.build();

		EvolutionStatistics<Double, DoubleMomentStatistics> stats = EvolutionStatistics.ofNumber();
		Serie<Long,Double> testError = new Serie<>("Test fitness");
		Serie<Long,Double> validationError = new Serie<>("Validation fitness");

		EvolutionResult<ProgramGene<Double>, Double> result = engine.stream()
				.limit(Limits.byFixedGeneration(350))
				.peek(stats)
				.peek(e -> {
					testError.add(e.generation(), e.bestPhenotype().fitness());
					validationError.add(e.generation(), regression.validate(e));
				})
				.peek(e -> System.out.println(e.generation()+" "+e.bestFitness()))
//				.peek(e -> {
//					System.out.println("-----------------------");
//					e.population().forEach(System.out::println)	;
//				})
				.collect(EvolutionResult.toBestEvolutionResult());

		executor.shutdown();
		printResult(stats, testError, validationError, result);
	}





	private static ISeq<Op<Double>> allowedOps() {
		return ISeq.of(MathOp.ADD, MathOp.SUB, MathOp.MUL,MathOp.DIV, new Percentage(), new Ema(14));
	}





	private static ISeq<Op<Double>> inputVariables() {
		return ISeq.of(
				EphemeralConst.of(() -> (double)RandomRegistry.random().nextDouble()*10),
				Var.of("open", memory.columnIndexOf("open")),
				Var.of("high", memory.columnIndexOf("high")),
				Var.of("low",  memory.columnIndexOf("low")),
				Var.of("close", memory.columnIndexOf("close")));
				//Var.of("volume", memory.columnIndexOf("volume")));
	}





	private static List<Sample<Double>> samples() {
		return memory.asDoubleTestSamples();
	}





	private static List<Sample<Double>> validatedSamples() {
		return memory.asDoubleValidationSamples();
	}





	private static void printResult(EvolutionStatistics<Double, DoubleMomentStatistics> stats, Serie<Long, Double> testError,
			Serie<Long, Double> validationError, EvolutionResult<ProgramGene<Double>, Double> result) {

		ProgramGene<Double> program = result.bestPhenotype().genotype().gene();
		TreeNode<Op<Double>> tree = program.toTreeNode();
		MathExpr.rewrite(tree);
		System.out.println("Generations: " + result.totalGenerations());
		System.out.println("Function:    " + new MathExpr(tree));
		System.out.println("Error:       " + result.bestFitness());
		System.out.println("Stats:\n" + stats);

		Plotter.builder().lineChart("Fitness", testError,validationError).build().plot();

		Serie<Long,Double> rsi = new Serie<>("rsi");
		Serie<Long,Double> genRsi = new Serie<>("genRsi");

		List<Sample<Double>> samples = validatedSamples();
		System.out.println(samples.size());
		samples = samples.subList(Math.max(0,samples.size()-500),samples.size());
		long i = 0;
		for (Sample<Double> sample : samples) {
			rsi.add(i, sample.result());
			genRsi.add(i,program.eval(
					sample.argAt(0),
					sample.argAt(1),
					sample.argAt(2),
					sample.argAt(3),
					sample.argAt(4)));
			i++;
		}

		Plotter.builder().lineChart("RSI", rsi,genRsi).build().plot();
	}
}
