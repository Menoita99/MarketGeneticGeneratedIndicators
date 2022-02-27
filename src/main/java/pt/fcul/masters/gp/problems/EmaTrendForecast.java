package pt.fcul.masters.gp.problems;

import java.util.List;
import java.util.function.Predicate;

import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.table.column.EmaColumn;

public class EmaTrendForecast extends RegressionGpProblem{

	private static final int PERIOD = 21;
	private EmaColumn emaColumn;

	public EmaTrendForecast( 
			int depth,
			ISeq<Op<Double>> operations,
			ISeq<Op<Double>> terminals, 
			Predicate<? super ProgramChromosome<Double>> validator,
			Table<Double> table) {
		super(table, terminals, operations, depth, validator);
	}
	
	@Override
	protected void init(Table<Double> table) {
		emaColumn = new EmaColumn(table.columnIndexOf("close"),PERIOD);
		emaColumn.addColumn(table);
		table.removeRows(emaColumn.toRemove());
	}

	@Override
	public Double calculateExpectedValue(List<Double> row, Integer index) {
		if(index + 1 < table.getHBuffer().size()) {
			double current = row.get(table.columnIndexOf(emaColumn.columnName()));
			double next = table.getHBuffer().get(index+1).get(table.columnIndexOf(emaColumn.columnName()));
			double forecast = next - current;
			return calculateAgentExpectedValue(forecast);
		}
		return 0D;
	}

	@Override
	public Double calculateAgentExpectedValue(double agentOutput) {
		return agentOutput > 0 ? 1D : agentOutput < 0 ? -1D : 0D;
	}  
	

	
//	@Override
//	public double calculateError(double forecast, double expected) {
//		forecast = calculateAgentExpectedValue(forecast);
//		return  forecast == expected ? 0 : 1;
//	}

}
