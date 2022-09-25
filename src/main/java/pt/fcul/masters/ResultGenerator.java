package pt.fcul.masters;

import static pt.fcul.masters.db.model.Market.COTY;
import static pt.fcul.masters.db.model.Market.EUR_USD;
import static pt.fcul.masters.db.model.Market.FORD;
import static pt.fcul.masters.db.model.Market.KO;
import static pt.fcul.masters.db.model.Market.TWTR;
import static pt.fcul.masters.utils.Constants.EXECUTOR;
import static pt.fcul.masters.utils.Constants.VECTORIAL_CONF;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.TreeRewriteAlterer;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Const;
import io.jenetics.prog.op.ConstRewriter;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.data.normalizer.DynamicStandartNormalizer;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.data.normalizer.StandartNormalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.gp.problems.ProfitSeekingGP;
import pt.fcul.masters.logger.BasicGpLogger;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.StvgpLogger;
import pt.fcul.masters.stvgp.StvgpGene;
import pt.fcul.masters.stvgp.StvgpSingleNodeCrossover;
import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.stvgp.op.StvgpOps;
import pt.fcul.masters.stvgp.op.StvgpVar;
import pt.fcul.masters.stvgp.problems.ProfitSeekingStvgp;
import pt.fcul.masters.table.ComplexVectorTable;
import pt.fcul.masters.table.DoubleTable;
import pt.fcul.masters.table.RowNormalizationComplexVectorTable;
import pt.fcul.masters.table.RowNormalizationVectorTable;
import pt.fcul.masters.table.StvgpTable;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.utils.ColumnUtil;
import pt.fcul.masters.vgp.op.ComplexVectorialGpOP;
import pt.fcul.masters.vgp.op.VectorialGpOP;
import pt.fcul.masters.vgp.problems.ProfitSeekingComplexVGP;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.ComplexVector;
import pt.fcul.masters.vgp.util.Vector;

@Log
public class ResultGenerator {

	private static final int DEPTH = 12;
	private static final int SIZE = 512;
	private static final int POPULATION_SIZE = 600;
	private static final int MAX_PHENOTYPE_AGE = 50;
	private static final int MAX_GENERATIONS = 400;
	private static final int VEC_SIZE = 21;
	private static final List<Market> MARKETS = List.of(COTY,KO,FORD,TWTR,EUR_USD);

	private static final int RUNS = 3;

	private static final EngineConfiguration<ProgramGene<ComplexVector>, Double> COMPLEX_VECTORIAL_CONF = EngineConfiguration.standart();
	private static final Normalizer NORMALIZER = new DynamicStandartNormalizer(20 * 6);
	private static final Normalizer VECTOR_NORMALIZER = new StandartNormalizer();

	public static void main(String[] args) {
		mainProfitSeekingGP();
		mainProfitSeekingVGP();
		mainProfitSeekingComplexVGP();
		mainProfitSeekingNormalizedVectorVGP();
		mainProfitSeekingNormalizedVectorComplexVGP();
		mainProfitSeekingStvgp();
	}

	public static void mainProfitSeekingVGP() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {

					ProfitSeekingVGP problem = standartConfs(m);
//					ProfitSeekingVGP problem = standartConfsVgpRowNorm(m);
					VECTORIAL_CONF.setMaxGenerations(MAX_GENERATIONS);
					VECTORIAL_CONF.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					VECTORIAL_CONF.setPopulationSize(POPULATION_SIZE);

					BasicGpLogger<Vector> gpLogger = new BasicGpLogger<>(problem, VECTORIAL_CONF);

					gpLogger.saveData();
					gpLogger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(VECTORIAL_CONF)
							.alterers(new TreeRewriteAlterer<>(ConstRewriter.ofType(Vector.class)),
									new SingleNodeCrossover<>(VECTORIAL_CONF.getSelectionProb()),
									new Mutator<>(VECTORIAL_CONF.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build().stream()
							.limit(Limits.byFixedGeneration(VECTORIAL_CONF.getMaxGenerations())).peek(gpLogger::log)
							.collect(EvolutionResult.toBestEvolutionResult());

					gpLogger.saveFitness();
					gpLogger.saveTransactions();
					gpLogger.saveValidation();
					log.info("Finished, saving logs");
				}
			}
		} finally {
			// EXECUTOR.shutdown();
		}
	}

	public static void mainProfitSeekingNormalizedVectorVGP() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {

					ProfitSeekingVGP problem = standartConfsVgpRowNorm(m);
					VECTORIAL_CONF.setMaxGenerations(MAX_GENERATIONS);
					VECTORIAL_CONF.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					VECTORIAL_CONF.setPopulationSize(POPULATION_SIZE);

					BasicGpLogger<Vector> gpLogger = new BasicGpLogger<>(problem, VECTORIAL_CONF);
					gpLogger.setSaveFolder(gpLogger.getSaveFolder()+"_normalizedByRow");

					gpLogger.saveData();
					gpLogger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(VECTORIAL_CONF)
							.alterers(new TreeRewriteAlterer<>(ConstRewriter.ofType(Vector.class)),
									new SingleNodeCrossover<>(VECTORIAL_CONF.getSelectionProb()),
									new Mutator<>(VECTORIAL_CONF.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build().stream()
							.limit(Limits.byFixedGeneration(VECTORIAL_CONF.getMaxGenerations())).peek(gpLogger::log)
							.collect(EvolutionResult.toBestEvolutionResult());

					gpLogger.saveFitness();
					gpLogger.saveTransactions();
					gpLogger.saveValidation();
					log.info("Finished, saving logs");
				}
			}
		} finally {
			// EXECUTOR.shutdown();
		}
	}
	
	public static void mainProfitSeekingComplexVGP() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {
					ProfitSeekingComplexVGP problem = standartConfsProfitSeekingComplexVGP(m);
//					ProfitSeekingComplexVGP problem = standartConfsProfitSeekingComplexVGPRowNorm(m);
					COMPLEX_VECTORIAL_CONF.setMaxGenerations(MAX_GENERATIONS);
					COMPLEX_VECTORIAL_CONF.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					COMPLEX_VECTORIAL_CONF.setPopulationSize(POPULATION_SIZE);

					BasicGpLogger<ComplexVector> gpLogger = new BasicGpLogger<>(problem, COMPLEX_VECTORIAL_CONF);

					gpLogger.saveData();
					gpLogger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(COMPLEX_VECTORIAL_CONF)
							.alterers(new TreeRewriteAlterer<>(ConstRewriter.ofType(ComplexVector.class)),
									new SingleNodeCrossover<>(COMPLEX_VECTORIAL_CONF.getSelectionProb()),
									new Mutator<>(COMPLEX_VECTORIAL_CONF.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build().stream()
							.limit(Limits.byFixedGeneration(COMPLEX_VECTORIAL_CONF.getMaxGenerations()))
							.peek(gpLogger::log).collect(EvolutionResult.toBestEvolutionResult());

					gpLogger.saveFitness();
					gpLogger.saveTransactions();

					gpLogger.saveValidation();

					log.info("Finished, saving logs");
				}
			}
		} finally {
			// EXECUTOR.shutdown();
		}
	}

	public static void mainProfitSeekingNormalizedVectorComplexVGP() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {
					ProfitSeekingComplexVGP problem = standartConfsProfitSeekingComplexVGPRowNorm(m);
					COMPLEX_VECTORIAL_CONF.setMaxGenerations(MAX_GENERATIONS);
					COMPLEX_VECTORIAL_CONF.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					COMPLEX_VECTORIAL_CONF.setPopulationSize(POPULATION_SIZE);

					BasicGpLogger<ComplexVector> gpLogger = new BasicGpLogger<>(problem, COMPLEX_VECTORIAL_CONF);
					gpLogger.setSaveFolder(gpLogger.getSaveFolder()+"_normalizedByRow");

					gpLogger.saveData();
					gpLogger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(COMPLEX_VECTORIAL_CONF)
							.alterers(new TreeRewriteAlterer<>(ConstRewriter.ofType(ComplexVector.class)),
									new SingleNodeCrossover<>(COMPLEX_VECTORIAL_CONF.getSelectionProb()),
									new Mutator<>(COMPLEX_VECTORIAL_CONF.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build().stream()
							.limit(Limits.byFixedGeneration(COMPLEX_VECTORIAL_CONF.getMaxGenerations()))
							.peek(gpLogger::log).collect(EvolutionResult.toBestEvolutionResult());

					gpLogger.saveFitness();
					gpLogger.saveTransactions();

					gpLogger.saveValidation();

					log.info("Finished, saving logs");
				}
			}
		} finally {
			// EXECUTOR.shutdown();
		}
	}
	
	public static void mainProfitSeekingGP() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {
					ProfitSeekingGP problem = standartConfsProfitSeekingGP(m);

					EngineConfiguration<ProgramGene<Double>, Double> standart = EngineConfiguration.standart();
					standart.setMaxGenerations(MAX_GENERATIONS);
					standart.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					standart.setPopulationSize(POPULATION_SIZE);

					BasicGpLogger<Double> gpLogger = new BasicGpLogger<>(problem, standart);

					gpLogger.saveData();
					gpLogger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(standart)
							.alterers(new TreeRewriteAlterer<>(ConstRewriter.DOUBLE),
									new SingleNodeCrossover<>(standart.getSelectionProb()),
									new Mutator<>(standart.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build()

							.stream().limit(Limits.byFixedGeneration(standart.getMaxGenerations())).peek(gpLogger::log)
							.collect(EvolutionResult.toBestEvolutionResult());

					gpLogger.saveFitness();
					gpLogger.saveTransactions();

					// validation
					gpLogger.saveValidation();

					log.info("Finished, saving logs");
				}
			}
		} finally {
			// EXECUTOR.shutdown();
		}
	}

	public static void mainProfitSeekingStvgp() {
		try {

			for (int i = 0; i < RUNS; i++) {
				for (Market m : MARKETS) {
					ProfitSeekingStvgp problem = standartConfsProfitSeekingStvgp(m);

					EngineConfiguration<StvgpGene, Double> STVGP_CONF = EngineConfiguration.standart();
					STVGP_CONF.setMaxGenerations(MAX_GENERATIONS);
					STVGP_CONF.setMaxPhenotypeAge(MAX_PHENOTYPE_AGE);
					STVGP_CONF.setPopulationSize(POPULATION_SIZE);

					StvgpLogger logger = new StvgpLogger(problem);

					logger.saveData();
					logger.saveConf();

					log.info("Starting engine");
					Engine.builder(problem).maximizing().setup(STVGP_CONF)
							.alterers(new StvgpSingleNodeCrossover<>(STVGP_CONF.getSelectionProb()),
									new Mutator<>(STVGP_CONF.getSelectionMutationProb()))
							.interceptor(EvolutionResult.toUniquePopulation()).executor(EXECUTOR).build()

							.stream().limit(Limits.byFixedGeneration(STVGP_CONF.getMaxGenerations())).peek(logger::log)
							.collect(EvolutionResult.toBestEvolutionResult());

					log.info("Finished, saving logs");
					logger.saveFitness();
					logger.saveTransactions();
					logger.saveValidation();

					log.info("Finished, saving logs");

					logger.plotFitness();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ProfitSeekingStvgp standartConfsProfitSeekingStvgp(Market m) {
		try {
			log.info("Initializing table...");
			StvgpTable table = new StvgpTable(m, TimeFrame.D, LocalDateTime.of(2015, 1, 1, 0, 0),
					LocalDateTime.of(2023, 1, 1, 0, 0), VEC_SIZE, new DynamicStepNormalizer(25 * 6));

			ColumnUtil.addEma(table, "closeNorm", 200, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 50, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 13, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 5, VEC_SIZE);

			ColumnUtil.addRsi(table, "close", VEC_SIZE, true);

			ColumnUtil.add(table, VEC_SIZE,
					(row, index) -> row.get(table.columnIndexOf("ema5")).getAsVectorType().last()
							- row.get(table.columnIndexOf("ema13")).getAsVectorType().last(),
					"smallEmaDiff");
			ColumnUtil.add(table, VEC_SIZE,
					(row, index) -> row.get(table.columnIndexOf("ema50")).getAsVectorType().last()
							- row.get(table.columnIndexOf("ema200")).getAsVectorType().last(),
					"bigEmaDiff");

			table.removeRows(0, 200);
			log.info("Initilized table.");

			return new ProfitSeekingStvgp(table, 3,
					ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR, StvgpOps.NOT, StvgpOps.MEAN_GT,
							StvgpOps.CUM_MEAN_GT, StvgpOps.SUM_GT),

					ISeq.of(StvgpOps.ADD, StvgpOps.SUB, StvgpOps.DOT, StvgpOps.DIV,
//							StvgpOps.RES,
//							StvgpOps.PROD, 
//							StvgpOps.ABS, 
//							StvgpOps.ACOS, 
//							StvgpOps.ASIN, 
//							StvgpOps.ATAN,
							StvgpOps.COS, StvgpOps.SIN, StvgpOps.TAN,
//							StvgpOps.CUM_SUM, 
//							StvgpOps.L1_NORM,
//							StvgpOps.L2_NORM, 
//							StvgpOps.MAX, 
//							StvgpOps.MIN, 
//							StvgpOps.SUM, 
//							StvgpOps.LOG, 
							StvgpOps.NEG, StvgpOps.VECT_IF_ELSE, StvgpOps.STD_VAR),

					ISeq.of(StvgpOps.TRUE, StvgpOps.FALSE),

					ISeq.of(StvgpVar.of("rsi", table.columnIndexOf("rsi"), StvgpType.vector()),
							StvgpVar.of("ema200", table.columnIndexOf("ema200"), StvgpType.vector()),
							StvgpVar.of("ema50", table.columnIndexOf("ema50"), StvgpType.vector()),
							StvgpVar.of("ema13", table.columnIndexOf("ema13"), StvgpType.vector()),
							StvgpVar.of("ema5", table.columnIndexOf("ema5"), StvgpType.vector()),
							StvgpVar.of("normClose", table.columnIndexOf("closeNorm"), StvgpType.vector()),
							StvgpVar.of("normVol", table.columnIndexOf("volumeNorm"), StvgpType.vector()),
							StvgpVar.of("profitPercentage", table.getColumns().size(), StvgpType.vector()),
							StvgpVar.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff"), StvgpType.vector()),
							StvgpVar.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"), StvgpType.vector()),
							StvgpOps.ONE, StvgpOps.ZERO, StvgpOps.MINUS_ONE),
					(t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProfitSeekingGP standartConfsProfitSeekingGP(Market m) {
		try {
			log.info("Initializing table...");
			Normalizer normalizer = NORMALIZER; // 6 meses
			DoubleTable table = new DoubleTable(m, TimeFrame.D, LocalDateTime.of(2015, 1, 1, 0, 0),
					LocalDateTime.of(2023, 1, 1, 0, 0));
			table.addColumn(normalizer.apply(table.getColumn("close")), "closeNorm");

			ColumnUtil.addEma(table, "closeNorm", 200);
			ColumnUtil.addEma(table, "closeNorm", 50);
			ColumnUtil.addEma(table, "closeNorm", 13);
			ColumnUtil.addEma(table, "closeNorm", 5);

			ColumnUtil.addRsi(table, "close", true);

			table.createValueFrom(
					(row, index) -> row.get(table.columnIndexOf("ema5")) - row.get(table.columnIndexOf("ema13")),
					"smallEmaDiff");
			table.createValueFrom(
					(row, index) -> row.get(table.columnIndexOf("ema50")) - row.get(table.columnIndexOf("ema200")),
					"bigEmaDiff");
			table.removeRows(0, 200);

			log.info("Initilized table.");

			return new ProfitSeekingGP(table, ISeq.of(Const.of(0D), Const.of(1D), Const.of(-1D),
					Var.of("rsi", table.columnIndexOf("rsi")), Var.of("profitPercentage", table.getColumns().size()),
					Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
					Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff")),
					Var.of("profitPercentage", table.getColumns().size()),
					Var.of("ema200", table.columnIndexOf("ema200")), Var.of("ema50", table.columnIndexOf("ema50")),
					Var.of("ema13", table.columnIndexOf("ema13")), Var.of("ema5", table.columnIndexOf("ema5"))),
					ISeq.of(

							MathOp.ADD, MathOp.MUL, MathOp.SUB, MathOp.DIV,

							MathOp.SIGNUM, MathOp.NEG,

							MathOp.SIN, MathOp.COS, MathOp.TAN,
//							MathOp.HYPOT,
//							MathOp.POW,

							MathOp.GT),
					3, (t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProfitSeekingComplexVGP standartConfsProfitSeekingComplexVGP(Market m) {
		try {
			log.info("Initializing table...");
			Normalizer normalizer = NORMALIZER;
			ComplexVectorTable table = new ComplexVectorTable(m, TimeFrame.D, LocalDateTime.of(2015, 1, 1, 0, 0),
					LocalDateTime.of(2023, 1, 1, 0, 0), VEC_SIZE, normalizer);
			log.info("Initilized table.");

			ColumnUtil.addEma(table, "closeNorm", 200, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 50, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 13, VEC_SIZE);
			ColumnUtil.addEma(table, "closeNorm", 5, VEC_SIZE);
			ColumnUtil.addComplexRsi(table, "close", VEC_SIZE, true);

			ColumnUtil.addComplex(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema5")).last().getReal()
					- row.get(table.columnIndexOf("ema13")).last().getReal(), "smallEmaDiff");
			ColumnUtil.addComplex(table, VEC_SIZE,
					(row, index) -> row.get(table.columnIndexOf("ema50")).last().getReal()
							- row.get(table.columnIndexOf("ema200")).last().getReal(),
					"bigEmaDiff");

			table.removeRow(200);

			return new ProfitSeekingComplexVGP(table, ISeq.of(EphemeralConst.of(() -> ComplexVector.random(VEC_SIZE)),
					Const.of(Complex.I.toString(), ComplexVector.of(Complex.I)),

					Const.of(Complex.valueOf(0, Math.PI).toString(), ComplexVector.of(Complex.valueOf(0, Math.PI))),
					Const.of(Complex.valueOf(0, -Math.PI).toString(), ComplexVector.of(Complex.valueOf(0, -Math.PI))),

					Const.of(Complex.valueOf(0, Math.PI / 2).toString(),
							ComplexVector.of(Complex.valueOf(0, Math.PI / 2))),
					Const.of(Complex.valueOf(0, -Math.PI / 2).toString(),
							ComplexVector.of(Complex.valueOf(0, -Math.PI / 2))),

					Const.of(ComplexVector.of(0)), Const.of(ComplexVector.of(1)), Const.of(ComplexVector.of(-1)),

					Var.of("rsi", table.columnIndexOf("rsi")), Var.of("normClose", table.columnIndexOf("closeNorm")),
					Var.of("normVol", table.columnIndexOf("volumeNorm")),
					Var.of("profitPercentage", table.getColumns().size()),

					Var.of("ema200", table.columnIndexOf("ema200")), Var.of("ema50", table.columnIndexOf("ema50")),
					Var.of("ema13", table.columnIndexOf("ema13")), Var.of("ema5", table.columnIndexOf("ema5")),

					Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
					Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"))

			// Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiffnorm")),
			// Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiffnorm"))

			), ISeq.of(ComplexVectorialGpOP.ADD, ComplexVectorialGpOP.DOT, ComplexVectorialGpOP.SUB,
					ComplexVectorialGpOP.DIV,
					//
					ComplexVectorialGpOP.LOG, ComplexVectorialGpOP.SQRT,
					//
//							ComplexVectorialGpOP.CONJUGATE,
//							ComplexVectorialGpOP.RECIPROCAL,
//							ComplexVectorialGpOP.ATAN,
//							ComplexVectorialGpOP.ACOS,
//							ComplexVectorialGpOP.ASIN,
//
//							ComplexVectorialGpOP.TANH,
//							ComplexVectorialGpOP.COSH,
//							ComplexVectorialGpOP.SINH,

					ComplexVectorialGpOP.SIN, ComplexVectorialGpOP.COS, ComplexVectorialGpOP.TAN,
//
//							ComplexVectorialGpOP.CUM_SUM,
//							ComplexVectorialGpOP.CUM_DIV,
					ComplexVectorialGpOP.CUM_MEAN,
//							ComplexVectorialGpOP.CUM_PROD,
//							ComplexVectorialGpOP.CUM_SUB,
//							//							
//							ComplexVectorialGpOP.PROD,
					ComplexVectorialGpOP.MEAN,
//							ComplexVectorialGpOP.SUM,
					//
					ComplexVectorialGpOP.NEG,
					//
					ComplexVectorialGpOP.GT_THEN_REAL, ComplexVectorialGpOP.GT_THEN_COMPLEX), 3,
					(t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProfitSeekingComplexVGP standartConfsProfitSeekingComplexVGPRowNorm(Market m) {
		try {
			log.info("Initializing table...");
			RowNormalizationComplexVectorTable table = new RowNormalizationComplexVectorTable(m, TimeFrame.D,
					LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2023, 1, 1, 0, 0), VEC_SIZE, VECTOR_NORMALIZER);
			log.info("Initilized table.");

			ColumnUtil.addEma(table, "close", 200, VEC_SIZE);
			ColumnUtil.addEma(table, "close", 50, VEC_SIZE);
			ColumnUtil.addEma(table, "close", 13, VEC_SIZE);
			ColumnUtil.addEma(table, "close", 5, VEC_SIZE);
			ColumnUtil.addComplexRsi(table, "close", VEC_SIZE, true);

			ColumnUtil.addComplex(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema5")).last().getReal()
					- row.get(table.columnIndexOf("ema13")).last().getReal(), "smallEmaDiff");
			ColumnUtil.addComplex(table, VEC_SIZE,
					(row, index) -> row.get(table.columnIndexOf("ema50")).last().getReal()
							- row.get(table.columnIndexOf("ema200")).last().getReal(),
					"bigEmaDiff");

			table.removeRow(200);
			table.normalize();
			return new ProfitSeekingComplexVGP(table, ISeq.of(EphemeralConst.of(() -> ComplexVector.random(VEC_SIZE)),
					Const.of(Complex.I.toString(), ComplexVector.of(Complex.I)),

					Const.of(Complex.valueOf(0, Math.PI).toString(), ComplexVector.of(Complex.valueOf(0, Math.PI))),
					Const.of(Complex.valueOf(0, -Math.PI).toString(), ComplexVector.of(Complex.valueOf(0, -Math.PI))),

					Const.of(Complex.valueOf(0, Math.PI / 2).toString(),
							ComplexVector.of(Complex.valueOf(0, Math.PI / 2))),
					Const.of(Complex.valueOf(0, -Math.PI / 2).toString(),
							ComplexVector.of(Complex.valueOf(0, -Math.PI / 2))),

					Const.of(ComplexVector.of(0)), Const.of(ComplexVector.of(1)), Const.of(ComplexVector.of(-1)),

					Var.of("rsi", table.columnIndexOf("rsi")), Var.of("closeNorm", table.columnIndexOf("closeNorm")),
					Var.of("close", table.columnIndexOf("close")), Var.of("vol", table.columnIndexOf("close")),
					Var.of("profitPercentage", table.getColumns().size()),

					Var.of("ema200", table.columnIndexOf("ema200")), Var.of("ema50", table.columnIndexOf("ema50")),
					Var.of("ema13", table.columnIndexOf("ema13")), Var.of("ema5", table.columnIndexOf("ema5")),

					Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
					Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"))

			// Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiffnorm")),
			// Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiffnorm"))

			), ISeq.of(ComplexVectorialGpOP.ADD, ComplexVectorialGpOP.DOT, ComplexVectorialGpOP.SUB,
					ComplexVectorialGpOP.DIV,
					//
					ComplexVectorialGpOP.LOG, ComplexVectorialGpOP.SQRT,
					//
//							ComplexVectorialGpOP.CONJUGATE,
//							ComplexVectorialGpOP.RECIPROCAL,
//							ComplexVectorialGpOP.ATAN,
//							ComplexVectorialGpOP.ACOS,
//							ComplexVectorialGpOP.ASIN,
//
//							ComplexVectorialGpOP.TANH,
//							ComplexVectorialGpOP.COSH,
//							ComplexVectorialGpOP.SINH,

					ComplexVectorialGpOP.SIN, ComplexVectorialGpOP.COS, ComplexVectorialGpOP.TAN,

//							ComplexVectorialGpOP.CUM_SUM,
//							ComplexVectorialGpOP.CUM_DIV,
					ComplexVectorialGpOP.CUM_MEAN,
//							ComplexVectorialGpOP.CUM_PROD,
//							ComplexVectorialGpOP.CUM_SUB,
//							//							
//							ComplexVectorialGpOP.PROD,
//							ComplexVectorialGpOP.MEAN,
//							ComplexVectorialGpOP.SUM,
					//
					ComplexVectorialGpOP.NEG,
					//
					ComplexVectorialGpOP.GT_THEN_REAL, ComplexVectorialGpOP.GT_THEN_COMPLEX), 3,
					(t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProfitSeekingVGP standartConfsVgpRowNorm(Market m) {
		try {
			log.info("Initializing table...");
			RowNormalizationVectorTable table = new RowNormalizationVectorTable(m, TimeFrame.D,
					LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2023, 1, 1, 0, 0), VEC_SIZE, VECTOR_NORMALIZER);

			ColumnUtil.addEma(table, "close", 200, VEC_SIZE, "ema200");
			ColumnUtil.addEma(table, "close", 50, VEC_SIZE, "ema50");
			ColumnUtil.addEma(table, "close", 13, VEC_SIZE, "ema13");
			ColumnUtil.addEma(table, "close", 5, VEC_SIZE, "ema5");
			ColumnUtil.addEma(table, "volume", 50, VEC_SIZE, "volEma50");
			ColumnUtil.addRsi(table, "close", VEC_SIZE, true);

			ColumnUtil.add(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema5")).last()
					- row.get(table.columnIndexOf("ema13")).last(), "smallEmaDiff");
			ColumnUtil.add(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema50")).last()
					- row.get(table.columnIndexOf("ema200")).last(), "bigEmaDiff");

			table.removeRows(0, 200);
			table.normalize();
			log.info("Initilized table.");

			return new ProfitSeekingVGP(table, ISeq.of(Const.of(Vector.of(0)), Const.of(Vector.of(1)),
					Const.of(Vector.of(-1)), Var.of("rsi", table.columnIndexOf("rsi")),
					Var.of("closeNorm", table.columnIndexOf("closeNorm")),
					Var.of("close", table.columnIndexOf("close")), Var.of("vol", table.columnIndexOf("volume")),
					Var.of("profitPercentage", table.getColumns().size()),
					Var.of("ema200", table.columnIndexOf("ema200")), Var.of("ema50", table.columnIndexOf("ema50")),
					Var.of("ema13", table.columnIndexOf("ema13")), Var.of("ema5", table.columnIndexOf("ema5")),
					Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
					Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"))),
					ISeq.of(VectorialGpOP.ADD, VectorialGpOP.DOT, VectorialGpOP.SUB, VectorialGpOP.DIV,
							VectorialGpOP.CUM_SUM, VectorialGpOP.CUM_DIV, VectorialGpOP.CUM_MEAN,
							VectorialGpOP.CUM_PROD, VectorialGpOP.CUM_SUB, VectorialGpOP.MAX, VectorialGpOP.MIN,
							VectorialGpOP.PROD, VectorialGpOP.MEAN, VectorialGpOP.NEG, VectorialGpOP.SIN,
							VectorialGpOP.COS, VectorialGpOP.TAN, VectorialGpOP.GT_THEN, VectorialGpOP.STD_VAR),
					3, (t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true, 1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProfitSeekingVGP standartConfs(Market m) {
		try {
			log.info("Initializing table...");
			VectorTable table = new VectorTable(m, TimeFrame.D, LocalDateTime.of(2015, 1, 1, 0, 0),
					LocalDateTime.of(2023, 1, 1, 0, 0), VEC_SIZE, NORMALIZER);

			ColumnUtil.addEma(table, "closeNorm", 200, VEC_SIZE, "ema200");
			ColumnUtil.addEma(table, "closeNorm", 50, VEC_SIZE, "ema50");
			ColumnUtil.addEma(table, "closeNorm", 13, VEC_SIZE, "ema13");
			ColumnUtil.addEma(table, "closeNorm", 5, VEC_SIZE, "ema5");
			ColumnUtil.addEma(table, "volumeNorm", 50, VEC_SIZE, "volEma50");
			ColumnUtil.addRsi(table, "close", VEC_SIZE, true);
			;

			ColumnUtil.add(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema5")).last()
					- row.get(table.columnIndexOf("ema13")).last(), "smallEmaDiff");
			ColumnUtil.add(table, VEC_SIZE, (row, index) -> row.get(table.columnIndexOf("ema50")).last()
					- row.get(table.columnIndexOf("ema200")).last(), "bigEmaDiff");

			table.removeRows(0, 200);
			log.info("Initilized table.");

			return new ProfitSeekingVGP(table, ISeq.of(Const.of(Vector.of(0)), Const.of(Vector.of(1)),
					Const.of(Vector.of(-1)), Var.of("rsi", table.columnIndexOf("rsi")),
					Var.of("normClose", table.columnIndexOf("closeNorm")),
					Var.of("normVol", table.columnIndexOf("volumeNorm")),
					Var.of("profitPercentage", table.getColumns().size()),
					Var.of("ema200", table.columnIndexOf("ema200")), Var.of("ema50", table.columnIndexOf("ema50")),
					Var.of("ema13", table.columnIndexOf("ema13")), Var.of("ema5", table.columnIndexOf("ema5")),
					Var.of("smallEmaDiff", table.columnIndexOf("smallEmaDiff")),
					Var.of("bigEmaDiff", table.columnIndexOf("bigEmaDiff"))),
					ISeq.of(VectorialGpOP.ADD, VectorialGpOP.DOT, VectorialGpOP.SUB, VectorialGpOP.DIV,
//							VectorialGpOP.CUM_SUM,
//							VectorialGpOP.CUM_DIV,
							VectorialGpOP.CUM_MEAN,
//							VectorialGpOP.CUM_PROD,
//							VectorialGpOP.CUM_SUB,
//							VectorialGpOP.MAX,
//							VectorialGpOP.MIN,
							VectorialGpOP.PROD, VectorialGpOP.MEAN, VectorialGpOP.NEG, VectorialGpOP.SIN,
							VectorialGpOP.COS, VectorialGpOP.TAN, VectorialGpOP.GT_THEN, VectorialGpOP.STD_VAR),
					3, (t -> t.gene().depth() < DEPTH && t.gene().size() < SIZE), true, 1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
