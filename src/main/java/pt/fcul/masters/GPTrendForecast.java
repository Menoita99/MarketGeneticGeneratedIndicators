package pt.fcul.masters;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import io.jenetics.prog.regression.LossFunction;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.memory.DoubleTable;
import pt.fcul.masters.statefull.op.Ema;

@Data
public class GPTrendForecast implements Problem<Tree<Op<Double>, ?>, ProgramGene<Double>, Double>{


	private int depth;
	private ISeq<Op<Double>> operations;
	private ISeq<Op<Double>> terminals;
	private Predicate<? super ProgramChromosome<Double>> validator;

	private DoubleTable table;


	public GPTrendForecast(int depth,ISeq<Op<Double>> operations,ISeq<Op<Double>> terminals,Predicate<? super ProgramChromosome<Double>> validator,DoubleTable table) {
		this.depth = depth;
		this.operations = operations;
		this.terminals = terminals;
		this.validator = validator;
		this.table = table;

		table.createValueFrom((row,index)->{
			if(index + 1 < table.getHBuffer().size()) {
				double current = row.get(table.columnIndexOf("close"));
				double next = table.getHBuffer().get(index+1).get(table.columnIndexOf("close"));
				double d = ((next/current) - 1 ) * 100;
				return d;
			}
			return 0D;
		}, "ROI"); // add roi to the table
	}






	//TODO don't count last one and give time to init ema ops
	//TODO implement normalization
	//TODO implement moving window
	//TODO implement outlier detection
	@Override
	public Function<Tree<Op<Double>, ?>, Double> fitness() {
		//		return (agent) -> discountedAccuracy(agent,true);
		//		return (agent) -> accuracy(agent,true);
		return (agent) -> forecast(agent,true);
	}




	public Double validate(Tree<Op<Double>, ?> agent) {
		//		return discountedAccuracy(agent,false);
		//		return accuracy(agent,false);
		return forecast(agent,false);
	}



	public Double discountedAccuracy(Tree<Op<Double>, ?> agent, boolean isTrain) {
		double accuracy = 0;
		Pair<Integer, Integer> data = isTrain ? table.getTrainSet() : table.getValidationSet();

		for(int i = data.key();i<data.value(); i++ ) {
			List<Double> row = table.getHBuffer().get(i);

			double roi = row.get(table.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));

			accuracy += forecast == 0 || Double.isNaN(forecast) ? 0 : ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : -1;
		}

		return Math.abs(accuracy);
	}




	public Double accuracy(Tree<Op<Double>, ?> agent, boolean isTrain) {
		double accuracy = 0;
		Pair<Integer, Integer> data = isTrain ? table.getTrainSet() : table.getValidationSet();

		for(int i = data.key();i<data.value(); i++ ) {
			List<Double> row = table.getHBuffer().get(i);

			double roi = row.get(table.columnIndexOf("ROI"));
			double forecast = Program.eval(agent, getArgs(row));

			accuracy += ((forecast > 0 && roi > 0) || (forecast < 0 && roi < 0)) ? 1 : 0;
		}

		return accuracy/((double)data.value()-(double)data.key());
	}



	public Double forecast(Tree<Op<Double>, ?> agent, boolean isTrain) {
		Pair<Integer, Integer> data = isTrain ? table.getTrainSet() : table.getValidationSet();
		final int gap = 5;
		double mse = 0;

//		double lastForecast =  table.getHBuffer().get(0).get(table.columnIndexOf("normClose"));

		for(int i = data.key();i<data.value() - gap; i++ ) {
			List<Double> row = table.getHBuffer().get(i);
			double forecast = Program.eval(agent, getArgs(row));
			double expected = table.getHBuffer().get(i+gap).get(table.columnIndexOf("normClose"));
			mse += !Double.isInfinite(forecast) && !Double.isNaN(forecast) ?
					Math.abs(LossFunction.mse(new Double[] {forecast}, new Double[] {expected})) : 10;
			
//			if(!Double.isInfinite(forecast) && !Double.isNaN(lastForecast)) 
//				lastForecast = Math.abs(forecast);
		}
		return mse;
	}




	private Double[] getArgs(List<Double> row) {
		//		return new Double[] {
		//				row.get(table.columnIndexOf("low")),
		//				row.get(table.columnIndexOf("open")),
		//				row.get(table.columnIndexOf("close")),
		//				row.get(table.columnIndexOf("high"))
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
	public List<Double[]> getAccuracyBehaviorData(Tree<Op<Double>, ?> agent) {
		List<Double[]> data = new LinkedList<>();
		double accuracy = 0;
		double i = 0;
		for(List<Double> row : table.getHBuffer()) {
			i++;
			double roi = row.get(table.columnIndexOf("ROI"));
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
	public List<Double[]> getForecastBehaviorData(Tree<Op<Double>, ?> agent) {
		List<Double[]> data = new LinkedList<>();
		double accuracy = 0;
		final int gap = 5;
		
		for(int i = 0; i < table.getHBuffer().size()-gap; i++) {
			double actualValue = table.getHBuffer().get(i).get(table.columnIndexOf("normClose"));
			double futureValue = table.getHBuffer().get(i+gap).get(table.columnIndexOf("normClose"));
			double forecast = Program.eval(agent, getArgs(table.getHBuffer().get(i)));

			accuracy += ((actualValue > futureValue && actualValue > forecast) || (actualValue < futureValue && actualValue < forecast)) ? 1 : 0;
			data.add(new Double[] {forecast,futureValue,accuracy/i});
		}

		return data;
	}





	public static GPTrendForecast standartConfs() {
		DoubleTable table = new DoubleTable(Market.EUR_USD,TimeFrame.H1,LocalDateTime.of(2015, 1, 1, 0, 0));
		//		GPTrendForecast.addEmas(table);
		//		VectorCandle vc = new VectorCandle();
		//		table.createValueFrom(row -> (double)vc.getVectorCandle(row.get(table.columnIndexOf("high")), row.get(table.columnIndexOf("low")), row.get(table.columnIndexOf("volume"))), "vc");

		List<Double> closeColumn = new ArrayList<>();
		List<Double> openColumn = new ArrayList<>();
		List<Double> lowColumn = new ArrayList<>();
		List<Double> highColumn = new ArrayList<>();
		List<Double> volumeColumn = new ArrayList<>();

		table.foreach(row -> {
			closeColumn.add(row.get(table.columnIndexOf("close")));
			openColumn.add(row.get(table.columnIndexOf("open")));
			lowColumn.add(row.get(table.columnIndexOf("low")));
			highColumn.add(row.get(table.columnIndexOf("high")));
			volumeColumn.add(row.get(table.columnIndexOf("volume")));
		});

		Normalizer normalizer = new DynamicStepNormalizer(2500);
		
		table.addColumn(normalizer.apply(closeColumn), "normClose");
		table.addColumn(normalizer.apply(openColumn), "normOpen");
		table.addColumn(normalizer.apply(lowColumn), "normLow");
		table.addColumn(normalizer.apply(highColumn), "normHigh");
		table.addColumn(normalizer.apply(volumeColumn), "normVol");

		Ema ema5 = new Ema(5);
		table.createValueFrom(row -> ema5.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema5.name());

		Ema ema13 = new Ema(13);
		table.createValueFrom(row -> ema13.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema13.name());

		Ema ema50 = new Ema(50);
		table.createValueFrom(row -> ema50.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema50.name());

		Ema ema200 = new Ema(200);
		table.createValueFrom(row -> ema200.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema200.name());

		//		Ema ema800 = new Ema(800);
		//		table.createValueFrom(row -> ema800.apply(new Double[] {row.get(table.columnIndexOf("normClose"))}),ema800.name());


		return new GPTrendForecast(
				6, 
				ISeq.of(MathOp.EXP,MathOp.LOG,
						MathOp.COS,MathOp.SIN,MathOp.TAN,
						MathOp.ADD,MathOp.SUB,MathOp.MUL,MathOp.DIV,
						new Ema())
				, 
				ISeq.of(
						EphemeralConst.of(() -> (double)RandomRegistry.random().nextDouble()*10),
						Var.of("normOpen", table.columnIndexOf("normOpen")),
						Var.of("normHigh", table.columnIndexOf("normHigh")),
						Var.of("normLow",  table.columnIndexOf("normLow")),
						Var.of("normClose", table.columnIndexOf("normClose")),
						Var.of("normVol", table.columnIndexOf("normVol")),
						//						Var.of("open", table.columnIndexOf("open")),
						//						Var.of("high", table.columnIndexOf("high")),
						//						Var.of("low",  table.columnIndexOf("low")),
						//						Var.of("close", table.columnIndexOf("close"))
						Var.of("Ema5", table.columnIndexOf("Ema[5]")),
						Var.of("Ema13", table.columnIndexOf("Ema[13]")),
						Var.of("Ema50", table.columnIndexOf("Ema[50]"))
						//						Var.of("Ema200", table.columnIndexOf("Ema[200]")),
						//						Var.of("Ema800", table.columnIndexOf("Ema[800]"))
						//						Var.of("vc", table.columnIndexOf("vc"))
						)
				, 
				t -> t.gene().size() < 100,//> t.gene().depth() < 13,//t -> t.gene().depth() < 17),t -> 
				table);
	}
}
