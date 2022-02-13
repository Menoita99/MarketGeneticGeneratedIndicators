package pt.fcul.masters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.plotter.file.Csv;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Const;
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
import pt.fcul.masters.examples.ValidatedRegression;
import pt.fcul.masters.memory.DoubleTable;
import pt.fcul.masters.op.gp.statefull.Ema;
import pt.fcul.masters.op.gp.statefull.Percentage;
import pt.fcul.masters.op.gp.statefull.Rsi;

public class RsiRegressionRunner{


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private static DoubleTable memory = new DoubleTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0));


	public static void main(String[] args) {
		Rsi rs = new Rsi();
//		memory.removeColumn("volume");
		memory.createValueFrom((list) -> rs.apply(new Double[] {list.get(memory.columnIndexOf("close"))}), "rs");

		List<Sample<Double>> samples = samples();
		List<Sample<Double>> validatedSamples = validatedSamples();

		ValidatedRegression<Double> regression = ValidatedRegression.ofLists(
				ValidatedRegression.codecOf(allowedOps(), inputVariables(), 5, t -> t.gene().size() < 100),//t -> t.gene().depth() < 17),
				Error.of(LossFunction::mse),
				samples,
				validatedSamples);

		Engine<ProgramGene<Double>, Double> engine = Engine.builder(regression)
				.minimizing()
				.interceptor(EvolutionResult.toUniquePopulation(1))
				.offspringSelector(new TournamentSelector<>(10))
				.survivorsFraction(0.02)
				.survivorsSelector(new TournamentSelector<>(10))
				.alterers(
						new SingleNodeCrossover<>(0.7),
						new Mutator<>(0.01))
				.executor(executor)
				.populationSize(1000)
				.build();

		EvolutionStatistics<Double, DoubleMomentStatistics> stats = EvolutionStatistics.ofNumber();
		Serie<Long,Double> testError = new Serie<>("Test fitness");
		Serie<Long,Double> validationError = new Serie<>("Validation fitness");

		EvolutionResult<ProgramGene<Double>, Double> result = engine.stream()
				//.limit(Limits.byFitnessThreshold(0.00002))
				.limit(Limits.byFixedGeneration(70))
				.limit(Limits.bySteadyFitness(5))
				.peek(stats)
				.peek(e -> {
					testError.add(e.generation(), e.bestPhenotype().fitness() > 1000 ? 1000D : e.bestPhenotype().fitness());
					double validate = regression.validate(e);
					validationError.add(e.generation(), validate > 1000 ? 1000D :validate);
				})
//				.peek(e -> {
//					System.out.println("-----------------------");
//					e.population().forEach(System.out::println)	;
//				})
				.peek(e -> System.out.println(e.generation()+" "+e.bestFitness()))
				.collect(EvolutionResult.toBestEvolutionResult());

		executor.shutdown();
		printResult(stats, testError, validationError, result);
	}





	private static ISeq<Op<Double>> allowedOps() {
		return ISeq.of(
				MathOp.ADD, MathOp.SUB, MathOp.MUL, MathOp.DIV,//, MathOp.MAX, MathOp.MIN
				MathOp.SIN, MathOp.COS, MathOp.TAN ,new Percentage(), 
				new Ema(),
				new Ema(14)
				);
	}





	private static ISeq<Op<Double>> inputVariables() {
		return ISeq.of(
				Const.of(0D),
				Const.of(100D),
				EphemeralConst.of(() -> (double)RandomRegistry.random().nextDouble()*10),
				Var.of("open", memory.columnIndexOf("open")),
				Var.of("high", memory.columnIndexOf("high")),
				Var.of("low",  memory.columnIndexOf("low")),
				Var.of("close", memory.columnIndexOf("close")));
				//Var.of("volume", memory.columnIndexOf("volume")));
	}





	private static List<Sample<Double>> samples() {
		return memory.asDoubleTrainSamples();
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
		//System.out.println("Function:    " + new MathExpr(tree));
		try(PrintWriter pw = new PrintWriter(new File("C:\\Users\\Owner\\Desktop\\formulas.txt"))){
			pw.println(new MathExpr(tree));
			pw.println(tree.size());
			pw.print(result.bestPhenotype().genotype().gene().size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Error:       " + result.bestFitness());
		System.out.println("Stats:\n" + stats);

		Plotter.builder().lineChart("Fitness", testError,validationError).build().plot();

		Serie<Long,Double> rsi = new Serie<>("rsi");
		Serie<Long,Double> genRsi = new Serie<>("genRsi");

		List<Sample<Double>> samples = validatedSamples();
	//	samples = samples.subList(Math.max(0,samples.size()-1000),samples.size());
		System.out.println(samples.size());
		long i = 0;
		for (Sample<Double> sample : samples) {
			rsi.add(i, normalizeRs(sample.result()));
			genRsi.add(i,normalizeRs(program.eval(
					sample.argAt(0),
					sample.argAt(1),
					sample.argAt(2),
					sample.argAt(3),
					sample.argAt(4))));
			i++;
		}

	
		try {
			Csv.printSameXSeries(new File("C:\\Users\\Owner\\Desktop\\agent_data.csv"),rsi,genRsi);
			Csv.printSameXSeries(new File("C:\\Users\\Owner\\Desktop\\fitness_data.csv"),testError,validationError );
//			Csv.popFileDialog(new File("C:\\Users\\Owner\\Desktop")).printSameXSeries(testError,validationError );
		} catch (IOException e) {
			e.printStackTrace();
		}
		Plotter.builder().lineChart("RSI", rsi,genRsi).build().plot();
	}
	
	private static double normalizeRs(double rs) {
		return  rs;//100 - (100 / (1 + rs));
	}
}
