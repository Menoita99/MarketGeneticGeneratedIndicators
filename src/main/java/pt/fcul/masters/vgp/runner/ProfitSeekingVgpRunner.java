package pt.fcul.masters.vgp.runner;

import java.io.File;
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
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class ProfitSeekingVgpRunner {

	private static final int VECTOR_SIZE = 13;
	private static final int MAX_GENERATIONS = 70;
	private static final int MAX_STEADY_FITNESS = 10;
	private static final int POPULATION_SIZE = 1000;
	private static final int TOURNAMENT_SIZE = (int)(POPULATION_SIZE * 0.1);
	private static final int MAX_PHENOTYPE_AGE = 10;
	private static final double SELECTOR_MUT = 0.0001;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;

	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final ProfitSeekingVGP PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			BasicGpLogger<Vector, Double> gpLogger = new BasicGpLogger<>(PROBLEM);
			
			log.info("Starting engine");
			Engine.builder(PROBLEM).maximizing()
					//.interceptor(EvolutionResult.toUniquePopulation())
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
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
					.peek(gpLogger::log)
					.collect(EvolutionResult.toBestEvolutionResult());
			log.info("Finished, saving logs");
			gpLogger.save();
			gpLogger.plot();
			gpLogger.plotValidation(true);
			
		} finally {
			executor.shutdown();
		}
	}

	private static ProfitSeekingVGP standartConfs() {
		try {
			log.info("Initializing table...");
//			VectorTable table = new VectorTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2018, 1, 1, 0, 0),VECTOR_SIZE, new DynamicStepNormalizer(2500));
			VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\USD_JPY H1 2015_1_1_ 0_0 VGP_13 DynamicDerivativeNormalizer_2500.csv").toPath());
			log.info("Initilized table.");
//		addNormalizationColumns(table);
//		addEmas(table,"normClose");

			return new ProfitSeekingVGP(
					table,
					ISeq.of(
							EphemeralConst.of(() -> Vector.random(VECTOR_SIZE).dot(Vector.of(100).sub(Vector.of(-50)))),
							Var.of("normOpen", table.columnIndexOf("openNorm")),
							Var.of("normHigh", table.columnIndexOf("highNorm")),
							Var.of("normLow",  table.columnIndexOf("lowNorm")),
							Var.of("normClose", table.columnIndexOf("closeNorm")),
							Var.of("normVol", table.columnIndexOf("volumeNorm")),
							Var.of("open", table.columnIndexOf("open")),
							Var.of("high", table.columnIndexOf("high")),
							Var.of("low",  table.columnIndexOf("low")),
							Var.of("close", table.columnIndexOf("close"))
//						Var.of("vc", table.columnIndexOf("vc"))
					),
					ISeq.of(
							VectorialGpOP.ADD,
							VectorialGpOP.DOT,
							VectorialGpOP.SUB,
							VectorialGpOP.DIV,
							VectorialGpOP.LOG,
							
//					VectorialGpOP.L1_NORM,
						VectorialGpOP.L2_NORM,
						VectorialGpOP.CUM_SUM,
						VectorialGpOP.MAX,
						VectorialGpOP.MIN,
						VectorialGpOP.PROD,
						VectorialGpOP.MEAN,
						VectorialGpOP.SUM,
						VectorialGpOP.NEG,
//					VectorialGpOP.IMAX,
							
							VectorialGpOP.SIN,
							VectorialGpOP.COS,
							VectorialGpOP.TAN
							), 
					5, 
					((t) -> {
						return t.gene().size() < 200;
					}),
					true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}