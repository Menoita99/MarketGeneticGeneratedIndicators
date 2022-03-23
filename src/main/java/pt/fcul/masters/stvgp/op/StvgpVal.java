package pt.fcul.masters.stvgp.op;

import java.math.BigDecimal;
import java.util.Objects;

import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.vgp.util.Vector;

public abstract class StvgpVal implements StvgpOp {

	private final String _name;

	StvgpVal(final String name) {
		_name = name;
	}

	@Override
	public final String name() {
		return _name;
	}

	/**
	 * Return the constant value.
	 *
	 * @return the constant value
	 */
	public abstract StvgpType value();

	/**
	 * The apply method will always returns the {@link #value()}.
	 *
	 * @param value the input parameters will be ignored
	 * @return always {@link #value()}
	 */
	@Override
	public final StvgpType apply(final StvgpType[] value) {
		return value();
	}

	/**
	 * StvgpTypehe arity of {@code Val} objects is always zero.
	 *
	 * @return always zero
	 */
	@Override
	public final int arity() {
		return 0;
	}

	@Override
	public StvgpType[] arityType() {
		return new StvgpType[0];
	}
	

	@Override
	public final int hashCode() {
		return Objects.hashCode(value());
	}

	@Override
	public final boolean equals(final Object obj) {
		return obj == this ||
			obj instanceof StvgpVal &&
			equals(((StvgpVal)obj).value(), value());
	}

	private static boolean equals(final Object a, final Object b) {
		if (a instanceof Vector v1 && b instanceof Double) {
			return Double.compare((Double)a, (Double)b) == 0;
		} else if (a instanceof Float && b instanceof Float) {
			return Float.compare((Float)a, (Float)b) == 0;
		} else if (a instanceof BigDecimal && b instanceof BigDecimal) {
			return ((BigDecimal)a).compareTo((BigDecimal)b) == 0;
		}

		return Objects.equals(a, b);
	}

}
