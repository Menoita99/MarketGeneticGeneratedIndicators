package pt.fcul.masters.stvgp.op;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

import io.jenetics.internal.util.Lazy;
import pt.fcul.masters.stvgp.StvgpType;

public class StvgpEphemeralConst extends StvgpVal implements StvgpOp, Serializable{

	private static final long serialVersionUID = 1L;

	private final Lazy<StvgpType> _value;
	private final Supplier<StvgpType> _supplier;

	private StvgpEphemeralConst(final String name,final Lazy<StvgpType> value,final Supplier<StvgpType> supplier) {
		super(name);
		_value = requireNonNull(value);
		_supplier = requireNonNull(supplier);
	}

	private StvgpEphemeralConst(final String name, final Supplier<StvgpType> supplier) {
		this(name, Lazy.of(supplier), supplier);
	}

	/**
	 * Return a newly created, uninitialized constant of type {@code StvgpType}.
	 *
	 * @return a newly created, uninitialized constant of type {@code StvgpType}
	 */
	@Override
	public StvgpOp get() {
		return new StvgpEphemeralConst(name(), _supplier);
	}

	/**
	 * Fixes and returns the constant value.
	 *
	 * @since 5.0
	 *
	 * @return the constant value
	 */
	@Override
	public StvgpType value() {
		return _value.get();
	}

	@Override
	public String toString() {
		return name() != null
			? format("%s(%s)", name(), value())
			: Objects.toString(value());
	}

	/**
	 * Create a new ephemeral constant with the given {@code name} and value
	 * {@code supplier}. For every newly created operation tree, a new constant
	 * value is chosen for this terminal operation. The value is than kept
	 * constant for this tree.
	 *
	 * @param name the name of the ephemeral constant
	 * @param supplier the value supplier
	 * @param <T> the constant type
	 * @return a new ephemeral constant
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static StvgpEphemeralConst of(final String name,final Supplier<StvgpType> supplier) {
		return new StvgpEphemeralConst(requireNonNull(name), supplier);
	}

	/**
	 * Create a new ephemeral constant with the given value {@code supplier}.
	 * For every newly created operation tree, a new constant value is chosen
	 * for this terminal operation. The value is than kept constant for this tree.
	 *
	 * @param supplier the value supplier
	 * @param <T> the constant type
	 * @return a new ephemeral constant
	 * @throws NullPointerException if the {@code supplier} is {@code null}
	 */
	public static StvgpEphemeralConst of(final Supplier<StvgpType> supplier) {
		return new StvgpEphemeralConst(null, supplier);
	}

	@Override
	public StvgpType outputType() {
		return value();
	}
}
