package pt.fcul.masters.validators;

import java.util.function.Consumer;

import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;

public class Validator<C extends Comparable<? super C>> implements Consumer<EvolutionResult<?, C>>{

	@Override
	public void accept(EvolutionResult<?, C> evoResult) {
//		 TODO Auto-generated method stub
		Phenotype<?, C> candidate = evoResult.bestPhenotype().nullifyFitness();
		Phenotype<?, C> evaluatedCandidate = candidate.eval(null);
	}

}
