package pt.fcul.masters.gp.problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.prog.regression.LossFunction;
import io.jenetics.util.ISeq;
import lombok.Data;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Pair;

@Data
public abstract class RegressionGpProblem  implements GpProblem<Double>{

	private int gap = 1;
	protected Table<Double> table;
	protected ISeq<Op<Double>> terminals;
	protected ISeq<Op<Double>> operations;
	protected int depth;
	protected Predicate<? super ProgramChromosome<Double>> validator;

	public RegressionGpProblem(Table<Double> table, 
			ISeq<Op<Double>> terminals, 
			ISeq<Op<Double>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<Double>> validator) {
		this.table = table;
		this.terminals = terminals;
		this.operations = operations;
		this.depth = depth;
		this.validator = validator;
		init(table);
		table.createValueFrom(this::calculateExpectedValue, "EXPECTED");
		table.removeRows(table.getHBuffer().size()-getGap(), table.getHBuffer().size());
	}

	protected void init(Table<Double> table) {}
	
	
	/**
	 * This method defines how to create the column with the name EXPECTED that will be used 
	 * in the fitness function to calculate de error
	 */
	public abstract Double calculateExpectedValue(List<Double> row, Integer index) ;

	
	/**
	 * This value will be used to calculate the confidence of the agent.
	 */
	public abstract Double calculateAgentExpectedValue(double agentOutput);

	@Override
	public Function<Tree<Op<Double>, ?>, Double> fitness() {
		return (agent) -> { 
			Pair<Integer, Integer> data =  getTable().getTrainSet();
		
			double[] forecast = new double[data.value() - gap - data.key()];
			double[] expected = new double[data.value() - gap - data.key()];
			
			for(int i = data.key(); i < data.value() - gap; i++ ) {
				List<Double> row = getTable().getRow(i);

				forecast[i - data.key()] = Program.eval(agent, row.toArray(new Double[row.size()]));
				expected[i - data.key()] = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
			}
			return calculateError(forecast,expected);
		};
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
				Genotype::gene);
	}

	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<Double>, ?> agent, boolean useTrainSet) {
		Pair<Integer, Integer> data = useTrainSet ? getTable().getTrainSet() : getTable().getValidationSet();
		
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.EXPECTED_OUTPUT, new LinkedList<>(),
				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		
		double[] forecast = new double[data.value() - gap - data.key()];
		double[] expected = new double[data.value() - gap - data.key()];
		
		for(int i = data.key(); i < data.value() - gap; i++ ) {
			List<Double> row = getTable().getRow(i);
			Double agentOutput = Program.eval(agent, row.toArray(new Double[row.size()]));
			
			forecast[i - data.key()]  = agentOutput;
			expected[i - data.key()] = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
			
			output.get(ValidationMetric.AGENT_OUTPUT).add(agentOutput);
			output.get(ValidationMetric.EXPECTED_OUTPUT).add(expected[i - data.key()]);
			output.get(ValidationMetric.CONFIDENCE).add(Math.abs(agentOutput - calculateAgentExpectedValue(agentOutput)));
		}
		output.get(ValidationMetric.FITNESS).add(calculateError(forecast,expected));
		
		return output;
	}


	public double calculateError(double[] forecastArr, double[] expectedArr) {
		return LossFunction.mse(ArrayUtils.toObject(forecastArr), ArrayUtils.toObject(expectedArr));
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