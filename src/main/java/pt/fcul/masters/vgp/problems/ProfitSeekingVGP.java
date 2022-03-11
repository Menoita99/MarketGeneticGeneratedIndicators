package pt.fcul.masters.vgp.problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.ISeq;
import lombok.Data;
import lombok.ToString;
import lombok.extern.java.Log;
import pt.fcul.master.utils.Pair;
import pt.fcul.masters.gp.problems.GpProblem;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.vgp.util.Vector;

@Data
@Log
public class ProfitSeekingVGP implements GpProblem<Vector> {
	

	private Table<Vector> table;
	private ISeq<Op<Vector>> terminals;
	private ISeq<Op<Vector>> operations;
	private int depth;
	private Predicate<? super ProgramChromosome<Vector>> validator;
	
	private boolean compoundMode;
	
	private static final double INITIAL_INVESTMENT = 10_000;
	private static final double TRANSACTION_FEE = 0.00;
	private static final double LEVERAGE = 1;
	private static final double PENALIZE = 0.1;

	
	
	/**
	 * 
	 * @param table table with the data
	 * @param terminals data
	 * @param operations operations
	 * @param depth initial tree depth
	 * @param validator 
	 * @param compoundMode
	 */
	public ProfitSeekingVGP(Table<Vector> table, 
			ISeq<Op<Vector>> terminals, 
			ISeq<Op<Vector>> operations, 
			int depth,
			Predicate<? super ProgramChromosome<Vector>> validator,
			boolean compoundMode) {
		this.table = table;
		this.terminals = terminals;
		this.operations = operations;
		this.depth = depth;
		this.validator = validator;
		this.compoundMode = compoundMode;
		log.info("Iniciatized problem");
	}

	@Override
	public Function<Tree<Op<Vector>, ?>, Double> fitness() {
		return (agent) -> this.simulateMarket(agent, true, null);
	}

	@Override
	public Codec<Tree<Op<Vector>, ?>, ProgramGene<Vector>> codec() {
		return  Codec.of(
					Genotype.of(
							ProgramChromosome.of(
									depth,
									validator,
									operations,
									terminals
									)
							),
				Genotype::gene);
	}

	@Override
	public Map<ValidationMetric, List<Double>> validate(Tree<Op<Vector>, ?> agent, boolean useTrainSet) {
		Map<ValidationMetric, List<Double>> output = new HashMap<>();
		output.putAll(Map.of(ValidationMetric.FITNESS, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.CONFIDENCE, new LinkedList<>()));
		
		double fitness = simulateMarket(agent, useTrainSet, (agentOutput, money)->{
			output.get(ValidationMetric.AGENT_OUTPUT).add((double)agentOutput.asMeanScalar());
			output.get(ValidationMetric.MONEY).add(money-INITIAL_INVESTMENT);
			output.get(ValidationMetric.CONFIDENCE).add((double)agentOutput.asMeanScalar());
		});
		output.get(ValidationMetric.FITNESS).add(fitness);
		
		return output;
	}

	@Override
	public ISeq<Op<Vector>> operations() {
		return this.operations;
	}

	@Override
	public ISeq<Op<Vector>> terminals() {
		return this.terminals;
	}

	@Override
	public Table<Vector> getTable() {
		return this.table;
	}

	@Override
	public EngineConfiguration getConf() {
		return new EngineConfiguration();
	}
	
	
	public Double simulateMarket(Tree<Op<Vector>, ?> agent, boolean useTrainData, BiConsumer<Vector, Double> interceptor) {
		Pair<Integer, Integer> data = useTrainData ? table.randomTrainSet((int)(table.getTrainSet().value() * 0.05)) : table.getValidationSet();
		
		double money = INITIAL_INVESTMENT;
		Action currentAction = Action.NOOP;
		double timewithoutaction = 0;
		double shares = 0;
		
		for(int i = data.key() ; i < data.value() ; i ++) {
			List<Vector> row = getTable().getRow(i);
			double currentPrice = row.get(table.columnIndexOf("close")).last();
			
			Vector intention = Program.eval(agent, row.toArray(new Vector[row.size()]));
			Action action = Action.getSignal(intention.asMeanScalar());
			
			if(action != currentAction && action != Action.NOOP) {
				if(currentAction != Action.NOOP) { //capitalize
					if(compoundMode)
						money = capitalize(money, currentAction, shares, currentPrice,timewithoutaction);
					else
						money += capitalize(money, currentAction, shares, currentPrice,timewithoutaction) - INITIAL_INVESTMENT;
				}	
				
				currentAction = action;
				timewithoutaction = 0;
				shares = (compoundMode ? money : INITIAL_INVESTMENT) * LEVERAGE / currentPrice;
			}else 
				timewithoutaction++;
			
			
			if(interceptor != null)
				interceptor.accept(intention, money);
		}
		
		double lastPrice = table.getRow(data.value()-1).get(table.columnIndexOf("close")).last(); 
		if(compoundMode)
			money = capitalize(money, currentAction, shares, lastPrice,timewithoutaction);
		else
			money += capitalize(money, currentAction, shares, lastPrice,timewithoutaction) - INITIAL_INVESTMENT;
		
		return money;
	}

	private double capitalize(double money, Action currentAction, double shares, double currentPrice, double timewithoutaction) {
		if(currentAction == Action.BUY) {
			return shares * currentPrice * (1D - TRANSACTION_FEE) - (timewithoutaction * PENALIZE);
		}else if (currentAction == Action.SELL) {
			return 2 * (compoundMode ? money : INITIAL_INVESTMENT) - shares * currentPrice * (1D - TRANSACTION_FEE) - (timewithoutaction * PENALIZE);
		}
		return money;
	}
	
	@ToString
	private enum Action{
		BUY,SELL,NOOP;
		
		public static Action getSignal(double agentOutput) {
			if(Double.isNaN(agentOutput))
				return NOOP;
			return agentOutput > 0 ? BUY : agentOutput < 0 ? SELL : NOOP;
		}
	}
}
