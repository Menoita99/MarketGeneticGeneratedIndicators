package pt.fcul.masters.vgp.runner;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.VgpRsiTrendForecast;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class VgpRsiTrendForecastRunner {

	private static final int MAX_GENERATIONS = 70;
	private static final int MAX_STEADY_FITNESS = 10;
	private static final int POPULATION_SIZE = 1000;
	private static final int TOURNAMENT_SIZE = (int)(POPULATION_SIZE * 0.1);
	private static final int MAX_PHENOTYPE_AGE = 10;
	private static final double SELECTOR_MUT = 0.0001;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;

	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final VgpRsiTrendForecast PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			BasicGpLogger<Vector, Double> gpLogger = new BasicGpLogger<>(PROBLEM, EngineConfiguration.unUsed());

			log.info("Starting engine");
			Engine.builder(PROBLEM).minimizing()
					//.interceptor(EvolutionResult.toUniquePopulation(1))
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
					.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
					.peek(gpLogger::log)
					.collect(EvolutionResult.toBestEvolutionResult());
			log.info("Finished, saving logs");
			gpLogger.save();
			gpLogger.plot();
		} finally {
		//	executor.shutdown();
		}
	}

	private static VgpRsiTrendForecast standartConfs() {
		log.info("Initializing table...");
		VectorTable table = new VectorTable(Market.USD_JPY,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0),13);
		log.info("Initilized table.");
//		addNormalizationColumns(table);
//		addEmas(table,"normClose");

		return new VgpRsiTrendForecast(
				5, 
				ISeq.of(
					VectorialGpOP.ADD,
					VectorialGpOP.DOT,
					VectorialGpOP.SUB,
					VectorialGpOP.DIV,
					VectorialGpOP.LOG,
					
//					VectorialGpOP.L1_NORM,
//					VectorialGpOP.L2_NORM,
//					VectorialGpOP.CUM_SUM,
//					VectorialGpOP.MAX,
//					VectorialGpOP.MIN,
//					VectorialGpOP.PROD,
//					VectorialGpOP.MEAN,
//					VectorialGpOP.SUM,
//					VectorialGpOP.IMAX,
					
					VectorialGpOP.SIN,
					VectorialGpOP.COS,
					VectorialGpOP.TAN
				),
				ISeq.of(
						EphemeralConst.of(() -> Vector.random(13).dot(Vector.of(100).sub(Vector.of(-50)))),
//						Var.of("normOpen", table.columnIndexOf("normOpen")),
//						Var.of("normHigh", table.columnIndexOf("normHigh")),
//						Var.of("normLow",  table.columnIndexOf("normLow")),
//						Var.of("normClose", table.columnIndexOf("normClose")),
//						Var.of("normVol", table.columnIndexOf("normVol")),
//						Var.of("open", table.columnIndexOf("open")),
//						Var.of("high", table.columnIndexOf("high")),
//						Var.of("low",  table.columnIndexOf("low")),
						Var.of("close", table.columnIndexOf("close"))
//						Var.of("vc", table.columnIndexOf("vc"))
				), 
				((t) -> {
//					System.out.println("caled");
					return t.gene().size() < 200;//> t.gene().depth() < 13,//t -> t.gene().depth() < 17),t -> 
				}),
				//t -> false,
				table);
	}

}
