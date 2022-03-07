package pt.fcul.master.stvgp;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.ext.AbstractTreeChromosome;
import io.jenetics.ext.util.FlatTreeNode;
import io.jenetics.ext.util.Tree;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.op.Op;
import io.jenetics.util.ISeq;
import pt.fcul.master.stvgp.op.StvgpOp;

public class StvgpChromosome 
	extends AbstractTreeChromosome<StvgpOp, StvgpGene>
	implements Function<StvgpType[], StvgpType>
{

	private static final long serialVersionUID = 1L;

	private final Predicate<? super StvgpChromosome> _validator;

	private final ISeq<StvgpOp> operationsBoolean;
	private final ISeq<StvgpOp> operationsRelational;
	private final ISeq<StvgpOp> operationsVectorial;
	private final ISeq<StvgpOp> terminalBoolean;
	private final ISeq<StvgpOp> terminalVectorial;

	/**
	 * Create a new program chromosome from the given program genes. This
	 * constructor assumes that the given {@code program} is valid. Since the
	 * program validation is quite expensive, the validity check is skipped in
	 * this constructor.
	 *
	 * @param program the program. During the program evolution, newly created
	 *        program trees has the same <em>depth</em> than this tree.
	 * @param validator the chromosome validator. A typical validator would
	 *        check the size of the tree and if the tree is too large, mark it
	 *        at <em>invalid</em>. The <em>validator</em> may be {@code null}.
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IllegalArgumentException if either the {@code operations} or
	 *         {@code terminals} sequence is empty
	 */
	protected StvgpChromosome(
			final ISeq<StvgpGene> program,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		super(program);
		_validator = requireNonNull(validator);
		
		this.operationsBoolean = requireNonNull(operationsBoolean);
		this.operationsRelational = requireNonNull(operationsRelational);
		this.operationsVectorial = requireNonNull(operationsVectorial);
		this.terminalBoolean = requireNonNull(terminalBoolean);
		this.terminalVectorial = requireNonNull(terminalVectorial);

		if (operationsBoolean.isEmpty()) {
			throw new IllegalArgumentException("No operations given.");
		}
		
		if (operationsRelational.isEmpty()) {
			throw new IllegalArgumentException("No operations given");
		}
		
		if (operationsVectorial.isEmpty()) {
			throw new IllegalArgumentException("No operations given.");
		}
		
		if (terminalBoolean.isEmpty()) {
			throw new IllegalArgumentException("No terminals given");
		}

		if (terminalVectorial.isEmpty()) {
			throw new IllegalArgumentException("No terminals given.");
		}
	}

	/**
	 * Return the allowed operations.
	 *
	 * @since 5.0
	 *
	 * @return the allowed operations
	 */
	public ISeq<StvgpOp> operations() {
		return ISeq.concat(operationsVectorial,ISeq.concat(operationsBoolean, operationsRelational));
	}

	/**
	 * Return the allowed terminal operations.
	 *
	 * @since 5.0
	 *
	 * @return the allowed terminal operations
	 */
	public ISeq<StvgpOp> terminals() {
		return ISeq.concat(terminalBoolean, terminalVectorial);
	}

	@Override
	public boolean isValid() {
		if (_valid == null) {
			_valid = _validator.test(this);
		}

		return _valid;
	}

	private boolean isSuperValid() {
		return super.isValid();
	}

	/**
	 * Evaluates the root node of this chromosome.
	 *
	 * @see StvgpGene#apply(Object[])
	 * @see StvgpChromosome#eval(Object[])
	 *
	 * @param args the input variables
	 * @return the evaluated value
	 * @throws NullPointerException if the given variable array is {@code null}
	 */
	@Override
	public StvgpType apply(final StvgpType[] args) {
		return root().apply(args);
	}

	/**
	 * Evaluates the root node of this chromosome.
	 *
	 * @see StvgpGene#eval(Object[])
	 * @see StvgpChromosome#apply(Object[])
	 *
	 * @param args the function arguments
	 * @return the evaluated value
	 * @throws NullPointerException if the given variable array is {@code null}
	 */
	@SafeVarargs
	public final StvgpType eval(final StvgpType... args) {
		return root().eval(args);
	}

	@Override
	public StvgpChromosome newInstance(final ISeq<StvgpGene> genes) {
		return create(genes, _validator, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	@Override
	public StvgpChromosome newInstance() {
		return create(root().depth(), _validator, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	/**
	 * Create a new chromosome from the given operation tree (program).
	 *
	 * @param program the operation tree
	 * @param validator the chromosome validator. A typical validator would
	 *        check the size of the tree and if the tree is too large, mark it
	 *        at <em>invalid</em>. The <em>validator</em> may be {@code null}.
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @param the operation type
	 * @return a new chromosome from the given operation tree
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the given operation tree is invalid,
	 *         which means there is at least one node where the operation arity
	 *         and the node child count differ.
	 */
	public static StvgpChromosome of(
			final Tree<StvgpOp, ?> program,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		StvgpProgram.check(program);
		
		checkOperations(operationsBoolean);
		checkOperations(operationsRelational);
		checkOperations(operationsVectorial);
		checkTerminals(terminalBoolean);
		checkTerminals(terminalVectorial);

		return create(program, validator, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	// Create the chromosomes without checks.
	private static StvgpChromosome create(
			final Tree<StvgpOp, ?> program,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		final ISeq<StvgpGene> genes = FlatTreeNode.ofTree(program)
				.stream()
				.map(n -> new StvgpGene(n.value(), n.childOffset(), operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial))
				.collect(ISeq.toISeq());

		return new StvgpChromosome(genes, validator,operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	private static void checkOperations(final ISeq<? extends Op<?>> operations) {
		final ISeq<?> terminals = operations.stream()
				.filter(Op::isTerminal)
				.collect(ISeq.toISeq());

		if (!terminals.isEmpty()) {
			throw new IllegalArgumentException(format(
					"Operations must not contain terminals: %s",
					terminals.toString(",")
					));
		}
	}

	private static void checkTerminals(final ISeq<? extends Op<?>> terminals) {
		final ISeq<?> operations = terminals.stream()
				.filter(op -> !op.isTerminal())
				.collect(ISeq.toISeq());

		if (!operations.isEmpty()) {
			throw new IllegalArgumentException(format(
					"Terminals must not contain operations: %s",
					operations.toString(",")
					));
		}
	}

	/**
	 * Create a new chromosome from the given operation tree (program).
	 *
	 * @param program the operation tree
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @param the operation type
	 * @return a new chromosome from the given operation tree
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the given operation tree is invalid,
	 *         which means there is at least one node where the operation arity
	 *         and the node child count differ.
	 */
	@SuppressWarnings("unchecked")
	public static StvgpChromosome of(
			final Tree<StvgpOp, ?> program,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		return of(program,(Predicate<? super StvgpChromosome> & Serializable) StvgpChromosome::isSuperValid,
				operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	/**
	 * Create a new program chromosome with the defined depth. This method will
	 * create a <em>full</em> program tree.
	 *
	 * @param depth the depth of the created program tree
	 * @param validator the chromosome validator. A typical validator would
	 *        check the size of the tree and if the tree is too large, mark it
	 *        at <em>invalid</em>. The <em>validator</em> may be {@code null}.
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @param the operation type
	 * @return a new program chromosome from the given (flattened) program tree
	 * @throws NullPointerException if one of the parameters is {@code null}
	 * @throws IllegalArgumentException if the {@code depth} is smaller than zero
	 */
	public static StvgpChromosome of(
			final int depth,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		checkOperations(operationsBoolean);
		checkOperations(operationsRelational);
		checkOperations(operationsVectorial);
		checkTerminals(terminalBoolean);
		checkTerminals(terminalVectorial);
		return create(depth, validator,operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	private static StvgpChromosome create(
			final int depth,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		TreeNode<StvgpOp> program = StvgpProgram.of(depth, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
		return create(program,	validator,operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	/**
	 * Create a new program chromosome with the defined depth. This method will
	 * create a <em>full</em> program tree.
	 *
	 * @param depth the depth of the created (full) program tree
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @param the operation type
	 * @return a new program chromosome from the given (flattened) program tree
	 * @throws NullPointerException if one of the parameters is {@code null}
	 * @throws IllegalArgumentException if the {@code depth} is smaller than zero
	 */
	@SuppressWarnings("unchecked")
	public static StvgpChromosome of(
			final int depth,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		return of(
				depth,
				(Predicate<? super StvgpChromosome> & Serializable)StvgpChromosome::isSuperValid,
				operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	/**
	 * Create a new program chromosome from the given (flattened) program tree.
	 * This method doesn't make any assumption about the validity of the given
	 * operation tree. If the tree is not valid, it will repair it. This
	 * behaviour allows the <em>safe</em> usage of all existing alterer.
	 *
	 * <pre>{@code
	 * final StvgpChromosome<Double> ch = StvgpChromosome.of(
	 *     genes,
	 *     // If the program has more that 200 nodes, it is marked as "invalid".
	 *     ch -> ch.length() <= 200,
	 *     operations,
	 *     terminals
	 * );
	 * }</pre>
	 *
	 * @param genes the program genes
	 * @param validator the chromosome validator to use
	 * @param operations the allowed non-terminal operations
	 * @param terminals the allowed terminal operations
	 * @param the operation type
	 * @return a new program chromosome from the given (flattened) program tree
	 * @throws NullPointerException if one of the parameters is {@code null}
	 */
	public static StvgpChromosome of(
			final ISeq<StvgpGene> genes,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		final TreeNode<StvgpOp> program = StvgpProgram.toTree(genes, terminalBoolean, terminalVectorial);
		return of(program, validator, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	private static StvgpChromosome create(
			final ISeq<StvgpGene> genes,
			final Predicate<? super StvgpChromosome> validator,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		final TreeNode<StvgpOp> program = StvgpProgram.toTree(genes, terminalBoolean, terminalVectorial);
		return create(program, validator, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}

	public static StvgpChromosome of(
			final ISeq<StvgpGene> genes,
			final ISeq<StvgpOp> operationsBoolean,
			final ISeq<StvgpOp> operationsRelational,
			final ISeq<StvgpOp> operationsVectorial,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		return of(genes, StvgpChromosome::isSuperValid,operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial);
	}


	/* *************************************************************************
	 *  Java object serialization
	 * ************************************************************************/

//	private Object writeReplace() {
//		return new Serial(Serial.PROGRAM_CHROMOSOME, this);
//	}

//	private void readObject(final ObjectInputStream stream)
//			throws InvalidObjectException
//	{
//		throw new InvalidObjectException("Serialization proxy required.");
//	}
//
//	void write(final ObjectOutput out) throws IOException {
//		writeInt(length(), out);
//		out.writeObject(_operations);
//		out.writeObject(_terminals);
//
//		for (StvgpGene gene : _genes) {
//			out.writeObject(gene.allele());
//			writeInt(gene.childOffset(), out);
//		}
//	}
//
//	@SuppressWarnings({"unchecked", "rawtypes"})
//	static StvgpChromosome read(final ObjectInput in)
//			throws IOException, ClassNotFoundException
//	{
//		final var length = readInt(in);
//		final var operations = (ISeq)in.readObject();
//		final var terminals = (ISeq)in.readObject();
//
//		final MSeq genes = MSeq.ofLength(length);
//		for (int i = 0; i < genes.length(); ++i) {
//			final Op op = (Op)in.readObject();
//			final int childOffset = readInt(in);
//			genes.set(i, new StvgpGene((StvgpOp) op, childOffset, operations, terminals));
//		}
//
//		return StvgpChromosome.of(genes.toISeq(), operations, terminals);
//	}

}
