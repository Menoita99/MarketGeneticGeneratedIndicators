package pt.fcul.masters.vgp.runner;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nd4j.linalg.api.ndarray.INDArray;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.table.INDArrayTable;
import pt.fcul.masters.vgp.op.INDArrayGpOP;
import pt.fcul.masters.vgp.problems.INDArrayGpRsiTrendForecast;

@Log
public class INDArrayGpRsiTrendForecastRunner {


	private static final int MAX_GENERATIONS = 70;
	private static final int TOURNAMENT_SIZE = 10;
	private static final int POPULATION_SIZE = 100;
	private static final int MAX_PHENOTYPE_AGE = 10;
	private static final double SELECTOR_MUT = 0.0001;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;

	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final INDArrayGpRsiTrendForecast PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			BasicGpLogger<INDArray, Double> gpLogger = new BasicGpLogger<>(PROBLEM);

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
					.limit(Limits.bySteadyFitness(5))
					.peek(gpLogger::log)
					.collect(EvolutionResult.toBestEvolutionResult());
			log.info("Finished, saving logs");
			gpLogger.save();
			gpLogger.plot();
		} finally {
		//	executor.shutdown();
		}
	}

	private static INDArrayGpRsiTrendForecast standartConfs() {
		log.info("Initializing table...");
		INDArrayTable table = new INDArrayTable(Market.USD_JPY,TimeFrame.H1,LocalDateTime.of(2022, 1, 1, 0, 0),1);
		log.info("Initilized table.");
//		addNormalizationColumns(table);
//		addEmas(table,"normClose");

		return new INDArrayGpRsiTrendForecast(
				4, 
				ISeq.of(
					INDArrayGpOP.ADD,
					INDArrayGpOP.MUL,
					INDArrayGpOP.SUB,
					INDArrayGpOP.DIV
//					VectorialGpOP.DOT,
//					VectorialGpOP.LOG,
//					VectorialGpOP.L1_NORM,
//					VectorialGpOP.CUM_SUM,
//					VectorialGpOP.SIN,
//					VectorialGpOP.COS,
//					VectorialGpOP.TAN
				),
				ISeq.of(
//						EphemeralConst.of(() -> Nd4j.rand(table.getVectorSize()).muli(100)),
//						Var.of("normOpen", table.columnIndexOf("normOpen")),
//						Var.of("normHigh", table.columnIndexOf("normHigh")),
//						Var.of("normLow",  table.columnIndexOf("normLow")),
//						Var.of("normClose", table.columnIndexOf("normClose")),
//						Var.of("normVol", table.columnIndexOf("normVol")),
						Var.of("open", table.columnIndexOf("open")),
						Var.of("high", table.columnIndexOf("high")),
						Var.of("low",  table.columnIndexOf("low")),
						Var.of("close", table.columnIndexOf("close"))
//						Var.of("vc", table.columnIndexOf("vc"))
				), 
				((t) -> {
//					System.out.println("caled");
					return t.gene().size() < 100;//> t.gene().depth() < 13,//t -> t.gene().depth() < 17),t -> 
				}),
				//t -> false,
				table);
	}
}
