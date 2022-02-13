package pt.fcul.masters.problems;

import java.util.List;
import java.util.Map;

import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.memory.Table;

/**
 * 
 * @author Rui Menoita
 * @param <T> the type of input variable to the model
 * 
 */
public interface GpProblem<T> extends Problem<Tree<Op<T>, ?>, ProgramGene<T>, Double> {
	
	Map<ValidationMetric,List<Double>> validate(Tree<Op<T>, ?> agent , boolean useTrainSet);

	ISeq<Op<T>> operations();

	ISeq<Op<T>> terminals();
	
	Table<T> getTable();

	EngineConfiguration getConf();
}
