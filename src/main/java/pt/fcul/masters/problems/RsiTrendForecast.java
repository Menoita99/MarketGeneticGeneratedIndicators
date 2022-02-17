package pt.fcul.masters.problems;

import java.util.List;
import java.util.function.Predicate;

import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.table.column.RsiColumn;

public class RsiTrendForecast extends RegressionGpProblem{

	public RsiTrendForecast( 
			int depth,
			ISeq<Op<Double>> operations,
			ISeq<Op<Double>> terminals, 
			Predicate<? super ProgramChromosome<Double>> validator,
			Table<Double> table) {
		super(table, terminals, operations, depth, validator);
	}  

	
	
	@Override
	public Double calculateExpectedValue(List<Double> row, Integer index) {
		if(index + 1 < table.getHBuffer().size()) {
			double current = row.get(table.columnIndexOf("rsi"));
			double next = table.getHBuffer().get(index+1).get(table.columnIndexOf("rsi"));
			double forecast = next - current;
			return calculateAgentExpectedValue(forecast);
		}
		return 0D;
	}

	
	
	@Override
	public Double calculateAgentExpectedValue(double forecast) {
		return forecast > 0 ? 1D : forecast < 0 ? -1D : 0D;
	}

	
	
	@Override
	protected void init(Table<Double> table) {
		RsiColumn rsicolumn = new RsiColumn(table.columnIndexOf("close"));
		rsicolumn.addColumn(table);
		table.removeRows(rsicolumn.toRemove());
	}
}
