package pt.fcul.masters.logger;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.plotter.file.FileWriter;

import io.jenetics.Gene;
import io.jenetics.Selector;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine.Builder;
import io.jenetics.engine.Engine.Setup;
import io.jenetics.engine.EvolutionParams;
import lombok.Data;

@Data
public class EngineConfiguration <G extends Gene<?, G>,C extends Comparable<? super C>> implements Setup<G,C>{


	private int vectorSize = 13;
	private int maxSteadyFitness = 10;
	private int maxPhenotypeAge = 3;
	private int maxGenerations = 70;
	private int populationSize = 1000;
	private double tournamentFractionSize = 0.05;
	private double selectionMutationProb = 0.001;
	private double selectionProb = 0.7;
	private double survivorProb = 0.02;

	private Selector<G, C> survivorsSelector ;
	private Selector<G, C> offspringSelector;

	private Map<String,String> customParams = new LinkedHashMap<>();

	public boolean unUsed = false;

	public EngineConfiguration() {
		survivorsSelector = new TournamentSelector<>((int)(populationSize * tournamentFractionSize));
		offspringSelector = new TournamentSelector<>((int)(populationSize * tournamentFractionSize));
	}


	public EngineConfiguration(int vectorSize, int maxSteadyFitness, int maxPhenotypeAge, int maxGenerations,
			int populationSize, double tournamentFractionSize, double selectionMutationProb, double selectionProb,
			double survivorProb) {
		this.vectorSize = vectorSize;
		this.maxSteadyFitness = maxSteadyFitness;
		this.maxPhenotypeAge = maxPhenotypeAge;
		this.maxGenerations = maxGenerations;
		this.populationSize = populationSize;
		this.tournamentFractionSize = tournamentFractionSize;
		this.selectionMutationProb = selectionMutationProb;
		this.selectionProb = selectionProb;
		this.survivorProb = survivorProb;

		survivorsSelector = new TournamentSelector<>((int)(populationSize * tournamentFractionSize));
		offspringSelector = new TournamentSelector<>((int)(populationSize * tournamentFractionSize));
	}


	public static <G extends Gene<?, G>,C extends Comparable<? super C>> EngineConfiguration<G,C> standart() {
		return new EngineConfiguration<>();
	}


	public EvolutionParams<G,C> toEvoParams(){
		return EvolutionParams.<G,C>builder()
				.offspringSelector(offspringSelector)
				.survivorsSelector(survivorsSelector)
				.maximalPhenotypeAge(maxPhenotypeAge)
				.populationSize(populationSize)
				.build();
	}



	public void save(String path) {
		try {
			if(!unUsed)
				FileWriter.print( path+"confs.txt", this);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void apply(Builder<G, C> builder) {
		builder
		.offspringSelector(offspringSelector)
		.survivorsFraction(survivorProb)
		.survivorsSelector(survivorsSelector)
		.maximalPhenotypeAge(maxPhenotypeAge)
		.populationSize(populationSize);
	}


	public static <G extends Gene<?, G>,C extends Comparable<? super C>> EngineConfiguration<G,C> unUsed() {
		EngineConfiguration<G, C> ec = new EngineConfiguration<>();
		ec.setUnUsed(true);
		return ec;
	}
}
