package pt.fcul.masters.vgp.problems;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.nd4j.linalg.api.ndarray.INDArray;

import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;

public class RegressionVectorialGpProblem implements GpProblem<INDArray> {

	@Override
	public Function<Tree<Op<INDArray>, ?>, Double> fitness() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Codec<Tree<Op<INDArray>, ?>, ProgramGene<INDArray>> codec() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<INDArray>, ?> agent, boolean useTrainSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISeq<Op<INDArray>> operations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISeq<Op<INDArray>> terminals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<INDArray> getTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EngineConfiguration getConf() {
		// TODO Auto-generated method stub
		return null;
	}

}
