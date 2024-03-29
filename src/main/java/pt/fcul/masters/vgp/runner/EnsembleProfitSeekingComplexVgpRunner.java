package pt.fcul.masters.vgp.runner;

import static pt.fcul.masters.utils.Constants.EXECUTOR;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Const;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import lombok.extern.java.Log;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.ComplexVectorTable;
import pt.fcul.masters.vgp.op.ComplexVectorialGpOP;
import pt.fcul.masters.vgp.problems.EnsembleProfitSeekingComplexVGP;
import pt.fcul.masters.vgp.util.ComplexVector;

@Log
public class EnsembleProfitSeekingComplexVgpRunner {


	
	private static final int VECTOR_SIZE = 50;
	private static final EngineConfiguration<ProgramGene<ComplexVector>, Double> COMPLEX_VECTORIAL_CONF = EngineConfiguration.standart();




	public static void main(String[] args) {
		try {
//			COMPLEX_VECTORIAL_CONF.setMaxGenerations(100);
			EnsembleProfitSeekingComplexVGP problem = standartConfs();
			COMPLEX_VECTORIAL_CONF.setMaxPhenotypeAge(10);
			
			EvolutionResult<ProgramGene<ComplexVector>, Double> tree = Engine.builder(problem).maximizing()
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
			.peek(e-> System.out.println(e.generation()+" "+e.bestFitness()))
			.collect(EvolutionResult.toBestEvolutionResult());
			
			Genotype<ProgramGene<ComplexVector>> gt = tree.bestPhenotype().genotype();
			
			Map<ValidationMetric, List<Double>> validation = problem.validate(ISeq.of(gt.get(0).gene(),gt.get(1).gene()), false);
			
			Plotter.builder().multiLineChart("Price/Money", 
						Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
						Serie.of("Money", validation.get(ValidationMetric.MONEY))).build().plot();
			
			Plotter.builder().multiLineChart("Price/Transaction", 
					Serie.of("Price", validation.get(ValidationMetric.PRICE)), 
					Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION))).build().plot();
			
			Plotter.builder().multiLineChart("Money/Transaction", 
					Serie.of("Money", validation.get(ValidationMetric.MONEY)), 
					Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION))).build().plot();
			
		} finally {
			EXECUTOR.shutdown();
		}
	}

	
	
	
	
	private static EnsembleProfitSeekingComplexVGP standartConfs() {
		try {
			log.info("Initializing table...");
		//	ComplexVectorTable table = new ComplexVectorTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2012, 1, 1, 0, 0),VECTOR_SIZE, new DynamicDerivativeNormalizer(24*5*2));
			ComplexVectorTable table = ComplexVectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\EUR_USD H1 2012_1_1_ 0_0 VGP_50 DynamicDerivativeNormalizer_120.csv").toPath());
			log.info("Initilized table.");

//			ColumnUtil.addEma(table,"closeNorm",200,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",50,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",13,VECTOR_SIZE);
//			ColumnUtil.addEma(table,"closeNorm",5,VECTOR_SIZE);
//			ColumnUtil.addRsi(table,"close", VECTOR_SIZE);
//			table.removeRow(200);

			return new EnsembleProfitSeekingComplexVGP(
					table,
					ISeq.of(
							EphemeralConst.of(() -> ComplexVector.random(VECTOR_SIZE)),
							Const.of(ComplexVector.of(Math.PI)),
							Const.of(ComplexVector.of(Math.PI/2d)),
							Const.of(ComplexVector.of(3d*Math.PI/2d)),
							Const.of(ComplexVector.of(Math.E)),
							Var.of("normOpen", table.columnIndexOf("openNorm")),
							Var.of("normHigh", table.columnIndexOf("highNorm")),
							Var.of("normLow",  table.columnIndexOf("lowNorm")),
							Var.of("normClose", table.columnIndexOf("closeNorm")),
							Var.of("normVol", table.columnIndexOf("volumeNorm")),
							Var.of("ema200", table.columnIndexOf("ema200")),
							Var.of("ema50", table.columnIndexOf("ema50")),
							Var.of("ema13",  table.columnIndexOf("ema13")),
							Var.of("ema5", table.columnIndexOf("ema5"))
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
							ComplexVectorialGpOP.EXP,
							
							ComplexVectorialGpOP.LOG,
							ComplexVectorialGpOP.SQRT,
							ComplexVectorialGpOP.SQRT1Z,	
							
							ComplexVectorialGpOP.CONJUGATE,
							ComplexVectorialGpOP.RECIPROCAL,
//							ComplexVectorialGpOP.ONE_FIELD,
//							ComplexVectorialGpOP.ZERO_FIELD,
							
							ComplexVectorialGpOP.ATAN,
							ComplexVectorialGpOP.ACOS,
							ComplexVectorialGpOP.ASIN,
							
							ComplexVectorialGpOP.TANH,
							ComplexVectorialGpOP.COSH,
							ComplexVectorialGpOP.SINH,
							
							ComplexVectorialGpOP.SIN,
							ComplexVectorialGpOP.COS,
							ComplexVectorialGpOP.TAN,
							
							ComplexVectorialGpOP.CUM_SUM,
							ComplexVectorialGpOP.CUM_DIV,
							ComplexVectorialGpOP.CUM_MEAN,
							ComplexVectorialGpOP.CUM_PROD,
							ComplexVectorialGpOP.CUM_SUB,
							
							ComplexVectorialGpOP.MAX_ABS,
							ComplexVectorialGpOP.MAX_IMG,
							ComplexVectorialGpOP.MAX_PHI,
							ComplexVectorialGpOP.MAX_REAL,
							
							ComplexVectorialGpOP.MIN_ABS,
							ComplexVectorialGpOP.MIN_IMG,
							ComplexVectorialGpOP.MIN_PHI,
							ComplexVectorialGpOP.MIN_REAL,
							
							ComplexVectorialGpOP.PROD,
							ComplexVectorialGpOP.MEAN,
							ComplexVectorialGpOP.LAST,
							ComplexVectorialGpOP.FIRST,
							ComplexVectorialGpOP.SUM,
							
							ComplexVectorialGpOP.NEG,
							ComplexVectorialGpOP.ABS
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
