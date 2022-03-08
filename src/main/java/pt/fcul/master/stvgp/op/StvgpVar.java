package pt.fcul.master.stvgp.op;

import static io.jenetics.ext.internal.Names.isIdentifier;
import static java.lang.String.format;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.jenetics.ext.util.Tree;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.op.BoolOp;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.prog.op.Var;
import pt.fcul.master.stvgp.StvgpType;

public class StvgpVar implements StvgpOp, Comparable<StvgpVar>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final int _index;
	private StvgpType _type;

	/**
	 * Create a new variable with the given {@code name} and projection
	 * {@code index}.
	 *
	 * @param name the variable name. Used when printing the operation tree
	 *        (program)
	 * @param index the projection index
	 * @throws IllegalArgumentException if the given {@code name} is not a valid
	 *         Java identifier
	 * @throws IndexOutOfBoundsException if the projection {@code index} is
	 *         smaller than zero
	 * @throws NullPointerException if the given variable {@code name} is
	 *         {@code null}
	 */
	private StvgpVar(final String name, final int index, final StvgpType type) {
		if (!isIdentifier(name)) {
			throw new IllegalArgumentException(format("'%s' is not a valid identifier.", name));
		}
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index smaller than zero: " + index	);
		}

		_name = name;
		_index = index;
		_type = type;
	}

	/**
	 * The projection index of the variable.
	 *
	 * @return the projection index of the variable
	 */
	public int index() {
		return _index;
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
		return new StvgpType[0];
	}

	@Override
	public StvgpType outputType() {
		return _type;
	}

	@Override
	public StvgpType apply(final StvgpType[] variables) {
		return variables[_index];
	}

	@Override
	public int compareTo(final StvgpVar o) {
		return _name.compareTo(o._name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(_name);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this ||
			obj instanceof Var &&
			Objects.equals(((StvgpVar)obj)._name, _name);
	}

	@Override
	public String toString() {
		return _name;
	}

	/**
	 * Create a new variable with the given {@code name} and projection
	 * {@code index}.
	 *
	 * @see #parse(String)
	 *
	 * @param name the variable name. Used when printing the operation tree
	 *        (program)
	 * @param index the projection index
	 * @param <T> the variable type
	 * @return a new variable with the given {@code name} and projection
	 *         {@code index}
	 * @throws IllegalArgumentException if the given {@code name} is not a valid
	 *         Java identifier
	 * @throws IndexOutOfBoundsException if the projection {@code index} is
	 *         smaller than zero
	 * @throws NullPointerException if the given variable {@code name} is
	 *         {@code null}
	 */
	public static StvgpVar of(final String name, final int index, final StvgpType type) {
		return new StvgpVar(name, index,type);
	}

	/**
	 * Create a new variable with the given {@code name}. The projection index
	 * is set to zero. Always prefer to create new variables with
	 * {@link #of(String, int)}, especially when you define your terminal
	 * operation for your GP problem.
	 *
	 * @see #parse(String)
	 *
	 * @param name the variable name. Used when printing the operation tree
	 *        (program)
	 * @param <T> the variable type
	 * @return a new variable with the given {@code name} and projection index
	 *         zero
	 * @throws IllegalArgumentException if the given {@code name} is not a valid
	 *         Java identifier
	 * @throws NullPointerException if the given variable {@code name} is
	 *         {@code null}
	 */
	public static StvgpVar of(final String name, final StvgpType type) {
		return new StvgpVar(name, 0,type);
	}

	private static final Pattern VAR_INDEX = Pattern.compile("(.+)\\[\\s*(\\d+)\\s*]");

	/**
	 * Parses the given variable string to its name and index. The expected
	 * format is <em>var_name</em>[<em>index</em>].
	 *
	 * <pre> {@code
	 * x[0]
	 * y[3]
	 * my_var[4]
	 * }</pre>
	 *
	 * If no variable <em>index</em> is encoded in the name, a variable with
	 * index 0 is created.
	 *
	 * @see #of(String, int)
	 *
	 * @param name the variable name + index
	 * @param <T> the operation type
	 * @return a new variable parsed from the input string
	 * @throws IllegalArgumentException if the given variable couldn't be parsed
	 *         and the given {@code name} is not a valid Java identifier
	 * @throws NullPointerException if the given variable {@code name} is
	 *         {@code null}
	 */
	public static StvgpVar parse(final String name, final StvgpType type) {
		final Matcher matcher = VAR_INDEX.matcher(name);

		return matcher.matches()
			? of(matcher.group(1), Integer.parseInt(matcher.group(2)),type)
			: of(name, 0,type);
	}

	/**
	 * Re-indexes the variables of the given operation {@code tree}. If the
	 * operation tree is created from it's string representation, the indices
	 * of the variables ({@link Var}), are all set to zero, since it needs the
	 * whole tree for setting the indices correctly. The mapping from the node
	 * string to the {@link Op} object, on the other hand, is a <em>local</em>
	 * operation. This method gives you the possibility to fix the indices of
	 * the variables. The indices of the variables are assigned according it's
	 * <em>natural</em> order.
	 *
	 * <pre>{@code
	 * final TreeNode<Op<Double>> tree = TreeNode.parse(
	 *     "add(mul(x,y),sub(y,x))",
	 *     MathOp::toMathOp
	 * );
	 *
	 * assert Program.eval(tree, 10.0, 5.0) == 100.0; // wrong result
	 * Var.reindex(tree);
	 * assert Program.eval(tree, 10.0, 5.0) == 45.0; // correct result
	 * }</pre>
	 * The example above shows a use-case of this method. If you parse a tree
	 * string and convert it to an operation tree, you have to re-index the
	 * variables first. If not, you will get the wrong result when evaluating
	 * the tree. After the re-indexing you will get the correct result of 45.0.
	 *
	 * @since 5.0
	 *
	 * @see MathOp#toMathOp(String)
	 * @see Program#eval(Tree, Object[])
	 *
	 * @param tree the tree where the variable indices needs to be fixed
	 * @param <V> the operation value type
	 */
	public static <V> void reindex(final TreeNode<Op<V>> tree) {
		final SortedSet<Var<V>> vars = tree.stream()
			.filter(node -> node.value() instanceof Var)
			.map(node -> (Var<V>)node.value())
			.collect(Collectors.toCollection(TreeSet::new));

		int index = 0;
		final Map<Var<V>, Integer> indexes = new HashMap<>();
		for (Var<V> var : vars) {
			indexes.put(var, index++);
		}

		reindex(tree, indexes);
	}

	/**
	 * Re-indexes the variables of the given operation {@code tree}. If the
	 * operation tree is created from it's string representation, the indices
	 * of the variables ({@link Var}), are all set to zero, since it needs the
	 * whole tree for setting the indices correctly.
	 *
	 * <pre>{@code
	 * final TreeNode<Op<Double>> tree = TreeNode.parse(
	 *     "add(mul(x,y),sub(y,x))",
	 *     MathOp::toMathOp
	 * );
	 *
	 * assert Program.eval(tree, 10.0, 5.0) == 100.0; // wrong result
	 * final Map<Var<Double>, Integer> indexes = new HashMap<>();
	 * indexes.put(Var.of("x"), 0);
	 * indexes.put(Var.of("y"), 1);
	 * Var.reindex(tree, indexes);
	 * assert Program.eval(tree, 10.0, 5.0) == 45.0; // correct result
	 * }</pre>
	 * The example above shows a use-case of this method. If you parse a tree
	 * string and convert it to an operation tree, you have to re-index the
	 * variables first. If not, you will get the wrong result when evaluating
	 * the tree. After the re-indexing you will get the correct result of 45.0.
	 *
	 * @since 5.0
	 *
	 * @see MathOp#toMathOp(String)
	 * @see BoolOp#toBoolOp(String)
	 * @see Program#eval(Tree, Object[])
	 *
	 * @param tree the tree where the variable indices needs to be fixed
	 * @param indexes the variable to index mapping
	 * @param <V> the operation value type
	 */
	public static <V> void reindex(
		final TreeNode<Op<V>> tree,
		final Map<Var<V>, Integer> indexes
	) {
		for (TreeNode<Op<V>> node : tree) {
			final Op<V> op = node.value();
			if (op instanceof Var) {
				node.value(Var.of(op.name(), indexes.get(op)));
			}
		}
	}
}
