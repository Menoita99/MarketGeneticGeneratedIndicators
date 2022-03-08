package pt.fcul.master.stvgp.runner;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import pt.fcul.master.stvgp.StvgpSingleNodeCrossover;
import pt.fcul.master.stvgp.problems.ProfitSeekingStvgp;

public class Runner {

	public static void main(String[] args) {
		ProfitSeekingStvgp t = new ProfitSeekingStvgp();
		Engine.builder(t)
			.populationSize(4)
			.alterers(
					new StvgpSingleNodeCrossover<>(1), 
					new Mutator<>(1)
			)
			.build()
			.stream()
			.limit(10)
			.peek(e -> System.out.println(e.generation()+" "+e.bestPhenotype().genotype()))
			.collect(EvolutionResult.toBestPhenotype());
	}
	
	
	
}
