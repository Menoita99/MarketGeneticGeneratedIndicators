package pt.fcul.masters.gp.problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.prog.regression.LossFunction;
import io.jenetics.util.ISeq;
import lombok.Data;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.DoubleTable;
import pt.fcul.masters.table.Table;

@Data
public class GPTrendForecast implements Problem<Tree<Op<Double>, ?>, ProgramGene<Double>, Double>, GpProblem<Double>{


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





	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<Double>, ?> agent, boolean useTrainSet) {
		Pair<Integer, Integer> data = useTrainSet ? table.getTrainSet() : table.getValidationSet();
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.EXPECTED_OUTPUT, new LinkedList<>(),
				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		
		final int gap = 5;
		double mse = 0;

//		double lastForecast =  table.getHBuffer().get(0).get(table.columnIndexOf("normClose"));

		for(int i = data.key();i < data.value() - gap; i++ ) {
			List<Double> row = table.getHBuffer().get(i);
			double forecast = Program.eval(agent, getArgs(row));
			double expected = table.getHBuffer().get(i+gap).get(table.columnIndexOf("normClose"));
			mse += !Double.isInfinite(forecast) && !Double.isNaN(forecast) ?
					Math.abs(LossFunction.mse(new Double[] {forecast}, new Double[] {expected})) : 10;
			
//			if(!Double.isInfinite(forecast) && !Double.isNaN(lastForecast)) 
//				lastForecast = Math.abs(forecast);
			
			output.get(ValidationMetric.FITNESS).add(mse);
			output.get(ValidationMetric.AGENT_OUTPUT).add(forecast);
			output.get(ValidationMetric.EXPECTED_OUTPUT).add(expected);
			output.get(ValidationMetric.CONFIDENCE).add(Math.abs(forecast - toTrinary(forecast)));
		}
		return output;
	}






	private double toTrinary(double forecast) {
		return forecast > 0 ? 1 : forecast < 0 ? -1 : 0;
	}






	@Override
	public ISeq<Op<Double>> operations() {
		return operations;
	}






	@Override
	public ISeq<Op<Double>> terminals() {
		return terminals;
	}




	@Override
	public Table<Double> getTable() {
		return table;
	}
}
