package pt.fcul.masters;

import java.util.function.Function;

import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import lombok.Data;
import pt.fcul.masters.memory.MemoryManager;

//pensar em cromossoma com 4 functs stoploss takeprofit buy sell
@Data
public class Environment<T> implements Problem<Tree<Op<T>, ?>, ProgramGene<T>, Double>{

	
	private MemoryManager memory;

	
	public Environment(MemoryManager memory) {
		this.memory = memory;
	}
	
	public double evaluate(Tree<Op<T>, ?> optree) {
		return new Market(true).trade(optree);
	}
	
	public double validate(Tree<Op<T>, ?> optree) {
		return new Market(false).trade(optree);
	}
	
	@Override
	public Function<Tree<Op<T>, ?>, Double> fitness() {
		return this::evaluate;
	}

	@Override
	public Codec<Tree<Op<T>, ?>, ProgramGene<T>> codec() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private class Market{

		private boolean useTestData;

		public Market(boolean useTestData) {
			this.useTestData = useTestData;
		}

		public double trade(Tree<Op<T>, ?> optree) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

}
