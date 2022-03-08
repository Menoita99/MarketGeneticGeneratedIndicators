package pt.fcul.master.stvgp.problems;

import java.util.function.Function;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.Const;
import io.jenetics.util.ISeq;
import pt.fcul.master.stvgp.StvgpChromosome;
import pt.fcul.master.stvgp.StvgpGene;
import pt.fcul.master.stvgp.op.StvgpOp;
import pt.fcul.master.stvgp.op.StvgpOps;

public class ProfitSeekingStvgp implements Problem<Tree<StvgpOp, ?>, StvgpGene, Double>{

	@Override
	public Function<Tree<StvgpOp, ?>, Double> fitness() {
		return (agent) -> 0D;
	}

	@Override
	public Codec<Tree<StvgpOp, ?>, StvgpGene> codec() {
		return Codec.of(
				Genotype.of(
						StvgpChromosome.of(
								3,
								c -> false,
								ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR),
								ISeq.of(StvgpOps.MEAN_GT),
								ISeq.of(StvgpOps.ADD,StvgpOps.SUB),
								ISeq.of(StvgpOps.TRUE,StvgpOps.FALSE),
								ISeq.of(StvgpOps.randomVector(1))
						)
				),
				Genotype::gene
			);
	}
	
}
