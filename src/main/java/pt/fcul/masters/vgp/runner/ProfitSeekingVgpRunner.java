package pt.fcul.masters.vgp.runner;

import static pt.fcul.masters.utils.Constants.EXECUTOR;
import static pt.fcul.masters.utils.Constants.VECTORIAL_CONF;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class ProfitSeekingVgpRunner {


	public static void main(String[] args) {
		try {
			
			ProfitSeekingVGP problem = standartConfs();

			BasicGpLogger<Vector, Double> gpLogger = new BasicGpLogger<>(problem, VECTORIAL_CONF);

			gpLogger.saveData();
			gpLogger.saveConf();

			log.info("Starting engine");
			Engine.builder(problem).maximizing()
			//			.interceptor(EvolutionResult.toUniquePopulation())	
			.setup(VECTORIAL_CONF)
			.alterers(
					new SingleNodeCrossover<>(VECTORIAL_CONF.getSelectionProb()), 
					new Mutator<>(VECTORIAL_CONF.getSelectionMutationProb())
					)
			.executor(EXECUTOR)
			.build()

			.stream()
			.limit(Limits.byFixedGeneration(VECTORIAL_CONF.getMaxGenerations()))
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
			.peek(gpLogger::log)
			.collect(EvolutionResult.toBestEvolutionResult());

			gpLogger.saveFitness();
			gpLogger.saveTransactions();

			Map<ValidationMetric, List<Double>> validation = gpLogger.saveValidation();

			log.info("Finished, saving logs");

			gpLogger.plot();

			Plotter.builder().multiLineChart("Price/Money", 
					Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
					Serie.of("Money", validation.get(ValidationMetric.MONEY))).build().plot();

			Plotter.builder().multiLineChart("Price/Transaction", 
					Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
					Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION))).build().plot();

			Plotter.builder().multiLineChart("Money/Transaction", 
					Serie.of("Money", validation.get(ValidationMetric.MONEY)), 
					Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION))).build().plot();


			//			gpLogger.plotValidation(true);

		} finally {
			EXECUTOR.shutdown();
		}
	}





	private static ProfitSeekingVGP standartConfs() {
		try {
			log.info("Initializing table...");
			//VectorTable table = new VectorTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2005, 1, 1, 0, 0),21, new DynamicStepNormalizer(480));
			VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\USD_JPY H1 2012_1_1_ 0_0 VGP_50 DynamicStepNormalizer_250.csv").toPath());
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
							Var.of("normVol", table.columnIndexOf("volumeNorm")),
							
							Var.of("ema200", table.columnIndexOf("ema200")),
							Var.of("ema50", table.columnIndexOf("ema50")),
							Var.of("ema13",  table.columnIndexOf("ema13")),
							Var.of("ema5", table.columnIndexOf("ema5"))

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

							//							VectorialGpOP.LOG,
							VectorialGpOP.ABS,
							//							VectorialGpOP.ATAN,
							//							VectorialGpOP.ACOS,
							//							VectorialGpOP.ASIN,

							//							VectorialGpOP.L1_NORM,
							//							VectorialGpOP.L2_NORM,
							VectorialGpOP.CUM_SUM,
							VectorialGpOP.CUM_DIV,
							VectorialGpOP.CUM_MEAN,
							VectorialGpOP.CUM_PROD,
							VectorialGpOP.CUM_SUB,
							VectorialGpOP.MAX,
							VectorialGpOP.MIN,
							//							VectorialGpOP.PROD,
							VectorialGpOP.MEAN,
							//							VectorialGpOP.SUM,
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
