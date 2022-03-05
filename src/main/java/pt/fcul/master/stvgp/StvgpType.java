package pt.fcul.master.stvgp;

import java.util.Vector;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * this class or represents a vector or a boolean, 
 * it can never represent both at same time
 * 
 * @author Rui Menoita
 */
@ToString
@EqualsAndHashCode
public class StvgpType {
	
	private boolean bool;
	private Vector<Float> vector;
	
	public StvgpType(boolean bool) {
		this.bool = bool;
	}
	
	public StvgpType(Vector<Float> vector) {
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
	public void setVectorType(Vector<Float> data) {
		this.vector = data;
	}	
	
	public Vector<Float> getAsVectorType() {
		if(isBooleanType())
			throw new IllegalStateException("This object is a boolean, can't get as vector");
		return this.vector;
	}
}
