package pt.fcul.masters.vgp.problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.vgp.util.Vector;

@Log
@Data
public abstract class RegressionVectorialGpProblem  implements GpProblem<Vector> {

	private int gap = 1;
	protected Table<Vector> table;
	protected ISeq<Op<Vector>> terminals;
	protected ISeq<Op<Vector>> operations;
	protected int depth;
	protected Predicate<? super ProgramChromosome<Vector>> validator;

	public RegressionVectorialGpProblem(Table<Vector> table, 
			ISeq<Op<Vector>> terminals, 
			ISeq<Op<Vector>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<Vector>> validator) {
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

	protected void init(Table<Vector> table) {}
	
	
	/**
	 * This method defines how to create the column with the name EXPECTED that will be used 
	 * in the fitness function to calculate de error
	 */
	public abstract Vector calculateExpectedValue(List<Vector> row, Integer index) ;

	
	/**
	 * This value will be used to calculate the confidence of the agent.
	 */
	public abstract Double calculateAgentExpectedValue(Vector agentOutput);

	@Override
	public Function<Tree<Op<Vector>, ?>, Double> fitness() {
		return (agent) -> { 
			Pair<Integer, Integer> data =  getTable().getTrainSet();
			double error = 0;
//			long start = System.currentTimeMillis();
//			System.out.println(agent.size());
			for(int i = data.key(); i < data.value() - gap; i++ ) {
				List<Vector> row = getTable().getRow(i);
			
				Vector forecast = Program.eval(agent, row.toArray(new Vector[row.size()]));
				Vector expected = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
				
				error += calculateError(forecast,expected);
			}
//			System.out.println("Time consumed for "+(data.value()-data.key())+" " + (System.currentTimeMillis()-start));
			return error;
		};
	}

	@Override
	public Codec<Tree<Op<Vector>, ?>, ProgramGene<Vector>> codec() {
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
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<Vector>, ?> agent, boolean useTrainSet) {
		Pair<Integer, Integer> data = useTrainSet ? getTable().getTrainSet() : getTable().getValidationSet();
		
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.EXPECTED_OUTPUT, new LinkedList<>(),
				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		double errorSum = 0;
		for(int i = data.key(); i < data.value() - gap; i++ ) {
			List<Vector> row = getTable().getRow(i);
			Vector forecast = Program.eval(agent, row.toArray(new Vector[row.size()]));
			Vector expected = getTable().getRow(i+gap).get(getTable().columnIndexOf("EXPECTED"));
			double error = calculateError(forecast,expected);
			
			errorSum=+error;
			output.get(ValidationMetric.FITNESS).add(errorSum);
			output.get(ValidationMetric.AGENT_OUTPUT).add((double)forecast.mean().getArr()[0]);
			output.get(ValidationMetric.EXPECTED_OUTPUT).add((double) expected.mean().getArr()[0]);
			output.get(ValidationMetric.CONFIDENCE).add(Math.abs(forecast.mean().getArr()[0] - calculateAgentExpectedValue(forecast)));
		}
		return output;
	}
	
	public double calculateError(Vector forecastArr, Vector expectedArr) {
		double forecast = forecastArr.mean().getArr()[0];
		double expected = expectedArr.mean().getArr()[0];
		return !Double.isInfinite(forecast) && !Double.isNaN(forecast) ? LossFunction.mse(new Double[] {forecast}, new Double[] {expected}) : 10;
	}

	@Override
	public ISeq<Op<Vector>> operations() {
		return operations;
	}

	@Override
	public ISeq<Op<Vector>> terminals() {
		return terminals;
	}

	@Override
	public Table<Vector> getTable() {
		return table;
	}

	@Override
	public EngineConfiguration getConf() {
		return new EngineConfiguration();
	}

}
