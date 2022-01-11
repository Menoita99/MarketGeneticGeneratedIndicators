package pt.fcul.masters;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.memory.MemoryManager;
import pt.fcul.masters.statefull.op.Ema;
import pt.fcul.masters.statefull.op.VectorCandle;

@Data
public class GPTrendForecast implements Problem<Tree<Op<Double>, ?>, ProgramGene<Double>, Double>{


	private int depth;
	private ISeq<Op<Double>> operations;
	private ISeq<Op<Double>> terminals;
	private Predicate<? super ProgramChromosome<Double>> validator;

	private MemoryManager memory;


	public GPTrendForecast(int depth,ISeq<Op<Double>> operations,ISeq<Op<Double>> terminals,Predicate<? super ProgramChromosome<Double>> validator,MemoryManager memory) {
		this.depth = depth;
		this.operations = operations;
		this.terminals = terminals;
		this.validator = validator;
		this.memory = memory;

		memory.createValueFrom((row,index)->{
			if(index + 1 < memory.getHBuffer().size()) {
				double current = row.get(memory.columnIndexOf("close"));
				double next = memory.getHBuffer().get(index+1).get(memory.columnIndexOf("close"));
				double d = ((next/current) - 1 ) * 100;
				return d;
			}
			return 0D;
		}, "ROI"); // add roi to the table
	}




	public static void addEmas(MemoryManager memory) {
		Ema ema5 = new Ema(5);
		memory.createValueFrom(row -> ema5.apply(new Double[] {row.get(memory.columnIndexOf("close"))}),ema5.name());
		
		Ema ema13 = new Ema(13);
		memory.createValueFrom(row -> ema13.apply(new Double[] {row.get(memory.columnIndexOf("close"))}),ema13.name());
		
		Ema ema50 = new Ema(50);
		memory.createValueFrom(row -> ema50.apply(new Double[] {row.get(memory.columnIndexOf("close"))}),ema50.name());
		
		Ema ema200 = new Ema(200);
		memory.createValueFrom(row -> ema200.apply(new Double[] {row.get(memory.columnIndexOf("close"))}),ema200.name());
		
		Ema ema800 = new Ema(800);
		memory.createValueFrom(row -> ema800.apply(new Double[] {row.get(memory.columnIndexOf("close"))}),ema800.name());
	}




	//TODO don't count last one and give time to init ema ops
	//TODO implement normalization
	//TODO implement moving window
	//TODO implement outlier detection
	@Override
	public Function<Tree<Op<Double>, ?>, Double> fitness() {
//		return (agent) -> discountedAccuracy(agent,true);
		return (agent) -> accuracy(agent,true);
	}




	public Double validate(Tree<Op<Double>, ?> agent) {
//		return discountedAccuracy(agent,false);
		return accuracy(agent,false);
	}



	public Double discountedAccuracy(Tree<Op<Double>, ?> agent, boolean isTrain) {
		double accuracy = 0;
		Pair<Integer, Integer> data = isTrain ? memory.getTrainSet() : memory.getValidationSet();
		
		for(int i = data.key();i<data.value(); i++ ) {
			List<Double> row = memory.getHBuffer().get(i);
			
			double roi = row.get(memory.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));
			
			accuracy += forecast == 0 || Double.isNaN(forecast) ? 0 : ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : -1;
		}

		return Math.abs(accuracy);
	}




	public Double accuracy(Tree<Op<Double>, ?> agent, boolean isTrain) {
		double accuracy = 0;
		Pair<Integer, Integer> data = isTrain ? memory.getTrainSet() : memory.getValidationSet();
		
		for(int i = data.key();i<data.value(); i++ ) {
			List<Double> row = memory.getHBuffer().get(i);
			
			double roi = row.get(memory.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));

			accuracy += ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : 0;
		}

		return accuracy/((double)data.value()-(double)data.key());
	}




	private Double[] getArgs(List<Double> row) {
//		return new Double[] {
//				row.get(memory.columnIndexOf("low")),
//				row.get(memory.columnIndexOf("open")),
//				row.get(memory.columnIndexOf("close")),
//				row.get(memory.columnIndexOf("high"))
//		};
		return row.toArray(new Double[row.size()]);
	}




	@Override
	public Codec<Tree<Op<Double>, ?>, ProgramGene<Double>> codec() {
		return Codec.of(
				Genotype.of(
						ProgramChromosome.of(
								depth,
								validator,
								operations,
								terminals
								)
						),
				Genotype::gene
				);
	}



	/**
	 * | output | expected | fitness | 
	 * @param agent
	 * @return
	 */
	public List<Double[]> getBehaviorData(Tree<Op<Double>, ?> agent) {
		List<Double[]> data = new LinkedList<>();
		double accuracy = 0;
		double i = 0;
		for(List<Double> row : memory.getHBuffer()) {
			i++;
			double roi = row.get(memory.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));
			accuracy += ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : 0;
			data.add(new Double[] {forecast,roi,accuracy/i});
		}

		return data;
	}


	/**
	 * | output | expected | fitness | 
	 * @param agent
	 * @return
	 */
	public List<Double[]> getBehaviorData(Tree<Op<Double>, ?> agent, boolean isTrain) {
		List<Double[]> output = new LinkedList<>();
		double accuracy = 0;
		Pair<Integer, Integer> data = isTrain ? memory.getTrainSet() : memory.getValidationSet();
		
		for(int i = data.key();i<data.value(); i++ ) {
			List<Double> row = memory.getHBuffer().get(i);
			
			double roi = row.get(memory.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));
			accuracy += ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : 0;
			output.add(new Double[] {forecast,roi,accuracy/memory.getHBuffer().size()});
		}
		return output;
	}



	public static GPTrendForecast standartConfs() {
		MemoryManager memory = new MemoryManager(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0));
		GPTrendForecast.addEmas(memory);
		VectorCandle vc = new VectorCandle();
		memory.createValueFrom(row -> (double)vc.getVectorCandle(row.get(memory.columnIndexOf("high")), row.get(memory.columnIndexOf("low")), row.get(memory.columnIndexOf("volume"))), "vc");
		return new GPTrendForecast(
				5, 
				ISeq.of(MathOp.EXP,MathOp.LOG,
						MathOp.COS,MathOp.SIN,MathOp.TAN,
						MathOp.ADD,MathOp.SUB,MathOp.MUL,MathOp.DIV)
				, 
				ISeq.of(
						EphemeralConst.of(() -> (double)RandomRegistry.random().nextDouble()*10),
						Var.of("open", memory.columnIndexOf("open")),
						Var.of("high", memory.columnIndexOf("high")),
						Var.of("low",  memory.columnIndexOf("low")),
						Var.of("close", memory.columnIndexOf("close")),
						Var.of("Ema5", memory.columnIndexOf("Ema[5]")),
						Var.of("Ema13", memory.columnIndexOf("Ema[13]")),
						Var.of("Ema50", memory.columnIndexOf("Ema[50]")),
						Var.of("Ema200", memory.columnIndexOf("Ema[200]")),
						Var.of("Ema800", memory.columnIndexOf("Ema[800]")),
						Var.of("vc", memory.columnIndexOf("vc")))
				, 
				t -> t.gene().depth() < 10,//t -> t.gene().depth() < 17),t -> t.gene().size() < 100
				memory);
	}
}
