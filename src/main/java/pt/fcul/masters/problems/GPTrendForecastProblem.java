package pt.fcul.masters.problems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.plotter.file.Csv;
import com.plotter.file.FileWriter;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.IO;
import pt.fcul.master.utils.SystemProperties;
import pt.fcul.masters.GPTrendForecast;

public class GPTrendForecastProblem {




	private static final String SAVE_FOLDER = "C:\\Users\\Owner\\Desktop\\GP_SAVES";
	private static final String INSTANCE_SAVE_FOLDER = SAVE_FOLDER + "\\" + GPTrendForecast.class.getSimpleName() + "\\" 
			+ LocalDateTime.now().format(DateTimeFormatter.ofPattern(SystemProperties.getOrDefault("save.folder.timeformatter","dd-MM-yyyy HH_mm_ss"))) + "\\" ;
	
	private static final int MAX_GENERATIONS = 70;
	private static final int TOURNAMENT_SIZE = 10;
	private static final int POPULATION_SIZE = 1000;
	private static final int MAX_PHENOTYPE_AGE = 70;
	private static final double SELECTOR_MUT = 0.01;
	private static final double SELECTOR_PROB = 0.7;
	private static final double SURVIVOR_FRACTION = 0.02;
	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final GPTrendForecast PROBLEM = GPTrendForecast.standartConfs();
	
	
	
	
	private static Engine<ProgramGene<Double>, Double> getEngine() {
		PROBLEM.getMemory().toCsv(INSTANCE_SAVE_FOLDER+"data.csv");

		return Engine.builder(PROBLEM)
				.maximizing()
				.interceptor(EvolutionResult.toUniquePopulation(1))
				.offspringSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
				.survivorsFraction(SURVIVOR_FRACTION)
				.survivorsSelector(new TournamentSelector<>(TOURNAMENT_SIZE))
				.alterers(
						new SingleNodeCrossover<>(SELECTOR_PROB),
						new Mutator<>(SELECTOR_MUT))
				.executor(executor)
				.maximalPhenotypeAge(MAX_PHENOTYPE_AGE)
				.populationSize(POPULATION_SIZE)
				.build();
	}
	
	
	
	
	private static EvolutionResult<ProgramGene<Double>, Double> evolve(Engine<ProgramGene<Double>, Double> engine) {
		Serie<Long,Double> trainEvolutionFitness = new Serie<>("Train fitness");
		Serie<Long,Double> validateEvolutionFitness = new Serie<>("Validation fitness");
		EvolutionStatistics<Double, DoubleMomentStatistics> stats = EvolutionStatistics.ofNumber();
		
		EvolutionResult<ProgramGene<Double>, Double> result = engine.stream()
				.limit(Limits.byFixedGeneration(MAX_GENERATIONS))
				.limit(Limits.bySteadyFitness(5))
//				.peek(e -> {
//					System.out.println("-----------------------");
//					e.population().forEach(k ->System.out.println(new MathExpr(k.genotype().gene()))));}
				.peek(e -> {
					System.out.println(e.generation()+" "+e.bestFitness());
					//TODO  do this non blocking
					trainEvolutionFitness.add( e.generation(), e.bestFitness());
					validateEvolutionFitness.add(e.generation(), PROBLEM.validate(e.bestPhenotype().genotype().gene()));
				})
				.peek(stats)
				.collect(EvolutionResult.toBestEvolutionResult());
		
		try {
			Plotter.builder().lineChart("Fitness", trainEvolutionFitness,validateEvolutionFitness).build().plot();
			Csv.printSameXSeries(new File(INSTANCE_SAVE_FOLDER+"fitnessData.csv"),trainEvolutionFitness,validateEvolutionFitness);
			FileWriter.appendln(INSTANCE_SAVE_FOLDER+"stats.txt", stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}	
	
	
	
	
	private static void evaluate(EvolutionResult<ProgramGene<Double>, Double> result) {
		ProgramGene<Double> program = result.bestPhenotype().genotype().gene();
		TreeNode<Op<Double>> tree = program.toTreeNode();
		MathExpr.rewrite(tree);	
		
		System.out.println("Generations: " + result.totalGenerations());
		System.out.println("Function:    " + new MathExpr(tree));
		
		try {			
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"individualMathExpression.txt", new MathExpr(tree));
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "generations="+result.totalGenerations());
			
			IO.object.write(tree, INSTANCE_SAVE_FOLDER+"individual.gp");
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		List<Double[]> behaviorData = PROBLEM.getBehaviorData(tree);
		Serie<Integer,Double> accuracy = new Serie<>("accuracy");
		
		for (int i = 0; i < behaviorData.size(); i++)
			accuracy.add(i, behaviorData.get(i)[2]);

		try {
			Plotter.builder().lineChart("RSI", accuracy).build().plot();
			Csv.printSameXSeries(new File(INSTANCE_SAVE_FOLDER+"accuracy.csv"),accuracy);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private static void saveParams() {
		try {
			new File(INSTANCE_SAVE_FOLDER).mkdirs();
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "framework=GP");
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "POPULATION_SIZE="+POPULATION_SIZE);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "MAX_PHENOTYPE_AGE="+MAX_PHENOTYPE_AGE);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "SELECTOR_MUT="+SELECTOR_MUT);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "SELECTOR_PROB="+SELECTOR_PROB);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "TOURNAMENT_SIZE="+TOURNAMENT_SIZE);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "SURVIVOR_FRACTION="+SURVIVOR_FRACTION);
			FileWriter.appendln( INSTANCE_SAVE_FOLDER+"confs.txt", "MAX_GENERATIONS="+MAX_GENERATIONS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String[] args) {
		saveParams();
		Engine<ProgramGene<Double>, Double> engine = getEngine(); 
		EvolutionResult<ProgramGene<Double>, Double> result = evolve(engine);
		evaluate(result);
	}
}
