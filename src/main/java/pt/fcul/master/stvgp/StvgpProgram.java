package pt.fcul.master.stvgp;

import static io.jenetics.internal.util.Hashes.hash;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Random;

import io.jenetics.ext.util.FlatTree;
import io.jenetics.ext.util.Tree;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

public class StvgpProgram implements StvgpOp, Serializable {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final Tree<? extends StvgpOp, ?> _tree;

	/**
	 * Create a new program with the given name and the given operation tree.
	 * The arity of the program is calculated from the given operation tree and
	 * set to the maximal arity of the operations of the tree.
	 *
	 * @param name the program name
	 * @param tree the operation tree
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the given operation tree is invalid,
	 *         which means there is at least one node where the operation arity
	 *         and the node child count differ.
	 */
	public StvgpProgram(final String name, final Tree<? extends StvgpOp, ?> tree) {
		_name = requireNonNull(name);
		_tree = requireNonNull(tree);
		check(tree);
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public int arity() {
		return 0;
	}
	

	@Override
	public StvgpType[] arityType() {
		return tree().root().value().arityType();
	}

	@Override
	public StvgpType outputType() {
		return tree().root().value().outputType();
	}

	/**
	 * Return the underlying expression tree.
	 *
	 * @since 4.1
	 *
	 * @return the underlying expression tree
	 */
	public Tree<StvgpOp, ?> tree() {
		return TreeNode.ofTree(_tree);
	}

	@Override
	public StvgpType apply(final StvgpType[] args) {
		if (args.length < arity()) {
			throw new IllegalArgumentException(format(
				"Arguments length is smaller than program arity: %d < %d",
				args.length, arity()
			));
		}

		return eval(_tree, args);
	}

	/**
	 * Convenient method, which lets you apply the program function without
	 * explicitly create a wrapper array.
	 *
	 * @see #apply(Object[])
	 *
	 * @param args the function arguments
	 * @return the evaluated value
	 * @throws NullPointerException if the given variable array is {@code null}
	 * @throws IllegalArgumentException if the length of the arguments array
	 *         is smaller than the program arity
	 */
	@SafeVarargs
	public final StvgpType eval(final StvgpType... args) {
		return apply(args);
	}

	@Override
	public int hashCode() {
		return hash(_name, hash(_tree));
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this ||
			obj instanceof Program &&
			Objects.equals(((StvgpProgram)obj)._name, _name) &&
			Objects.equals(((StvgpProgram)obj)._tree, _tree);
	}

	@Override
	public String toString() {
		return _name;
	}


	/* *************************************************************************
	 * Static helper methods.
	 * ************************************************************************/

	/**
	 * Evaluates the given operation tree with the given variables.
	 *
	 * @param <T> the argument type
	 * @param tree the operation tree
	 * @param variables the input variables
	 * @return the result of the operation tree evaluation
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the length of the variable array
	 *         is smaller than the program arity
	 */
	@SafeVarargs
	public static StvgpType eval(
		final Tree<? extends StvgpOp, ?> tree,
		final StvgpType... variables
	) {
		requireNonNull(tree);
		requireNonNull(variables);

		final StvgpOp op = tree.value();
		return op.isTerminal()
			? evalOp(op, variables)
			: evalOp(op, evalChildren(tree, variables));
	}

	private static StvgpType evalOp(final StvgpOp op, final StvgpType... variables) {
//		if (op instanceof Var && ((Var)op).index() >= variables.length) {
//			throw new IllegalArgumentException(format(
//				"No value for variable '%s' given.", op
//			));
//		}

		return op.apply(variables);
	}

	@SafeVarargs
	private static StvgpType[] evalChildren(
		final Tree<? extends StvgpOp, ?> node,
		final StvgpType... variables
	) {
		final StvgpType[] result = newArray(variables.getClass(), node.childCount());
		for (int i = 0; i < node.childCount(); ++i) {
			result[i] = eval(node.childAt(i), variables);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(final Class<?> arrayType, final int size) {
		return (T[])Array.newInstance(arrayType.getComponentType(), size);
	}

	/**
	 * Validates the given program tree.
	 *
	 * @param program the program to validate
	 * @throws NullPointerException if the given {@code program} is {@code null}
	 * @throws IllegalArgumentException if the given operation tree is invalid,
	 *         which means there is at least one node where the operation arity
	 *         and the node child count differ.
	 */
	public static void check(final Tree<? extends Op<?>, ?> program) {
		program.forEach(StvgpProgram::checkArity);
	}

	private static void checkArity(final Tree<? extends Op<?>, ?> node) {
		if (node.value() != null &&
			node.value().arity() != node.childCount())
		{
			throw new IllegalArgumentException(format(
				"Op arity != child count: %d != %d",
				node.value().arity(), node.childCount()
			));
		}
	}

	/**
	 * Create a new, random program from the given (non) terminal operations
	 * with the desired depth. The created program tree is a <em>full</em> tree.
	 *
	 * @since 4.1
	 *
	 * @param name the program name
	 * @param depth the desired depth of the program tree
	 * @param operations the list of <em>non</em>-terminal operations
	 * @param terminals the list of terminal operations
	 * @param the operational type
	 * @return a new program
	 * @throws NullPointerException if one of the given operations is
	 *        {@code null}
	 * @throws IllegalArgumentException if the given tree depth is smaller than
	 *         zero
	 */
	public static StvgpProgram of(
		final String name,
		final int depth,
		final ISeq<StvgpOp> operationsBoolean,
		final ISeq<StvgpOp> operationsRelational,
		final ISeq<StvgpOp> operationsVectorial,
		final ISeq<StvgpOp> terminalBoolean,
		final ISeq<StvgpOp> terminalVectorial
	) {
		return new StvgpProgram(name, of(depth, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial));
	}

	/**
	 * Create a new, random program from the given (non) terminal operations
	 * with the desired depth. The created program tree is a <em>full</em> tree.
	 *
	 * @since 4.1
	 *
	 * @param name the program name
	 * @param depth the desired depth of the program tree
	 * @param operations the list of <em>non</em>-terminal operations
	 * @param terminals the list of terminal operations
	 * @param random the random engine used for creating the program
	 * @param the operational type
	 * @return a new program
	 * @throws NullPointerException if one of the given operations is
	 *        {@code null}
	 * @throws IllegalArgumentException if the given tree depth is smaller than
	 *         zero
	 */
	public static StvgpProgram of(
		final String name,
		final int depth,
		final ISeq<StvgpOp> operationsBoolean,
		final ISeq<StvgpOp> operationsRelational,
		final ISeq<StvgpOp> operationsVectorial,
		final ISeq<StvgpOp> terminalBoolean,
		final ISeq<StvgpOp> terminalVectorial,
		final Random random
	) {
		return new StvgpProgram(name, of(depth,operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial, random));
	}

	/**
	 * Create a new, random program tree from the given (non) terminal
	 * operations with the desired depth. The created program tree is a
	 * <em>full</em> tree.
	 *
	 * @param depth the desired depth of the program tree
	 * @param operations the list of <em>non</em>-terminal operations
	 * @param terminals the list of terminal operations
	 * @param the operational type
	 * @return a new program tree
	 * @throws NullPointerException if one of the given operations is
	 *        {@code null}
	 * @throws IllegalArgumentException if the given tree depth is smaller than
	 *         zero
	 */
	public static TreeNode<StvgpOp> of(
		final int depth,
		final ISeq<StvgpOp> operationsBoolean,
		final ISeq<StvgpOp> operationsRelational,
		final ISeq<StvgpOp> operationsVectorial,
		final ISeq<StvgpOp> terminalBoolean,
		final ISeq<StvgpOp> terminalVectorial) {
		return of(depth, operationsBoolean,operationsRelational,operationsVectorial, terminalBoolean,terminalVectorial, RandomRegistry.random());
	}

	/**
	 * Create a new, random program tree from the given (non) terminal
	 * operations with the desired depth. The created program tree is a
	 * <em>full</em> tree.
	 *
	 * @since 4.1
	 *
	 * @param depth the desired depth of the program tree
	 * @param operations the list of <em>non</em>-terminal operations
	 * @param terminals the list of terminal operations
	 * @param random the random engine used for creating the program
	 * @param the operational type
	 * @return a new program tree
	 * @throws NullPointerException if one of the given operations is
	 *        {@code null}
	 * @throws IllegalArgumentException if the given tree depth is smaller than
	 *         zero
	 */
	public static TreeNode<StvgpOp> of(
		final int depth,
		final ISeq<StvgpOp> operationsBoolean,
		final ISeq<StvgpOp> operationsRelational,
		final ISeq<StvgpOp> operationsVectorial,
		final ISeq<StvgpOp> terminalBoolean,
		final ISeq<StvgpOp> terminalVectorial,
		final Random random) {
		if (depth < 0)
			throw new IllegalArgumentException("Tree depth is smaller than zero: " + depth);
		
		if (!operationsBoolean.forAll(o -> !o.isTerminal() && StvgpOp.getOpType(o) == Type.BOOLEAN))
			throw new IllegalArgumentException("Operation list contains terminal op.");
		
		if (!operationsRelational.forAll(o -> !o.isTerminal() && StvgpOp.getOpType(o) == Type.RELATIONAL))
			throw new IllegalArgumentException("Operation list contains terminal op.");
		
		if (!operationsVectorial.forAll(o -> !o.isTerminal() && StvgpOp.getOpType(o) == Type.VECTORIAL))
			throw new IllegalArgumentException("Operation list contains terminal op.");
		
		if (!terminalBoolean.forAll(o -> o.isTerminal() && StvgpOp.getOpType(o) == Type.BOOLEAN))
			throw new IllegalArgumentException("Terminal list contains non-terminal op.");
		
		if (!terminalVectorial.forAll(o -> o.isTerminal() && StvgpOp.getOpType(o) == Type.VECTORIAL))
			throw new IllegalArgumentException("Terminal list contains non-terminal op.");

		
		ISeq<StvgpOp> operationsBooleanRelational = ISeq.concat(operationsBoolean, operationsRelational);

		TreeNode<StvgpOp> root = TreeNode.of(operationsBooleanRelational.get(random.nextInt(operationsBooleanRelational.size())));//get boolean root
		
		fill(depth, root, operationsBooleanRelational, operationsVectorial, terminalBoolean, terminalVectorial, random);
		
		return root;
	}

	/**
	 * Root must be a boolean 
	 */
	private static void fill(
						int level,
						TreeNode<StvgpOp> node, 
						ISeq<StvgpOp> operationsBooleanRelational,
						ISeq<StvgpOp> operationsVectorial,
						ISeq<StvgpOp> terminalBoolean,
						ISeq<StvgpOp> terminalVectorial,
						Random random) {
		
		StvgpOp op = node.value();
		
		if(level == 0) {
			for(StvgpType arityType : op.arityType()) { // fill leafs
					node.attach(arityType.isBooleanType() ?
						TreeNode.of(terminalBoolean.get(random.nextInt(terminalBoolean.size()))) :
						TreeNode.of(terminalVectorial.get(random.nextInt(terminalVectorial.size()))));
			}
		}else {
			for(StvgpType arityType : op.arityType()) { //add ops
				TreeNode<StvgpOp> newNode = arityType.isBooleanType() ?
							TreeNode.of(operationsBooleanRelational.get(random.nextInt(operationsBooleanRelational.size()))) :
							TreeNode.of(operationsVectorial.get(random.nextInt(operationsVectorial.size())));
				
				node.attach(newNode);
				fill(level -1,newNode,operationsBooleanRelational,operationsVectorial,terminalBoolean,terminalVectorial,random);
			}
		}
	}
	
	
	/**
	 * Creates a valid program tree from the given flattened sequence of
	 * op nodes. The given {@code operations} and {@code termination} nodes are
	 * used for <em>repairing</em> the program tree, if necessary.
	 *
	 * @param nodes the flattened, possible corrupt, program tree
	 * @param terminals the usable non-terminal operation nodes to use for
	 *        reparation
	 * @param the operation argument type
	 * @return a new valid program tree build from the flattened program tree
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the {@code nodes} sequence is empty
	 */
	public static TreeNode<StvgpOp> toTree(final ISeq<? extends FlatTree<StvgpOp, ?>> nodes,
			final ISeq<StvgpOp> terminalBoolean,
			final ISeq<StvgpOp> terminalVectorial) {
		
		if (nodes.isEmpty()) {
			throw new IllegalArgumentException("Tree nodes must not be empty.");
		}

		final StvgpOp op = requireNonNull(nodes.get(0).value());
		final TreeNode<StvgpOp> tree = TreeNode.of(op);
		return toTree(
			tree,
			0,
			nodes,
			offsets(nodes),
			terminalBoolean,
			terminalVectorial,
			RandomRegistry.random()
		);
	}

	private static TreeNode<StvgpOp> toTree(
		final TreeNode<StvgpOp> root,
		final int index,
		final ISeq<? extends FlatTree<StvgpOp, ?>> nodes,
		final int[] offsets,
		final ISeq<StvgpOp> terminalBoolean,
		final ISeq<StvgpOp> terminalVectorial,
		final Random random) {
		
		if (index < nodes.size()) {
			final FlatTree<StvgpOp, ?> node = nodes.get(index);
			final StvgpOp op = node.value();

			for (int i  = 0; i < op.arity(); ++i) {
				assert offsets[index] != -1;

				final TreeNode<StvgpOp> treeNode = TreeNode.of();
				if (offsets[index] + i < nodes.size()) {
					treeNode.value(nodes.get(offsets[index] + i).value());
				} else {
//					treeNode.value(terminals.get(random.nextInt(terminals.size())));
				}

//				toTree(
//					treeNode,
//					offsets[index] + i,
//					nodes,
//					offsets,
//					terminals,
//					random
//				);
				root.attach(treeNode);
			}
		}

		return root;
	}

	/**
	 * Create the offset array for the given nodes. The offsets are calculated
	 * using the arity of the stored operations.
	 *
	 * @param nodes the flattened tree nodes
	 * @return the offset array for the given nodes
	 */
	static int[] offsets(final ISeq<? extends FlatTree<? extends Op<?>, ?>> nodes) {
		final int[] offsets = new int[nodes.size()];

		int offset = 1;
		for (int i = 0; i < offsets.length; ++i) {
			final Op<?> op = nodes.get(i).value();

			offsets[i] = op.isTerminal() ? -1 : offset;
			offset += op.arity();
		}

		return offsets;
	}
}
