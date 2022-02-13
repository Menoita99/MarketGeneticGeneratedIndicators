package pt.fcul.masters.problems;

import java.util.List;
import java.util.function.Predicate;

import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.memory.Table;

public class RsiTrendForecast extends RegressionGpProblem{

	public RsiTrendForecast(Table<Double> table, 
			ISeq<Op<Double>> terminals, 
			ISeq<Op<Double>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<Double>> validator) {
		super(table, terminals, operations, depth, validator);
	}

	@Override
	public Double calculateExpectedValue(List<Double> row, Integer index) {
		if(index + 1 < table.getHBuffer().size()) {
			double current = row.get(table.columnIndexOf("close"));
			double next = table.getHBuffer().get(index+1).get(table.columnIndexOf("close"));
			double forecast = next - current;
			return forecast > 0 ? 1D : forecast < 0 ? -1D : 0D;
		}
		return 0D;
	}
}
