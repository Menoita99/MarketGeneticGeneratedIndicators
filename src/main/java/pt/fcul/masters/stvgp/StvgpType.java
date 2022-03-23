package pt.fcul.masters.stvgp;

import lombok.EqualsAndHashCode;
import pt.fcul.masters.vgp.util.Vector;

/**
 * this class or represents a vector or a boolean, 
 * it can never represent both at the same time
 * 
 * @author Rui Menoita
 */
@EqualsAndHashCode
public class StvgpType {
	
	private boolean bool;
	private Vector vector;
	
	public StvgpType(boolean bool) {
		this.bool = bool;
	}
	
	public StvgpType(Vector vector) {
		this.vector = vector;
	}
	
	public boolean isBooleanType() {
		return vector == null;
	}
	
	public boolean isVectorType() {
		return vector != null;
	}
	
	/**
	 * This will convert this type to boolean
	 * @param data 
	 */
	public void setBooleanType(boolean data) {
		this.vector = null;
		this.bool = data;
	}
	
	public boolean getAsBooleanType() {
		if(!isBooleanType())
			throw new IllegalStateException("This object is a vector, can't get as boolean");
		return this.bool;
	}
	
	/**
	 * This will convert this type to Vector
	 * @param data 
	 */
	public void setVectorType(Vector data) {
		this.vector = data;
	}	
	
	public Vector getAsVectorType() {
		if(isBooleanType())
			throw new IllegalStateException("This object is a boolean, can't get as vector");
		return this.vector;
	}
	
	public static StvgpType bool() {
		return new StvgpType(false);
	}
	
	
	public static StvgpType vector() {
		return new StvgpType(Vector.empty());
	}

	public static StvgpType of(boolean b) {
		return new StvgpType(b);
	}

	public static StvgpType of(Vector v) {
		return new StvgpType(v);
	}
	
	@Override
	public String toString() {
		return isBooleanType() ? (getAsBooleanType() ? "TRUE" : "FALSE") : getAsVectorType().toString();
	}

	public boolean isSameType(StvgpType type) {
		return isBooleanType() == type.isBooleanType() ;
	}
}
