package pt.fcul.master.stvgp;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;

import io.jenetics.Gene;
import io.jenetics.ext.AbstractTreeGene;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Program;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.Getter;

@Getter
public class StvgpGene extends 
	AbstractTreeGene<StvgpOp, StvgpGene>
	implements Gene<StvgpOp, StvgpGene>, Function<StvgpType[], StvgpType>{

	private static final long serialVersionUID = 1L;

	private final ISeq<StvgpOp> operationBoolean;
	private final ISeq<StvgpOp> operationRelational;
	private final ISeq<StvgpOp> operationVectorial;
	
	private final ISeq<StvgpOp> terminalsBoolean;
	private final ISeq<StvgpOp> terminalsVectorial;

	StvgpGene(final StvgpOp op,
			final int childOffset,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		super(requireNonNull(get(op)), childOffset, op.arity());
		
		this.operationBoolean = requireNonNull(operationsBoolean);
		this.operationRelational = requireNonNull(operationsRelational);
		this.operationVectorial = requireNonNull(operationsVectorial);
		this.terminalsBoolean = requireNonNull(terminalBoolean);
		this.terminalsVectorial = requireNonNull(terminalVectorial);
	}


	
	
	private static StvgpOp get(final StvgpOp op) {
		final StvgpOp instance = (StvgpOp) op.get();
		if (instance != op && instance.arity() != op.arity()) {
			throw new IllegalArgumentException(format("Original op and created op have different arity: %d != %d,",	instance.arity(), op.arity()));
		}
		return instance;
	}

	/**
	 * Evaluates this program gene (recursively) with the given variable values.
	 *
	 * @see ProgramGene#eval(Object[])
	 * @see ProgramChromosome#eval(Object[])
	 *
	 * @param args the input variables
	 * @return the evaluated value
	 * @throws NullPointerException if the given variable array is {@code null}
	 */
	@Override
	public StvgpType apply(final StvgpType[] args) {
		checkTreeState();
		return Program.eval(this, args);
	}

	/**
	 * Convenient method, which lets you apply the program function without
	 * explicitly create a wrapper array.
	 *
	 * @see ProgramGene#apply(Object[])
	 * @see ProgramChromosome#eval(Object[])
	 *
	 * @param args the function arguments
	 * @return the evaluated value
	 * @throws NullPointerException if the given variable array is {@code null}
	 */
	@SafeVarargs
	public final StvgpType eval(final StvgpType... args) {
		return apply(args);
	}


	/**
	 * Creates a new {@link TreeNode} from this program gene.
	 *
	 * @since 5.0
	 *
	 * @return a new tree node value build from this program gene
	 */
	public TreeNode<StvgpOp> toTreeNode() {
		return TreeNode.ofTree(this);
	}

	@Override
	public StvgpGene newInstance() {
		//TODO REVER THIS MAY BE SIMPLIER
		final Random random = RandomRegistry.random();

		StvgpOp operation = value();
		
		
		StvgpGene parent = parent().get();
		int index = calculateChildIndex(parent);  				//index of this gene int the parent
		int indexcerto = parent.childOffset() - childOffset();
		System.out.println("Debug 182 "+ indexcerto + " "+ index);
		StvgpType type = parent.allele().arityType()[index];	//the type that this object must have
		
		
		if (isLeaf()) {
			if(type.isBooleanType())
				operation = terminalsBoolean.get(random.nextInt(terminalsBoolean.length()));
			else
				operation = terminalsVectorial.get(random.nextInt(terminalsVectorial.length()));
		} else {
			
			ISeq<StvgpOp> operations = ISeq.empty();

			if(type.isBooleanType())
				operations = (ISeq<StvgpOp>) ISeq.concat(operationBoolean, operationRelational);
			else
				operations = (ISeq<StvgpOp>) operationVectorial;
			
			operations = operations.stream()
				.filter(op -> op.arity() == value().arity())
				.collect(ISeq.toISeq());
			
			if (operations.length() > 1) {
				operation = operations.get(random.nextInt(operations.length()));
			}
		}

		return newInstance(operation);
	}

	private int calculateChildIndex(StvgpGene parent) {
		Iterator<StvgpGene> iterator = parent.iterator();
		int index = 0;
		while(iterator.hasNext()) {
			if(iterator.next().equals(this))
				return index;
			index++;
		}
		return index;
	}



	/**
	 * Create a new program gene with the given operation.
	 *
	 * @param op the operation of the new program gene
	 * @return a new program gene with the given operation
	 * @throws NullPointerException if the given {@code op} is {@code null}
	 * @throws IllegalArgumentException if the arity of the given operation is
	 *         different from the arity of current operation. This restriction
	 *         ensures that only valid program genes are created by this method.
	 */
	@Override
	public StvgpGene newInstance(final StvgpOp op) {
		if (value().arity() != op.arity()) {
			throw new IllegalArgumentException(format(
				"New operation must have same arity: %s[%d] != %s[%d]",
				value().name(), value().arity(), op.name(), op.arity()
			));
		}
		return new StvgpGene(op, childOffset(),getOperationBoolean(),getOperationRelational(),getOperationVectorial(), getTerminalsBoolean(),getTerminalsVectorial());
	}




	/**
	 * Return a new program gene with the given operation and the <em>local</em>
	 * tree structure.
	 *
	 * @param op the new operation
	 * @param childOffset the offset of the first node child within the
	 *        chromosome
	 * @param childCount the number of children of the new tree gene
	 * @return a new tree gene with the given parameters
	 * @throws IllegalArgumentException  if the {@code childCount} is smaller
	 *         than zero
	 * @throws IllegalArgumentException if the operation arity is different from
	 *         the {@code childCount}.
	 * @throws NullPointerException if the given {@code op} is {@code null}
	 */
	@Override
	public StvgpGene newInstance(
		final StvgpOp op,
		final int childOffset,
		final int childCount) {
		
		if (op.arity() != childCount) {
			throw new IllegalArgumentException(format("Operation arity and child count are different: %d, != %d",op.arity(), childCount	));
		}

		return new StvgpGene(op, childOffset,getOperationBoolean(),getOperationRelational(),getOperationVectorial(), getTerminalsBoolean(),getTerminalsVectorial());
	}
	
	
}
