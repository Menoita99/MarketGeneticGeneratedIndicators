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

	private Rsi rsi;

	public VgpRsiTrendForecast(
			int depth, 
			ISeq<Op<Vector>> operations,
			ISeq<Op<Vector>> terminals, 
			Predicate<? super ProgramChromosome<Vector>> validator,
					Table<Vector> table) {
		super(table, terminals, operations, depth, validator);
	}




	@Override
	protected void init(Table<Vector> table) {
		super.init(table);
		rsi  = new Rsi();
		table.createValueFrom((row,index)->{
			Vector closeArr = row.get(table.columnIndexOf("close"));
			float output = 0;

			if(index == 0) 
				for (int i = 0; i < closeArr.getArr().length; i++)
					output = rsi.apply(new Double[] {(double)closeArr.getArr()[i]}).floatValue();
			else
				output = rsi.apply(new Double[] {(double)closeArr.last()}).floatValue();

			return Vector.of(output);
		}, "rsi");
	}




	@Override
	public Vector calculateExpectedValue(List<Vector> row, Integer index) {
		if(index + 1 < table.getHBuffer().size()) {
			double current = row.get(table.columnIndexOf("rsi")).asMeanScalar();
			double next = table.getHBuffer().get(index+1).get(table.columnIndexOf("rsi")).asMeanScalar();
			double forecast = next - current;
			return Vector.of(calculateAgentExpectedValue(Vector.of(forecast)));
		}
		return Vector.of(0);
	}




	@Override
	public Double calculateAgentExpectedValue(Vector agentOutput) {
		double forecast = agentOutput.asMeanScalar();
		if(Double.isInfinite(forecast) || Double.isNaN(forecast))
			return forecast;
		return forecast > 0 ? 1D : forecast < 0 ? -1D : 0D;
	}




	//	@Override
	//	public double calculateError(Vector forecastArr, Vector expectedArr) {
	//		double forecast = forecastArr.asMeanScalar();
	//		double expected = expectedArr.asMeanScalar();
	//		return !Double.isInfinite(forecast) && !Double.isNaN(forecast) ? LossFunction.mse(new Double[] {forecast}, new Double[] {expected}) : 100;
	//	}




	@Override
	public double calculateError(Vector forecastArr, Vector expectedArr) {
		double expected = calculateAgentExpectedValue(expectedArr);
		double forecast = calculateAgentExpectedValue(forecastArr);
		if(Double.isInfinite(forecast) || Double.isNaN(forecast))
			return 10;
		return  forecast == expected ? 0 : 1;
	}
}
