package pt.fcul.masters.vgp.runner;

import static pt.fcul.masters.utils.Constants.EXECUTOR;
import static pt.fcul.masters.utils.Constants.VECTORIAL_CONF;

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
import io.jenetics.prog.op.Const;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.utils.ColumnUtil;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class ProfitSeekingVgpRunner {


	public static void main(String[] args) {
		try {

			ProfitSeekingVGP problem = standartConfs();
			VECTORIAL_CONF.setMaxGenerations(300);
			VECTORIAL_CONF.setMaxPhenotypeAge(300);	

			BasicGpLogger<Vector, Double> gpLogger = new BasicGpLogger<>(problem, VECTORIAL_CONF);

			gpLogger.saveData();
			gpLogger.saveConf();

			log.info("Starting engine");
			Engine.builder(problem).maximizing()
			//			.interceptor(EvolutionResult.toUniquePopulation())	
			.setup(VECTORIAL_CONF)
			.alterers(
					new SingleNodeCrossover<>(VECTORIAL_CONF.getSelectionProb()), 
					new Mutator<>(VECTORIAL_CONF.getSelectionMutationProb()))
			.executor(EXECUTOR)
			.build()

			.stream()
			.limit(Limits.byFixedGeneration(VECTORIAL_CONF.getMaxGenerations()))
			//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
			.peek(gpLogger::log)
			.collect(EvolutionResult.toBestEvolutionResult());

			gpLogger.saveFitness();
			gpLogger.saveTransactions();

			//validation
			Map<ValidationMetric, List<Double>> validation = gpLogger.saveValidation();

			log.info("Finished, saving logs");

			gpLogger.plotFitness();

			Serie<Integer, Double> price = Serie.of("Price - validation", validation.get(ValidationMetric.PRICE));
			Serie<Integer, Double> money = Serie.of("Money - validation", validation.get(ValidationMetric.MONEY));
			Serie<Integer, Double> transaction = Serie.of("Transaction - validation", validation.get(ValidationMetric.TRANSACTION));
			Serie<Integer, Double> normalization = Serie.of("Normalization - validation", validation.get(ValidationMetric.NORMALIZATION_CLOSE));
			
			Plotter.builder().multiLineChart("Price/Money - validation", price, money).build().plot();
			Plotter.builder().multiLineChart("Price/Transaction - validation", price, transaction).build().plot();
			Plotter.builder().multiLineChart("Money/Transaction - validation", money, transaction).build().plot();
			Plotter.builder().multiLineChart("Normalization/price - validation", normalization, price).build().plot();
			Plotter.builder().multiLineChart("Normalization/Transaction - validation", normalization, transaction).build().plot();
//			
			//train
			validation = problem.validate(gpLogger.getLogs().getLast().getTreeNode(), true);

			price = Serie.of("Price - Train", validation.get(ValidationMetric.PRICE));
			money = Serie.of("Money - Train", validation.get(ValidationMetric.MONEY));
			transaction = Serie.of("Transaction - Train", validation.get(ValidationMetric.TRANSACTION));
			normalization = Serie.of("Normalization - Train", validation.get(ValidationMetric.NORMALIZATION_CLOSE));
			
			Plotter.builder().multiLineChart("Price/Money - Train", price, money).build().plot();
			Plotter.builder().multiLineChart("Price/Transaction - Train", price, transaction).build().plot();
			Plotter.builder().multiLineChart("Money/Transaction - Train", money, transaction).build().plot();
			Plotter.builder().multiLineChart("Normalization/price - Train", normalization, price).build().plot();
			Plotter.builder().multiLineChart("Normalization/Transaction - Train", normalization, transaction).build().plot();

			try {
				Csv.printSameXSeries(new File(gpLogger.getInstanceSaveFolder()+"stats.csv"), price,normalization,transaction);
			} catch (IOException e) {
				e.printStackTrace();
			}
				//CSV
			// price
			// norm price
			// buy price
			// sell price

			//			gpLogger.plotValidation(true);

		} finally {
			EXECUTOR.shutdown();
		}
	}





	private static ProfitSeekingVGP standartConfs() {
		try {
			log.info("Initializing table...");
			VectorTable table = new VectorTable(Market.SBUX,TimeFrame.D,LocalDateTime.of(2012, 1, 1, 0, 0),21, new DynamicStepNormalizer(25*6));
//			VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\EUR_USD H1 2015_1_1_ 0_0 VGP 2018_1_1_ 0_0 VGP_21DynamicStepNormalizer_960.csv").toPath());
			
			ColumnUtil.addEma(table,"closeNorm",200,21);
			ColumnUtil.addEma(table,"closeNorm",50,21);
			ColumnUtil.addEma(table,"closeNorm",13,21);
			ColumnUtil.addEma(table,"closeNorm",5,21);
			
			ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema5")).last() - row.get(table.columnIndexOf("ema13")).last(), "smallEmaDiff");
			ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema50")).last() - row.get(table.columnIndexOf("ema200")).last(), "bigEmaDiff");
			
			table.removeRows(0, 200);
//			ColumnUtil.addRsi(table,"close", VECTOR_SIZE);
			log.info("Initilized table.");

			return new ProfitSeekingVGP(
					table,
					ISeq.of(
							Const.of(Vector.of(0)),
							Const.of(Vector.of(1)),
							Const.of(Vector.of(-1)),
				//			EphemeralConst.of(() -> Vector.random(VECTOR_SIZE).dot(Vector.of(100).sub(Vector.of(-50)))),
//							Var.of("normOpen", table.columnIndexOf("openNorm")),
//							Var.of("normHigh", table.columnIndexOf("highNorm")),
//							Var.of("normLow",  table.columnIndexOf("lowNorm")),
							Var.of("normClose", table.columnIndexOf("closeNorm")),
							Var.of("normVol", table.columnIndexOf("volumeNorm")),
							Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
							Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"))
//							Var.of("close", table.columnIndexOf("close")),
//							Var.of("profitPercentage", table.getColumns().size())

//							Var.of("ema200", table.columnIndexOf("ema200")),
//							Var.of("ema50", table.columnIndexOf("ema50")),
//							Var.of("ema13",  table.columnIndexOf("ema13")),
//							Var.of("ema5", table.columnIndexOf("ema5"))

							// rsi
							// emas
							// vcma


							//							Var.of("open", table.columnIndexOf("open")),
							//							Var.of("high", table.columnIndexOf("high")),
							//							Var.of("low",  table.columnIndexOf("low")),
							//						Var.of("vc", table.columnIndexOf("vc"))
							),
					ISeq.of(
//							VectorialGpOP.values()
							VectorialGpOP.ADD,
							VectorialGpOP.DOT,
							VectorialGpOP.SUB,
							VectorialGpOP.DIV,

							//							VectorialGpOP.LOG,
//							VectorialGpOP.ABS,
							//							VectorialGpOP.ATAN,
							//							VectorialGpOP.ACOS,
							//							VectorialGpOP.ASIN,

							//							VectorialGpOP.L1_NORM,
							//							VectorialGpOP.L2_NORM,
//							VectorialGpOP.CUM_SUM,
//							VectorialGpOP.CUM_DIV,
//							VectorialGpOP.CUM_MEAN,
//							VectorialGpOP.CUM_PROD,
//							VectorialGpOP.CUM_SUB,
//							VectorialGpOP.MAX,
//							VectorialGpOP.MIN,
//							VectorialGpOP.PROD,
							VectorialGpOP.MEAN,
//							VectorialGpOP.SUM,
							VectorialGpOP.NEG,

							VectorialGpOP.SIN,
							VectorialGpOP.COS,
							VectorialGpOP.TAN,
							
							VectorialGpOP.GT_THEN
							), 
					5, 
					(t->  t.gene().depth() < 17),
					true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
