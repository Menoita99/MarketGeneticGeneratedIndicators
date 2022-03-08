package pt.fcul.master.stvgp;

import static java.lang.String.format;

import java.util.Random;

import io.jenetics.ext.TreeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import pt.fcul.master.stvgp.op.StvgpOp;

public class StvgpSingleNodeCrossover 
	<G extends StvgpGene,C extends Comparable<? super C>> 
	extends TreeCrossover<StvgpGene, C>{

	public StvgpSingleNodeCrossover(double probability) {
		super(probability);
	}

	public StvgpSingleNodeCrossover() {
		this(DEFAULT_ALTER_PROBABILITY);
	}

	@Override
	protected <A> int crossover(final TreeNode<A> that, final TreeNode<A> other) {
		return swap(that, other);
	}

	// The static method makes it easier to test.
	static <A> int swap(final TreeNode<A> that, final TreeNode<A> other) {
		assert that != null;
		assert other != null;

		final Random random = RandomRegistry.random();

		final ISeq<TreeNode<A>> seq1 = that.breadthFirstStream().collect(ISeq.toISeq());
		ISeq<TreeNode<A>> seq2 = other.breadthFirstStream().collect(ISeq.toISeq());

		final int changed;
		if (seq1.length() > 1 && seq2.length() > 1) {
			final TreeNode<A> n1 = seq1.get(random.nextInt(seq1.length() - 1) + 1);
			final TreeNode<A> p1 = n1.parent().orElseThrow(AssertionError::new);

			StvgpOp.Type n1Type = StvgpOp.getOpType((StvgpOp)n1.value());
			seq2 = seq2.stream().filter(node -> n1Type == StvgpOp.getOpType((StvgpOp)node.value())).collect(ISeq.toISeq());
			
			if(seq2.isEmpty() || (seq2.size() == 1 && seq2.get(0).parent().isEmpty()))
				return 0;
			
			final TreeNode<A> n2 = seq2.size() > 1 ? seq2.get(random.nextInt(seq2.length() - 1) + 1) : seq2.get(0);
			final TreeNode<A> p2 = n2.parent().orElseThrow(AssertionError::new);

			final int i1 = p1.indexOf(n1);
			final int i2 = p2.indexOf(n2);

			p1.insert(i1, n2.detach());
			p2.insert(i2, n1.detach());

			changed = 2;
		} else {
			changed = 0;
		}

		return changed;
	}

	@Override
	public String toString() {
		return format("StvgpSingleNodeCrossover[%f]", _probability);
	}
}
