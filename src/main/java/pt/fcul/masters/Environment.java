package pt.fcul.masters;

import java.util.List;
import java.util.function.Function;

import lombok.Data;
import pt.fcul.master.utils.Signal;
import pt.fcul.masters.memory.MemoryManager;

@Data
public class Environment {

	private MemoryManager memory = new MemoryManager();

	//TODO output an order with/out stoploss and/or takeprofit
	private Function<List<Double>, Signal> convertToSignal;
	
	
	public Environment(Function<List<Double>, Signal> convertToSignal) {
		this.convertToSignal = convertToSignal;
	}
	
	public void addVar(Function<List<Double>, Double> tansformer, String varName) {
		memory.createValueFrom(tansformer, varName);
	}
	
	public static double evaluate() {
		//TODO
		return 0;
	}
	
	public static double validate() {
		//TODO
		return 0;
	}
	
	
	private class Processor{
		//TODO
		
		public void process() {
			//TODO
		}
	}
}
