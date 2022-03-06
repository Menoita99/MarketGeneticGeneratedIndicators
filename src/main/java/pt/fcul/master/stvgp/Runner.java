package pt.fcul.master.stvgp;

import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;

public class Runner {

	public static void main(String[] args) {
		ProfitSeekingStvgp t = new ProfitSeekingStvgp();
		Engine.builder(t)
			.populationSize(10)
			.build()
			.stream()
			.collect(EvolutionResult.toBestPhenotype());
	}
	
	
	
}
