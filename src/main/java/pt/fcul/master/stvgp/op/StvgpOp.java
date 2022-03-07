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
}
