package pt.fcul.masters.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.plotter.file.Csv;
import com.plotter.file.FileWriter;
import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.IO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.market.Transaction;
import pt.fcul.masters.utils.SystemProperties;
import pt.fcul.masters.vgp.util.ComplexVector;
import pt.fcul.masters.vgp.util.Vector;

@Data
@Log
public class BasicGpLogger<I> {

	//property save.folder.timeformatters
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(SystemProperties.getOrDefault("save.folder.timeformatter","dd-MM-yyyy HH_mm_ss"));

	//property save.folder
	private String saveFolder = SystemProperties.getOrDefault("save.folder", System.getProperty("user.home"));

	private final GpProblem<I> problem;

	private LinkedList<EvolutionEntry> logs = new LinkedList<>();

	private String instanceSaveFolder = "";

	private EngineConfiguration<ProgramGene<I>, Double> configuration;





	public BasicGpLogger(GpProblem<I> problem, EngineConfiguration<ProgramGene<I>, Double> configuration) {
		this.configuration = configuration;
		this.problem = problem;
		new File(getInstanceSaveFolder()).mkdirs();
	}






	public String getInstanceSaveFolder() {
		if(instanceSaveFolder.isBlank())
			instanceSaveFolder = saveFolder + "\\" + problem.getClass().getSimpleName() + "\\" + LocalDateTime.now().format(dateFormatter)+ "_" + problem.getTable().getMarket() + "\\";
		return instanceSaveFolder;
	}

	private int ticksInMarket;
	private int openTrades;
	private double roi;
	private double winRate;

	public void log(EvolutionResult<ProgramGene<I>,Double> result) {
		//TODO  do this non blocking
		Map<ValidationMetric, List<Double>> validation = problem.validate(result.bestPhenotype().genotype().gene(), false);
		final EvolutionEntry entry = new EvolutionEntry(
				result.generation(),
				validation.get(ValidationMetric.MONEY).get(validation.get(ValidationMetric.MONEY).size()-1),
				result.bestFitness(),
				validation.get(ValidationMetric.FITNESS).get(validation.get(ValidationMetric.FITNESS).size()-1),
				validation.containsKey(ValidationMetric.CONFIDENCE) ? validation.get(ValidationMetric.CONFIDENCE).stream().mapToDouble(d->d).average().getAsDouble() : -1,
				result.population().stream().mapToDouble(Phenotype::fitness).average().getAsDouble(),
				result.worstFitness(),
				result.invalidCount(),
				result.alterCount(),
				result.killCount(),
				result.durations().evaluationDuration().getSeconds(),
				result.durations().evolveDuration().getSeconds(),
				result.durations().offspringAlterDuration().getSeconds(),
				result.durations().offspringFilterDuration().getSeconds(),
				result.durations().offspringSelectionDuration().getSeconds(),
				result.durations().survivorFilterDuration().getSeconds(),
				result.durations().survivorsSelectionDuration().getSeconds(),
				result.bestPhenotype().genotype().gene().depth(),
				result.bestPhenotype().genotype().gene().size(),
				result.bestPhenotype().genotype().gene().toTreeNode(),
				validation.get(ValidationMetric.TRADED_TICKS).get(0).intValue(),
				validation.get(ValidationMetric.OPEN_TRADES).get(0).intValue(),
				validation.get(ValidationMetric.ROI).get(0),
				validation.get(ValidationMetric.WIN_RATE).get(0));
		logs.add(entry);
		log.info(entry.toString());
	}





	public void save() {
		saveConf();
		saveData();
		saveFitness();
		saveValidation();
	}





	public void saveConf() {
		configuration.save(getInstanceSaveFolder());

		String content = "Operations:"+System.lineSeparator();
		content += "Market: "+problem.getTable().getMarket()+System.lineSeparator();
		
		for(Op<I> op : problem.operations())
			content += op.toString()+System.lineSeparator();
		
		content += System.lineSeparator()+"Terminals:"+System.lineSeparator();
		for(Op<I> op : problem.terminals())
			content += op.toString()+System.lineSeparator();
		
		try {
			FileWriter.append(new File(getInstanceSaveFolder() + "cons.txt"), content);
		} catch (FileNotFoundException e) {
			log.warning(e.getMessage());
		}
	}





	public void saveData() {
		try {
			problem.getTable().toCsv(getInstanceSaveFolder() + "data.csv");
		}catch (Exception e) {
			log.warning(e.getMessage());
		}
	}





	@SuppressWarnings("unchecked")
	public Map<ValidationMetric, List<Double>> saveValidation() {
		TreeNode<Op<I>> tree = logs.getLast().getTreeNode();
		try {			
		//	MathExpr.rewrite((TreeNode<Op<Double>>)(Object) logs.getLast().getTreeNode());
			FileWriter.appendln( getInstanceSaveFolder()+"individualMathExpression.txt", new MathExpr((TreeNode<Op<Double>>)(Object) logs.getLast().getTreeNode()));
			IO.object.write(tree, getInstanceSaveFolder()+"individual.gp");
			log.info("Saved Agent at "+ (getInstanceSaveFolder()+"individual.gp"));
		} catch (IOException e) {
			log.warning(e.getMessage());
		}

		try {
			Map<ValidationMetric, List<Double>> validate = problem.validate(tree, false);

			List<Serie<?,?>> series = new LinkedList<>();
			
			for(ValidationMetric vm : ValidationMetric.values())
				if(validate.containsKey(vm))
					series.add(Serie.of(vm.toString(),validate.get(vm)));
			
			series.removeIf(s -> s.getData().size() <= 1);
			
			Csv.printSameXSeries(new File(getInstanceSaveFolder()+"agent_result.csv"),series);
			log.info("Saved results data at "+ (getInstanceSaveFolder()+"agent_result.csv"));

			return validate;
		} catch (Exception e) {
			e.printStackTrace();
			return Map.of();
		}
	}





	public void saveFitness() {
		try(PrintWriter pw = new PrintWriter(new File(getInstanceSaveFolder()+"fitnessData.csv"))){
			pw.println(EvolutionEntry.toFileColumns());
			logs.forEach(e -> pw.println(e.toFileString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Saved fitness data at "+ (getInstanceSaveFolder()+"fitnessData.csv"));
	}	





	public void plot() {
		plotFitness();
		plotValidation(false);
	}





	public void plotValidation(boolean useTestSet) {
		TreeNode<Op<I>> tree = logs.getLast().getTreeNode();
		Map<ValidationMetric, List<Double>> validate = problem.validate(tree, useTestSet);
		
		
		validate.forEach((k,v)-> {
			try {
				if(!v.isEmpty())
					Plotter.builder().lineChart(k.toString(), Serie.of(k.toString(),v)).build().plot();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		});
		
		if(validate.containsKey((ValidationMetric.AGENT_OUTPUT)) && validate.containsKey((ValidationMetric.EXPECTED_OUTPUT))){
			Serie<Integer,Double> agentOutput = Serie.of(ValidationMetric.AGENT_OUTPUT.toString(),validate.get(ValidationMetric.AGENT_OUTPUT));
			Serie<Integer,Double> expectedOutput = Serie.of(ValidationMetric.EXPECTED_OUTPUT.toString(),validate.get(ValidationMetric.EXPECTED_OUTPUT));
			Plotter.builder().lineChart("Agent Output and Expected Output", agentOutput,expectedOutput).build().plot();
		}
		
		//		accuracy.cleanIf((x,y)-> x < 2000 );
		//		expectedOutput.cleanIf((x,y)-> Double.isNaN(y) || Double.isInfinite(y));
		//		agentOutput.cleanIf((x,y)-> Double.isNaN(y) || Double.isInfinite(y));
		//		Plotter.builder().lineChart(PROBLEM.getMemory().getColumn("close"),"close").build().plot();
	}





	public void plotFitness() {
		Serie<Long, Double> trainEvolutionFitness = new Serie<>("Train Fitness");
		Serie<Long, Double> validateEvolutionFitness = new Serie<>("Validation Fitness");

		for (EvolutionEntry e : logs) {
			trainEvolutionFitness.add(e.getGeneration(), e.getBestFitness());
			validateEvolutionFitness.add(e.getGeneration(), e.getValidationFitness());
		}
		Plotter.builder().lineChart("Fitness", trainEvolutionFitness,validateEvolutionFitness).build().plot();
	}	
	
	
	public void saveTransactions() {
		TreeNode<Op<I>>tree = logs.getLast().getTreeNode();
		
		List<String> transactions = new LinkedList<>();
		List<String> traintransactions = new LinkedList<>();
		traintransactions.add(Transaction.fileColumns());
		transactions.add(Transaction.fileColumns());
		
		MarketSimulator<I> ms = MarketSimulator.builder(problem.getTable()).penalizerRate(0).build();
		ms.simulateMarket(args -> {
			I agentOutput = Program.eval(tree, args);
			if(agentOutput instanceof Vector ao)
				return MarketAction.asSignal(ao.asMeanScalar());
			if(agentOutput instanceof ComplexVector cv)
				return MarketAction.asSignal(cv.realMean());
			return MarketAction.asSignal((double)agentOutput);
		}, false, null);
		
		
		Serie<Integer, Double> serie = new Serie<>("Profit");
		serie.add(0, 0D);
		
		for(Transaction t: ms.getTransactions()) {
			transactions.add(t.toFileString());
			serie.add(t.getCloseIndex(), serie.getData().get(serie.getData().size() -1).y() + t.realizedProfit());
		}
		
		ms = MarketSimulator.builder(problem.getTable()).penalizerRate(0).build();
		ms.simulateMarket(args -> {
			I agentOutput = Program.eval(tree, args);
			if(agentOutput instanceof Vector ao)
				return MarketAction.asSignal(ao.asMeanScalar());
			if(agentOutput instanceof ComplexVector cv)
				return MarketAction.asSignal(cv.realMean());
			return MarketAction.asSignal((double)agentOutput);
		}, true, null);
		
		
		for(Transaction t: ms.getTransactions()) {
			traintransactions.add(t.toFileString());
			serie.add(t.getCloseIndex(), serie.getData().get(serie.getData().size() -1).y() + t.realizedProfit());
		}
		
		try {
			FileWriter.println(getInstanceSaveFolder() + "transactions.csv", transactions);
			FileWriter.println(getInstanceSaveFolder() + "Traintransactions.csv", traintransactions);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Plotter.builder().lineChart(serie, "Profit").build().plot();
	}




	 /* TODO add evaluation metrics
	 * ROI
	 * N of open trades
	 * Win rate
	 * Average time in the market
	 */
	@Data
	@AllArgsConstructor
	public final class EvolutionEntry{

		private long generation; 
		private double validationEndMoney;
		private double bestFitness; 
		private double validationFitness; 
		private double meanValidationConfidence; 
		private double averageFitness;
		private double worstFitness; 
		private int invalidCount;
		private int alterCount; 
		private int killCount; 
		private long evaluationDuration;
		private long evolveDuration;
		private long offspringAlterDuration;
		private long offspringFilterDuration;
		private long offspringSelectionDuration;
		private long survivorFilterDuration;
		private long survivorsSelectionDuration;
		private int depth; 
		private int size; 
		private TreeNode<Op<I>> treeNode;
		private int ticksInMarket;
		private int openTrades;
		private double roi;
		private double winRate;
		
		

		public String toFileString() {
			return  generation + "," +validationEndMoney + "," + bestFitness + "," + validationFitness + "," + meanValidationConfidence + "," + averageFitness + "," + worstFitness
					+ "," + invalidCount + "," + alterCount + "," + killCount + "," + evaluationDuration + "," + evolveDuration
					+ "," + offspringAlterDuration + "," + offspringFilterDuration + "," + offspringSelectionDuration
					+ "," + survivorFilterDuration + ","+ survivorsSelectionDuration + "," + depth + "," + size + ",\"" + treeNode + "\"" 
					+ "," + ticksInMarket  + "," + openTrades  + "," + roi  + "," + winRate;
		}

		public static String toFileColumns() {
			return "Generation,Validation End Money,Best Fitness,Validation Fitness,Mean Validation Confidence,Average Fitness,Worst Fitness,Invalid Count"
					+ ",Alter Count,Kill Count,Evaluation Duration,Evolve Duration,Offspring Alter Duration"
					+ ",Offspring Filter Duration,Offspring Selection Duration,Survivor Filter Duration,Survivors Selection Duration"
					+ ",Depth,Size,Tree,Ticks in market,Open Trade,ROI,Win Rate";
		}
	}
}
