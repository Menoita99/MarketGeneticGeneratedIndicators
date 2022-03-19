package pt.fcul.masters.vgp.runner;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class ProfitSeekingVgpRunner {

	private static final EngineConfiguration<ProgramGene<Vector>, Double> CONF = EngineConfiguration.standart();


	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final ProfitSeekingVGP PROBLEM = standartConfs();

	public static void main(String[] args) {
		try {
			BasicGpLogger<Vector, Double> gpLogger = new BasicGpLogger<>(PROBLEM, CONF);

			gpLogger.saveData();
			gpLogger.saveConf();
			
			log.info("Starting engine");
			Engine.builder(PROBLEM).maximizing()
//			.interceptor(EvolutionResult.toUniquePopulation())	
			.setup(CONF)
			.alterers(
					new SingleNodeCrossover<>(CONF.getSelectionProb()), 
					new Mutator<>(CONF.getSelectionMutationProb())
					)
			.executor(executor)
			.build()

			.stream()
			.limit(Limits.byFixedGeneration(CONF.getMaxGenerations()))
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
			.peek(gpLogger::log)
			.collect(EvolutionResult.toBestEvolutionResult());
			log.info("Finished, saving logs");
			gpLogger.saveFitness();
			Map<ValidationMetric, List<Double>> validation = gpLogger.saveValidation();
			gpLogger.plot();
			
			
			Plotter.builder().multiLineChart("Price/Money", 
						Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
						Serie.of("Money", validation.get(ValidationMetric.MONEY))).build().plot();
			
			Plotter.builder().multiLineChart("Price/Transaction", 
					Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
					Serie.of("Money", validation.get(ValidationMetric.TRANSACTION))).build().plot();
			
			Plotter.builder().multiLineChart("Money/Transaction", 
					Serie.of("Price", validation.get(ValidationMetric.MONEY)), 
					Serie.of("Money", validation.get(ValidationMetric.TRANSACTION))).build().plot();
			
			
//			gpLogger.plotValidation(true);

		} finally {
			executor.shutdown();
		}
	}

	private static ProfitSeekingVGP standartConfs() {
		try {
			log.info("Initializing table...");
			//VectorTable table = new VectorTable(Market.USD_JPY,TimeFrame.H1,LocalDateTime.of(2005, 1, 1, 0, 0),VECTOR_SIZE, new DynamicStepNormalizer(480));
			VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\USD_JPY H1 2019_1_1_ 0_0 VGP_13 DynamicDerivativeNormalizer_2500.csv").toPath());
			log.info("Initilized table.");
			//		addNormalizationColumns(table);
			//		addEmas(table,"normClose");

			return new ProfitSeekingVGP(
					table,
					ISeq.of(
				//			EphemeralConst.of(() -> Vector.random(VECTOR_SIZE).dot(Vector.of(100).sub(Vector.of(-50)))),
							Var.of("normOpen", table.columnIndexOf("openNorm")),
							Var.of("normHigh", table.columnIndexOf("highNorm")),
							Var.of("normLow",  table.columnIndexOf("lowNorm")),
							Var.of("normClose", table.columnIndexOf("closeNorm")),
							Var.of("normVol", table.columnIndexOf("volumeNorm"))
							
							// rsi
							// emas
							// vcma
							
							
//							Var.of("open", table.columnIndexOf("open")),
//							Var.of("high", table.columnIndexOf("high")),
//							Var.of("low",  table.columnIndexOf("low")),
//							Var.of("close", table.columnIndexOf("close"))
							//						Var.of("vc", table.columnIndexOf("vc"))
							),
					ISeq.of(
							VectorialGpOP.ADD,
							VectorialGpOP.DOT,
							VectorialGpOP.SUB,
							VectorialGpOP.DIV,
							
							VectorialGpOP.LOG,
							VectorialGpOP.ABS,
							VectorialGpOP.ATAN,
							VectorialGpOP.ACOS,
							VectorialGpOP.ASIN,
													
							VectorialGpOP.L1_NORM,
							VectorialGpOP.L2_NORM,
							VectorialGpOP.CUM_SUM,
							VectorialGpOP.MAX,
							VectorialGpOP.MIN,
							VectorialGpOP.PROD,
							VectorialGpOP.MEAN,
							VectorialGpOP.SUM,
							VectorialGpOP.NEG,

							VectorialGpOP.SIN,
							VectorialGpOP.COS,
							VectorialGpOP.TAN
							), 
					6, 
					((t) ->  t.gene().size() < 250),
					true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
