package pt.fcul.master.stvgp.problems;

import java.util.function.Function;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.util.ISeq;
import pt.fcul.master.stvgp.StvgpChromosome;
import pt.fcul.master.stvgp.StvgpGene;
import pt.fcul.master.stvgp.StvgpType;
import pt.fcul.master.stvgp.op.StvgpEphemeralConst;
import pt.fcul.master.stvgp.op.StvgpOp;
import pt.fcul.master.stvgp.op.StvgpOps;
import pt.fcul.masters.vgp.util.Vector;

public class ProfitSeekingStvgp implements Problem<Tree<StvgpOp, ?>, StvgpGene, Double>{

	private static final int VECTOR_SIZE = 0;

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
								ISeq.of(StvgpOps.AND, StvgpOps.OR, StvgpOps.XOR,StvgpOps.MEAN_GT,StvgpOps.CUM_MEAN_GT,
										StvgpOps.IF_ELSE,StvgpOps.NOT,StvgpOps.SUM_GT),
								ISeq.of(StvgpOps.ADD,StvgpOps.SUB,StvgpOps.ABS,StvgpOps.ACOS,StvgpOps.ASIN,StvgpOps.ATAN
										,StvgpOps.COS,StvgpOps.CUM_SUM,StvgpOps.DIV,StvgpOps.DOT,StvgpOps.IMAX,StvgpOps.L1_NORM
										,StvgpOps.L2_NORM,StvgpOps.LOG,StvgpOps.MAX,StvgpOps.MIN,StvgpOps.PROD,StvgpOps.RES
										,StvgpOps.SIN,StvgpOps.SUM,StvgpOps.TAN,StvgpOps.VECT_IF_ELSE),
								ISeq.of(StvgpOps.TRUE,StvgpOps.FALSE),
								ISeq.of(StvgpEphemeralConst.of(() -> StvgpType.of(Vector.random(VECTOR_SIZE))))
						)
				),
				Genotype::gene
			);
	}
}
