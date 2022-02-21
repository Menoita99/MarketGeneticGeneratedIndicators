package pt.fcul.masters.vgp.problems;

import java.util.List;
import java.util.function.Predicate;


import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.gp.op.statefull.Rsi;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.vgp.util.Vector;

public class VgpRsiTrendForecast  extends RegressionVectorialGpProblem {

	public VgpRsiTrendForecast(
			int depth, 
			ISeq<Op<Vector>> operations,
			ISeq<Op<Vector>> terminals, 
			Predicate<? super ProgramChromosome<Vector>> validator,
			Table<Vector> table) {
		super(table, terminals, operations, depth, validator);
	}

	@Override
	public Vector calculateExpectedValue(List<Vector> row, Integer index) {
		Vector closeArr = row.get(table.columnIndexOf("close"));
		Rsi rsi = new Rsi();
		float output = 0;
		
		if(!rsi.getGains().isFull()) 
			for (int i = 0; i < closeArr.getArr().length; i++)
				output = rsi.apply(new Double[] {(double)closeArr.getArr()[i]}).floatValue();
		else
			output = rsi.apply(new Double[] {(double)closeArr.getArr()[(closeArr.getArr().length-1)]}).floatValue();
		
		return Vector.of(output);
	}

	@Override
	public Double calculateAgentExpectedValue(Vector agentOutput) {
		double forecast = agentOutput.mean().getArr()[0];
		return forecast;// > 0 ? 1D : forecast < 0 ? -1D : 0D;
	}
//	
//	
//	
//	@Override
//	public double calculateError(Vector forecastArr, Vector expectedArr) {
//		double expected = calculateAgentExpectedValue(expectedArr);
//		double forecast = calculateAgentExpectedValue(forecastArr);
//		return  forecast == expected ? 0 : 1;
//	}
}
