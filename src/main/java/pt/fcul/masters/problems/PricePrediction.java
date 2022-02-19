package pt.fcul.masters.problems;

import java.util.List;
import java.util.function.Predicate;

import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.table.Table;

public class PricePrediction extends RegressionGpProblem {

	public PricePrediction( 
			int depth,
			ISeq<Op<Double>> operations,
			ISeq<Op<Double>> terminals, 
			Predicate<? super ProgramChromosome<Double>> validator,
			Table<Double> table) {
		super(table, terminals, operations, depth, validator);
	}
	

	@Override
	public Double calculateExpectedValue(List<Double> row, Integer index) {
		if(index + getGap() < table.getHBuffer().size()) {
			double current = row.get(table.columnIndexOf("close"));
			double next = table.getHBuffer().get(index+getGap()).get(table.columnIndexOf("close"));
			double forecast = next - current;
			return calculateAgentExpectedValue(forecast);
		}
		return 0D;
	}

	
	@Override
	public Double calculateAgentExpectedValue(double agentOutput) {
		return agentOutput;
	}  
}
