package pt.fcul.master.stvgp.runner;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.master.stvgp.StvgpSingleNodeCrossover;
import pt.fcul.master.stvgp.StvgpType;
import pt.fcul.master.stvgp.op.StvgpOps;
import pt.fcul.master.stvgp.op.StvgpVar;
import pt.fcul.master.stvgp.problems.ProfitSeekingStvgp;
import pt.fcul.masters.logger.StvgpLogger;
import pt.fcul.masters.table.StvgpTable;


@Log
public class Runner {

	private static final int VECTOR_SIZE = 13;
//	private static final int MAX_STEADY_FITNESS = 10;
	private static final int MAX_PHENOTYPE_AGE = 3;
	private static final int MAX_GENERATIONS = 70;
	private static final int POPULATION_SIZE = 500;
	private static final int TOURNAMENT_SIZE = (int)(POPULATION_SIZE * 0.075);
	private static final double SELECTOR_MUT = 0.001;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final ProfitSeekingStvgp PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			StvgpLogger logger = new StvgpLogger(PROBLEM);

			logger.saveData();
			logger.saveConf();
			
			log.info("Starting engine");
			
			Engine.builder(PROBLEM).maximizing()
//			.interceptor(EvolutionResult.toUniquePopulation())	
			.offspringSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
			.survivorsFraction(SURVIVOR_FRACTION)
			.survivorsSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
			.alterers(
					new StvgpSingleNodeCrossover<>(SELECTOR_PROB), 
					new Mutator<>(SELECTOR_MUT)
					)
			.executor(executor)
			.maximalPhenotypeAge(MAX_PHENOTYPE_AGE)
			.populationSize(POPULATION_SIZE)
			
			.build()

			.stream()
			.limit(Limits.byFixedGeneration(MAX_GENERATIONS))
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
			.peek(logger::log)
			.collect(EvolutionResult.toBestEvolutionResult());
			
			log.info("Finished, saving logs");
			logger.saveTransactions();
//			logger.saveFitness();
//			logger.saveValidation();
//			logger.plot();
//			logger.plotValidation(true);
		} finally {
			executor.shutdown();
		}
	}

	private static ProfitSeekingStvgp standartConfs() {
		try {
			log.info("Initializing table...");
			//StvgpTable table = new StvgpTable(Market.USD_JPY,TimeFrame.H1,LocalDateTime.of(2005, 1, 1, 0, 0),VECTOR_SIZE, new DynamicStepNormalizer(480));
			StvgpTable table = StvgpTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\USD_JPY H1 2018_1_1_ 0_0 VGP_13 DynamicDerivativeNormalizer_2500.csv").toPath());
			log.info("Initilized table.");

			return new ProfitSeekingStvgp(
					table,
					5, 
					ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR, StvgpOps.NOT,
							StvgpOps.MEAN_GT
//							StvgpOps.CUM_MEAN_GT,
//							StvgpOps.SUM_GT
							),
					
					ISeq.of(StvgpOps.ADD, StvgpOps.SUB, 
//							StvgpOps.ABS, 
//							StvgpOps.ACOS, StvgpOps.ASIN, 
							StvgpOps.ATAN,
							StvgpOps.COS, StvgpOps.CUM_SUM, StvgpOps.DIV, StvgpOps.DOT, 
//							StvgpOps.L1_NORM,
							StvgpOps.L2_NORM, StvgpOps.LOG, StvgpOps.MAX, StvgpOps.MIN, StvgpOps.PROD, 
//							StvgpOps.RES,
							StvgpOps.SIN, StvgpOps.SUM, StvgpOps.TAN, StvgpOps.VECT_IF_ELSE),
					
					ISeq.of(StvgpOps.TRUE, StvgpOps.FALSE),
					
					ISeq.of(
//							StvgpEphemeralConst.of(() -> StvgpType.of(Vector.random(VECTOR_SIZE))),
//							StvgpVar.of("normOpen", table.columnIndexOf("openNorm"), StvgpType.vector()),
//							StvgpVar.of("normHigh", table.columnIndexOf("highNorm"), StvgpType.vector()),
//							StvgpVar.of("normLow",  table.columnIndexOf("lowNorm"), StvgpType.vector()),
//							StvgpVar.of("normClose", table.columnIndexOf("closeNorm"), StvgpType.vector()),
//							StvgpVar.of("normVol", table.columnIndexOf("volumeNorm"), StvgpType.vector())
							StvgpVar.of("close", table.columnIndexOf("close"), StvgpType.vector())
							
						),
					c -> c.gene().size() < 200);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
