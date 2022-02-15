package pt.fcul.masters;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.op.gp.statefull.Ema;
import pt.fcul.masters.problems.RsiTrendForecast;
import pt.fcul.masters.table.DoubleTable;
import pt.fcul.masters.table.Table;

public class RsiTrendForecastRunner {


	private static final int MAX_GENERATIONS = 70;
	private static final int TOURNAMENT_SIZE = 10;
	private static final int POPULATION_SIZE = 1000;
	private static final int MAX_PHENOTYPE_AGE = 10;
	private static final double SELECTOR_MUT = 0.0001;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;

	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final RsiTrendForecast PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			BasicGpLogger<Double, Double> gpLogger = new BasicGpLogger<>(PROBLEM);
			
			Engine.builder(PROBLEM).minimizing().interceptor(EvolutionResult.toUniquePopulation(1))
					.offspringSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
					.survivorsFraction(SURVIVOR_FRACTION)
					.survivorsSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
					.alterers(
							new SingleNodeCrossover<>(SELECTOR_PROB), 
							new Mutator<>(SELECTOR_MUT)
					)
					.executor(executor)
					.maximalPhenotypeAge(MAX_PHENOTYPE_AGE)
					.populationSize(POPULATION_SIZE)
					.build()

					.stream()
					.limit(Limits.byFixedGeneration(MAX_GENERATIONS))
					.limit(Limits.bySteadyFitness(5))
					.peek(gpLogger::log)
					.collect(EvolutionResult.toBestEvolutionResult());
			gpLogger.save();
				gpLogger.plot();
		} finally {
			executor.shutdown();
		}
	}

	private static RsiTrendForecast standartConfs() {
		Table<Double> table = new DoubleTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0));
		addNormalizationColumns(table);
		addEmas(table,"normClose");

		return new RsiTrendForecast(
				10, 
				ISeq.of(
						MathOp.EXP,MathOp.LOG,
						MathOp.COSH,MathOp.ACOS,
						MathOp.SINH,MathOp.ASIN,
						MathOp.TANH,MathOp.ATAN,
						MathOp.COS,MathOp.SIN,MathOp.TAN,
						MathOp.ADD,MathOp.SUB,MathOp.MUL,
						MathOp.DIV,MathOp.POW,MathOp.SIGNUM,
						MathOp.GT,MathOp.NEG,MathOp.SQRT,MathOp.CEIL,
						MathOp.FLOOR,MathOp.RINT,MathOp.HYPOT)//,
				//		new Ema())
				, 
				ISeq.of(
						EphemeralConst.of(() -> (double)RandomRegistry.random().nextDouble()*10),
						Var.of("normOpen", table.columnIndexOf("normOpen")),
						Var.of("normHigh", table.columnIndexOf("normHigh")),
						Var.of("normLow",  table.columnIndexOf("normLow")),
						Var.of("normClose", table.columnIndexOf("normClose")),
						Var.of("normVol", table.columnIndexOf("normVol")),
//						Var.of("open", table.columnIndexOf("open")),
//						Var.of("high", table.columnIndexOf("high")),
//						Var.of("low",  table.columnIndexOf("low")),
//						Var.of("close", table.columnIndexOf("close")),
						Var.of("Ema5", table.columnIndexOf("Ema[5]")),
						Var.of("Ema13", table.columnIndexOf("Ema[13]")),
						Var.of("Ema50", table.columnIndexOf("Ema[50]")),
						Var.of("Ema200", table.columnIndexOf("Ema[200]"))
//						Var.of("Ema800", table.columnIndexOf("Ema[800]"))
//						Var.of("vc", table.columnIndexOf("vc"))
						)
				, 
				//t -> t.gene().size() < 100,//> t.gene().depth() < 13,//t -> t.gene().depth() < 17),t -> 
				t -> false,
				table);
	}






	public static void addEmas(Table<Double> table, final String column) {
		Ema ema5 = new Ema(5);
		table.createValueFrom(row -> ema5.apply(new Double[] {row.get(table.columnIndexOf(column))}),ema5.name());

		Ema ema13 = new Ema(13);
		table.createValueFrom(row -> ema13.apply(new Double[] {row.get(table.columnIndexOf(column))}),ema13.name());

		Ema ema50 = new Ema(50);
		table.createValueFrom(row -> ema50.apply(new Double[] {row.get(table.columnIndexOf(column))}),ema50.name());

		Ema ema200 = new Ema(200);
		table.createValueFrom(row -> ema200.apply(new Double[] {row.get(table.columnIndexOf(column))}),ema200.name());

		//		Ema ema800 = new Ema(800);
		//		table.createValueFrom(row -> ema800.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema800.name());
		table.removeRows(0,200);
	}







	public static void addNormalizationColumns(Table<Double> table) {
		List<Double> closeColumn = new ArrayList<>();
		List<Double> openColumn = new ArrayList<>();
		List<Double> lowColumn = new ArrayList<>();
		List<Double> highColumn = new ArrayList<>();
		List<Double> volumeColumn = new ArrayList<>();

		table.foreach(row -> {
			closeColumn.add(row.get(table.columnIndexOf("close")));
			openColumn.add(row.get(table.columnIndexOf("open")));
			lowColumn.add(row.get(table.columnIndexOf("low")));
			highColumn.add(row.get(table.columnIndexOf("high")));
			volumeColumn.add(row.get(table.columnIndexOf("volume")));
		});

		Normalizer normalizer = new DynamicStepNormalizer(2500);
		
		table.addColumn(normalizer.apply(closeColumn), "normClose");
		table.addColumn(normalizer.apply(openColumn), "normOpen");
		table.addColumn(normalizer.apply(lowColumn), "normLow");
		table.addColumn(normalizer.apply(highColumn), "normHigh");
		table.addColumn(normalizer.apply(volumeColumn), "normVol");
	}
}
