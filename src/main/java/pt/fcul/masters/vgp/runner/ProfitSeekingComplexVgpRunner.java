package pt.fcul.masters.vgp.runner;

import static pt.fcul.masters.utils.Constants.EXECUTOR;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.plotter.file.Csv;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Const;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.data.normalizer.DynamicDerivativeNormalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.ComplexVectorTable;
import pt.fcul.masters.vgp.op.ComplexVectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingComplexVGP;
import pt.fcul.masters.vgp.util.ComplexVector;

@Log
public class ProfitSeekingComplexVgpRunner {

	
	private static final int VECTOR_SIZE = 24;
	private static final EngineConfiguration<ProgramGene<ComplexVector>, Double> COMPLEX_VECTORIAL_CONF = EngineConfiguration.standart();




	public static void main(String[] args) {
		try {
//			COMPLEX_VECTORIAL_CONF.setMaxGenerations(100);
			ProfitSeekingComplexVGP problem = standartConfs();
			
			BasicGpLogger<ComplexVector, Double> gpLogger = new BasicGpLogger<>(problem, COMPLEX_VECTORIAL_CONF);

			gpLogger.saveData();
			gpLogger.saveConf();
			
			
			log.info("Starting engine");
			Engine.builder(problem).maximizing()
//			.interceptor(EvolutionResult.toUniquePopulation())	
			.setup(COMPLEX_VECTORIAL_CONF)
			.alterers(
					new SingleNodeCrossover<>(COMPLEX_VECTORIAL_CONF.getSelectionProb()), 
					new Mutator<>(COMPLEX_VECTORIAL_CONF.getSelectionMutationProb())
					)
			.executor(EXECUTOR)
			.build()

			.stream()
			.limit(Limits.byFixedGeneration(COMPLEX_VECTORIAL_CONF.getMaxGenerations()))
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
			.peek(gpLogger::log)
			.collect(EvolutionResult.toBestEvolutionResult());
			
			gpLogger.saveFitness();
			gpLogger.saveTransactions();
			
			Map<ValidationMetric, List<Double>> validation = gpLogger.saveValidation();
			
			log.info("Finished, saving logs");

			gpLogger.plotFitness();

			Serie<Integer, Double> price = Serie.of("Price", validation.get(ValidationMetric.PRICE));
			Serie<Integer, Double> money = Serie.of("Money", validation.get(ValidationMetric.MONEY));
			Serie<Integer, Double> transaction = Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION));
			Serie<Integer, Double> normalization = Serie.of("Normalization", validation.get(ValidationMetric.NORMALIZATION_CLOSE));
			
			Plotter.builder().multiLineChart("Price/Money", price, money).build().plot();
			Plotter.builder().multiLineChart("Price/Transaction", price, transaction).build().plot();
			Plotter.builder().multiLineChart("Money/Transaction", money, transaction).build().plot();
//			Plotter.builder().multiLineChart("Normalization/price", normalization, price).build().plot();
			Plotter.builder().multiLineChart("Normalization/Transaction", normalization, transaction).build().plot();
			
			//train
			validation = problem.validate(gpLogger.getLogs().getLast().getTreeNode(), true);

			price = Serie.of("Price", validation.get(ValidationMetric.PRICE));
			money = Serie.of("Money", validation.get(ValidationMetric.MONEY));
			transaction = Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION));
			normalization = Serie.of("Normalization", validation.get(ValidationMetric.NORMALIZATION_CLOSE));
			
			Plotter.builder().multiLineChart("Price/Money", price, money).build().plot();
			Plotter.builder().multiLineChart("Price/Transaction", price, transaction).build().plot();
			Plotter.builder().multiLineChart("Money/Transaction", money, transaction).build().plot();
//			Plotter.builder().multiLineChart("Normalization/price", normalization, price).build().plot();
			Plotter.builder().multiLineChart("Normalization/Transaction", normalization, transaction).build().plot();
			
			try {
				Csv.printSameXSeries(new File(gpLogger.getInstanceSaveFolder()+"stats.csv"), price,normalization,transaction);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			gpLogger.plotValidation(true);

		} finally {
			EXECUTOR.shutdown();
		}
	}

	
	
	
	
	private static ProfitSeekingComplexVGP standartConfs() {
		try {
			log.info("Initializing table...");
			ComplexVectorTable table = new ComplexVectorTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0),LocalDateTime.of(2018, 1, 1, 0, 0),VECTOR_SIZE, new DynamicDerivativeNormalizer(960));
//			ComplexVectorTable table = ComplexVectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\EUR_USD H1 2018_1_1_ 0_0 VGP_21DynamicStepNormalizer_960.csv").toPath());
			log.info("Initilized table.");

//			ColumnUtil.addEma(table,"closeNorm",200,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",50,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",13,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",5,VECTOR_SIZE);
//			ColumnUtil.addRsi(table,"close", VECTOR_SIZE);
			table.removeRow(200);

			return new ProfitSeekingComplexVGP(
					table,
					ISeq.of(
//							EphemeralConst.of(() -> ComplexVector.random(VECTOR_SIZE)),
//							Const.of(ComplexVector.of(Math.PI)),
//							Const.of(ComplexVector.of(Math.PI/2d)),
//							Const.of(ComplexVector.of(3d*Math.PI/2d)),
//							Const.of(ComplexVector.of(Math.E)),
							Const.of(ComplexVector.of(0)),
							Const.of(ComplexVector.of(1)),
							Const.of(ComplexVector.of(-1)),
//							Var.of("normOpen", table.columnIndexOf("openNorm")),
//							Var.of("normHigh", table.columnIndexOf("highNorm")),
//							Var.of("normLow",  table.columnIndexOf("lowNorm")),
							Var.of("normClose", table.columnIndexOf("closeNorm")),
							Var.of("normVol", table.columnIndexOf("volumeNorm")),
							Var.of("profitPercentage", table.getColumns().size())
//							Var.of("ema200", table.columnIndexOf("ema200")),
//							Var.of("ema50", table.columnIndexOf("ema50")),
//							Var.of("ema13",  table.columnIndexOf("ema13")),
//							Var.of("ema5", table.columnIndexOf("ema5"))
//							Var.of("rsi", table.columnIndexOf("rsi"))
							// rsi
							// emas
							// vcma
							
							
//							Var.of("open", table.columnIndexOf("open")),
//							Var.of("high", table.columnIndexOf("high")),
//							Var.of("low",  table.columnIndexOf("low")),
//							Var.of("close", table.columnIndexOf("close"))
							//Var.of("vc", table.columnIndexOf("vc"))
							
							),
					ISeq.of(
//							ComplexVectorialGpOP.values()
							ComplexVectorialGpOP.ADD,
							ComplexVectorialGpOP.DOT,
							ComplexVectorialGpOP.SUB,
							ComplexVectorialGpOP.DIV,
//							ComplexVectorialGpOP.EXP,
//							
							ComplexVectorialGpOP.LOG,
							ComplexVectorialGpOP.SQRT,
							ComplexVectorialGpOP.SQRT1Z,	
//							
							ComplexVectorialGpOP.CONJUGATE,
							ComplexVectorialGpOP.RECIPROCAL,
//							ComplexVectorialGpOP.ONE_FIELD,
//							ComplexVectorialGpOP.ZERO_FIELD,
//							
							ComplexVectorialGpOP.ATAN,
							ComplexVectorialGpOP.ACOS,
							ComplexVectorialGpOP.ASIN,
//							
//							ComplexVectorialGpOP.TANH,
//							ComplexVectorialGpOP.COSH,
//							ComplexVectorialGpOP.SINH,
//							
							ComplexVectorialGpOP.SIN,
							ComplexVectorialGpOP.COS,
							ComplexVectorialGpOP.TAN,
//							
//							ComplexVectorialGpOP.CUM_SUM,
//							ComplexVectorialGpOP.CUM_DIV,
							ComplexVectorialGpOP.CUM_MEAN,
//							ComplexVectorialGpOP.CUM_PROD,
//							ComplexVectorialGpOP.CUM_SUB,
//							
//							ComplexVectorialGpOP.MAX_ABS,
//							ComplexVectorialGpOP.MAX_IMG,
//							ComplexVectorialGpOP.MAX_PHI,
//							ComplexVectorialGpOP.MAX_REAL,
//							
//							ComplexVectorialGpOP.MIN_ABS,
//							ComplexVectorialGpOP.MIN_IMG,
//							ComplexVectorialGpOP.MIN_PHI,
//							ComplexVectorialGpOP.MIN_REAL,
//							
//							ComplexVectorialGpOP.PROD,
							ComplexVectorialGpOP.MEAN,
							ComplexVectorialGpOP.SUM,
//							
							ComplexVectorialGpOP.NEG,
//							ComplexVectorialGpOP.ABS
							
							ComplexVectorialGpOP.GT_THEN_REAL
							), 
					5, 
					((t) ->  t.gene().size() < 100),
					true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
