package pt.fcul.masters.vgp.problems;

import java.util.List;
import java.util.function.Predicate;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.gp.op.statefull.Rsi;
import pt.fcul.masters.table.Table;

public class INDArrayGpRsiTrendForecast extends RegressionINDArrayGpProblem {
	
	private Rsi rsi;
	
	public INDArrayGpRsiTrendForecast(
			int depth, 
			ISeq<Op<INDArray>> operations,
			ISeq<Op<INDArray>> terminals, 
			Predicate<? super ProgramChromosome<INDArray>> validator,
			Table<INDArray> table) {
		super(table, terminals, operations, depth, validator);
	}
	
	
	
	
	@Override
	protected void init(Table<INDArray> table) {
		super.init(table);
		rsi  = new Rsi();
	}
	
	
	

	@Override
	public INDArray calculateExpectedValue(List<INDArray> row, Integer index) {
		INDArray closeArr = row.get(table.columnIndexOf("close"));
		float output = 0;
		
		if(!rsi.getGains().isFull()) 
			for (int i = 0; i < closeArr.shape()[0]; i++)
				output = rsi.apply(new Double[] {(double)closeArr.getFloat(i)}).floatValue();
		else
			output = rsi.apply(new Double[] {(double)closeArr.getFloat(closeArr.shape()[0]-1)}).floatValue();
		
		return Nd4j.create(new float[]{output});
	}

	
	
	
	@Override
	public Double calculateAgentExpectedValue(INDArray agentOutput) {
		double forecast = agentOutput.meanNumber().doubleValue();
		return forecast;// > 0 ? 1D : forecast < 0 ? -1D : 0D;
	}
	
	
	
	
//	@Override
//	public double calculateError(INDArray forecastArr, INDArray expectedArr) {
//		double expected = calculateAgentExpectedValue(expectedArr);
//		double forecast = calculateAgentExpectedValue(forecastArr);
//		return  forecast == expected ? 0 : 1;
//	}
}
