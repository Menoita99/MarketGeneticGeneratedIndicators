package pt.fcul.master.stvgp.problems;

import java.util.List;
import java.util.Map;

import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.util.ISeq;
import pt.fcul.master.stvgp.StvgpGene;
import pt.fcul.master.stvgp.StvgpType;
import pt.fcul.master.stvgp.op.StvgpOp;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;

public interface StvgpProblem extends Problem<Tree<StvgpOp, ?>, StvgpGene, Double> {
	
	Map<ValidationMetric,List<Double>> validate(Tree<StvgpOp, ?> agent , boolean useTrainSet);

	ISeq<StvgpOp> operations();

	ISeq<StvgpOp> terminals();
	
	Table<StvgpType> getTable();

	EngineConfiguration getConf();
}
