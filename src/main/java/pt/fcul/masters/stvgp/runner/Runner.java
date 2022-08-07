package pt.fcul.masters.stvgp.runner;

import static pt.fcul.masters.utils.Constants.EXECUTOR;

import java.time.LocalDateTime;
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
import io.jenetics.util.ISeq;
import lombok.Data;
import lombok.extern.java.Log;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.StvgpLogger;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.stvgp.StvgpGene;
import pt.fcul.masters.stvgp.StvgpSingleNodeCrossover;
import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.stvgp.op.StvgpOps;
import pt.fcul.masters.stvgp.op.StvgpVar;
import pt.fcul.masters.stvgp.problems.ProfitSeekingStvgp;
import pt.fcul.masters.table.StvgpTable;
import pt.fcul.masters.utils.ColumnUtil;


@Log
@Data
public class Runner {

	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final EngineConfiguration<StvgpGene, Double> STVGP_CONF = EngineConfiguration.standart();

	public static void main(String[] args) {
		try {
			for(int i = 0; i < 10 ; i++) {
				ProfitSeekingStvgp problem = standartConfs();
				STVGP_CONF.setMaxGenerations(1000);
				STVGP_CONF.setMaxPhenotypeAge(1000);
				STVGP_CONF.setPopulationSize(2000);

				StvgpLogger logger = new StvgpLogger(problem);

				logger.saveData();
				logger.saveConf();

				log.info("Starting engine");
				Engine.builder(problem).maximizing()
				//			.interceptor(EvolutionResult.toUniquePopulation())	
				.setup(STVGP_CONF)
				.alterers(
						new StvgpSingleNodeCrossover<>(STVGP_CONF.getSelectionProb()), 
						new Mutator<>(STVGP_CONF.getSelectionMutationProb())
						)
				.executor(EXECUTOR)
				.build()

				.stream()
				.limit(Limits.byFixedGeneration(STVGP_CONF.getMaxGenerations()))
				//		.limit(Limits.bySteadyFitness(MAX_STEADY_FITNESS))
				.peek(logger::log)
				.collect(EvolutionResult.toBestEvolutionResult());

				log.info("Finished, saving logs");
				logger.saveFitness();
				logger.saveTransactions();

				Map<ValidationMetric, List<Double>> validation = logger.saveValidation();

				log.info("Finished, saving logs");

				logger.plotFitness();

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
				validation = problem.validate(logger.getLogs().getLast().getTreeNode(), true);

				price = Serie.of("Price - Train", validation.get(ValidationMetric.PRICE));
				money = Serie.of("Money - Train", validation.get(ValidationMetric.MONEY));
				transaction = Serie.of("Transaction - Train", validation.get(ValidationMetric.TRANSACTION));
				normalization = Serie.of("Normalization - Train", validation.get(ValidationMetric.NORMALIZATION_CLOSE));

				Plotter.builder().multiLineChart("Price/Money - Train", price, money).build().plot();
				Plotter.builder().multiLineChart("Price/Transaction - Train", price, transaction).build().plot();
				Plotter.builder().multiLineChart("Money/Transaction - Train", money, transaction).build().plot();
				Plotter.builder().multiLineChart("Normalization/price - Train", normalization, price).build().plot();
				Plotter.builder().multiLineChart("Normalization/Transaction - Train", normalization, transaction).build().plot();
			}
		} finally {
			executor.shutdown();
		}
	}

	private static ProfitSeekingStvgp standartConfs() {
		try {
			log.info("Initializing table...");
			StvgpTable table = new StvgpTable(Market.SBUX,TimeFrame.D,LocalDateTime.of(2012, 1, 1, 0, 0),21, new DynamicStepNormalizer(25*6));

			ColumnUtil.addEma(table,"closeNorm",200,21);
			ColumnUtil.addEma(table,"closeNorm",50,21);
			ColumnUtil.addEma(table,"closeNorm",13,21);
			ColumnUtil.addEma(table,"closeNorm",5,21);

			ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema5")).getAsVectorType().last() - row.get(table.columnIndexOf("ema13")).getAsVectorType().last() , "smallEmaDiff");
			ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema50")).getAsVectorType().last()  - row.get(table.columnIndexOf("ema200")).getAsVectorType().last() , "bigEmaDiff");

			table.removeRows(0, 200);
			log.info("Initilized table.");

			return new ProfitSeekingStvgp(
					table,
					5, 
					ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR, StvgpOps.NOT,
							StvgpOps.MEAN_GT,
							StvgpOps.CUM_MEAN_GT,
							StvgpOps.SUM_GT
							),

					ISeq.of(StvgpOps.ADD, 
							StvgpOps.SUB, 
							StvgpOps.DOT, 
							StvgpOps.DIV, 
							StvgpOps.RES,
							StvgpOps.PROD, 
							StvgpOps.ABS, 
							StvgpOps.ACOS, 
							StvgpOps.ASIN, 
							StvgpOps.ATAN,
							StvgpOps.COS, 
							StvgpOps.SIN, 
							StvgpOps.TAN, 
							StvgpOps.CUM_SUM, 
							StvgpOps.L1_NORM,
							StvgpOps.L2_NORM, 
							StvgpOps.MAX, 
							StvgpOps.MIN, 
							StvgpOps.SUM, 
							StvgpOps.LOG, 
							StvgpOps.NEG, 
							StvgpOps.VECT_IF_ELSE
							),

					ISeq.of(StvgpOps.TRUE, StvgpOps.FALSE),

					ISeq.of(
							StvgpVar.of("normClose", table.columnIndexOf("closeNorm"), StvgpType.vector()),
//							StvgpVar.of("normVol", table.columnIndexOf("volumeNorm"), StvgpType.vector()),
							//							StvgpVar.of("profitPercentage", table.getColumns().size(), StvgpType.vector()),
							StvgpVar.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff"), StvgpType.vector()),
							StvgpVar.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"), StvgpType.vector()),
							StvgpOps.ONE,StvgpOps.ZERO,StvgpOps.MINUS_ONE
							),
					c -> c.gene().depth() < 17);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
