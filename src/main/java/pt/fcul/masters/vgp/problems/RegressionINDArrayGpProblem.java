package pt.fcul.masters.vgp.problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.nd4j.linalg.api.ndarray.INDArray;

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
import lombok.extern.java.Log;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Pair;

@Data
@Log
public abstract class RegressionINDArrayGpProblem implements GpProblem<INDArray> {

	private int gap = 1;
	protected Table<INDArray> table;
	protected ISeq<Op<INDArray>> terminals;
	protected ISeq<Op<INDArray>> operations;
	protected int depth;
	protected Predicate<? super ProgramChromosome<INDArray>> validator;

	public RegressionINDArrayGpProblem(Table<INDArray> table, 
			ISeq<Op<INDArray>> terminals, 
			ISeq<Op<INDArray>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<INDArray>> validator) {
		this.table = table;
		this.terminals = terminals;
		this.operations = operations;
		this.depth = depth;
		this.validator = validator;
		init(table);
		table.createValueFrom(this::calculateExpectedValue, "EXPECTED");
		table.removeRows(table.getHBuffer().size()-getGap(), table.getHBuffer().size());
		log.info("Iniciatized problem");
	}

	protected void init(Table<INDArray> table) {}
	
	
	/**
	 * This method defines how to create the column with the name EXPECTED that will be used 
	 * in the fitness function to calculate de error
	 */
	public abstract INDArray calculateExpectedValue(List<INDArray> row, Integer index) ;

	
	/**
	 * This value will be used to calculate the confidence of the agent.
	 */
	public abstract Double calculateAgentExpectedValue(INDArray agentOutput);


	@Override
	public Function<Tree<Op<INDArray>, ?>, Double> fitness() {
		return (agent) -> { 
			Pair<Integer, Integer> data =  getTable().getTrainSet();
		
			INDArray[] forecast = new INDArray[data.value() - gap - data.key()];
			INDArray[] expected = new INDArray[data.value() - gap - data.key()];
			
			for(int i = data.key(); i < data.value() - gap; i++ ) {
				List<INDArray> row = getTable().getRow(i);

				forecast[i - data.key()] = Program.eval(agent, row.toArray(new INDArray[row.size()]));
				expected[i - data.key()] = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
			}
			return calculateError(forecast,expected);
		};
	}

	@Override
	public Codec<Tree<Op<INDArray>, ?>, ProgramGene<INDArray>> codec() {
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
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<INDArray>, ?> agent, boolean useTrainSet) {
		Pair<Integer, Integer> data = useTrainSet ? getTable().getTrainSet() : getTable().getValidationSet();
		
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.EXPECTED_OUTPUT, new LinkedList<>(),
				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		
		INDArray[] forecast = new INDArray[data.value() - gap - data.key()];
		INDArray[] expected = new INDArray[data.value() - gap - data.key()];
		
		for(int i = data.key(); i < data.value() - gap; i++ ) {
			List<INDArray> row = getTable().getRow(i);
			INDArray agentOutput = Program.eval(agent, row.toArray(new INDArray[row.size()]));
			
			if(agentOutput == null)
				throw new IllegalStateException("");
			
			forecast[i - data.key()]  = agentOutput;
			expected[i - data.key()] = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
			
			output.get(ValidationMetric.AGENT_OUTPUT).add(agentOutput.meanNumber().doubleValue());
			output.get(ValidationMetric.EXPECTED_OUTPUT).add(expected[i - data.key()].meanNumber().doubleValue());
			output.get(ValidationMetric.CONFIDENCE).add(Math.abs(agentOutput.meanNumber().doubleValue() - calculateAgentExpectedValue(agentOutput)));
		}
		output.get(ValidationMetric.FITNESS).add(calculateError(forecast,expected));
		
		return output;
	}


	public double calculateError(INDArray[] forecast2, INDArray[] expected2) {
		Double[] forecast = new Double[expected2.length];
		Double[] expected = new Double[expected2.length];
		
		for (int i = 0; i < expected2.length; i++) {
			forecast[i] = Double.valueOf(forecast2[i].meanNumber().doubleValue());
			expected[i] = Double.valueOf(expected2[i].meanNumber().doubleValue());
		}
		
		return LossFunction.mse(forecast, expected);
	}

	@Override
	public ISeq<Op<INDArray>> operations() {
		return operations;
	}

	@Override
	public ISeq<Op<INDArray>> terminals() {
		return terminals;
	}

	@Override
	public Table<INDArray> getTable() {
		return table;
	}
}
