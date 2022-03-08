package pt.fcul.master.stvgp.op;

import io.jenetics.prog.op.Op;
import pt.fcul.master.stvgp.StvgpType;

public interface StvgpOp extends Op<StvgpType> {

	StvgpType[] arityType();
	
	StvgpType outputType();
	
	
	
	enum Type{
		BOOLEAN,RELATIONAL,VECTORIAL;
	}
	
	static Type getOpType(StvgpOp operation) {
		boolean hasBooleanArg = false;
		boolean hasVectorialArg = false;
		
		for(StvgpType type: operation.arityType()) {
			if(type.isBooleanType())
				hasBooleanArg = true;
			else if(type.isVectorType())
				hasVectorialArg = true;
		}
		
		if((hasBooleanArg && !hasVectorialArg && operation.outputType().isBooleanType())
				|| (!hasBooleanArg && !hasVectorialArg && operation.outputType().isBooleanType())) { // BOLEAN OP receives boolean and output a boolean
			return Type.BOOLEAN;
		}else if((!hasBooleanArg && hasVectorialArg && operation.outputType().isVectorType())
				|| (!hasBooleanArg && !hasVectorialArg && operation.outputType().isVectorType())) { // VECTORIAL OP receives vectors and outputs a vector
			return Type.VECTORIAL;
		}else if(operation.outputType().isBooleanType()) { // RELATIONAL OP receives only vectors or vectors and booleans and returns a boolean
			return Type.RELATIONAL;
		}else {
			throw new IllegalArgumentException("Unknow operations: "+ operation);
		}
	}

	/**
	 * Returns if 2 operations have the same type and number of arity and the same output
	 * @param op operation to test
	 * @param thisOp operation to test
	 * @return return true if operations are equivalent otherwise returns false
	 */
	static boolean equivalent(StvgpOp op1, StvgpOp op2) {
		if(op1.arity() != op2.arity())
			return false;
		
		if(!op1.outputType().isSameType(op2.outputType())) // checks if both operations produce the same output type	
			return false;
		
		for (int i = 0; i < op1.arityType().length; i++) //checks if both operations receive the same arguments in the same order
			if(!op1.arityType()[i].isSameType(op2.arityType()[i]))	
				return false;
		
		return true;
	}
}
