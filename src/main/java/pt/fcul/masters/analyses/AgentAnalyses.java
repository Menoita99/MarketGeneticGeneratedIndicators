package pt.fcul.masters.analyses;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.IO;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.util.Vector;

public class AgentAnalyses {
	
	private static List<Double> money = new LinkedList<>();
	private static List<Double> price = new LinkedList<>();
	private static List<Double> closeNorm = new LinkedList<>();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		Tree<Op<Vector>, ?> agent = (Tree<Op<Vector>, ?>) IO.object.read("C:\\Users\\Owner\\Desktop\\To analyse\\01-05-2022 14_29_56 vgp\\individual.gp");
		VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\To analyse\\01-05-2022 14_29_56 vgp\\data.csv").toPath());
//		ProfitSeekingVGP problem = new ProfitSeekingVGP(table, null, null, 0, null, false);
//		problem.simulateMarketWithSimulator(agent, false, AgentAnalyses::analyse);
		
		MarketSimulator<Vector> market = MarketSimulator.<Vector>builder(table)
				.penalizerRate(0.1)
				.compoundMode(true)
				.stoploss(0.025)
		//		.takeprofit(0.5)
				.trainSlice(table.getTrainSet())
				.build();
		
//		Serie<Integer, Double> agentValues = Serie.of
		List<Double> agentOutput = new LinkedList<>();
		
		System.out.println(market.simulateMarket(agentArgs -> generateSignal(agent, agentOutput, agentArgs), true, AgentAnalyses::analyse));
		System.out.println(market.simulateMarket(agentArgs -> generateSignal(agent, agentOutput, agentArgs), false, AgentAnalyses::analyse));
		
//		Serie<Integer, Integer> transactions = new Serie<>("Transactions");
//		
//		market.getTransactions().forEach( t -> transactions.add(t.getOpenIndex(), t.getType() == MarketAction.BUY ? 1 : t.getType() == MarketAction.SELL ? -1 : 0));
//		
//		Plotter.builder().lineChart(agentOutput, "Agent Output").build().plot();
//		Plotter.builder().lineChart(transactions, "Transactions").build().plot();
		Plotter.builder().multiLineChart("Money/ agent output",
				Serie.of("Money", money.subList(0, agentOutput.size())),
				Serie.of("AgentOutput", agentOutput)).build().plot();
		
		Plotter.builder().multiLineChart("Price/ agent output",
				Serie.of("Price", price.subList(0, agentOutput.size())),
				Serie.of("AgentOutput", agentOutput)).build().plot();
		
		Plotter.builder().multiLineChart("CloseNorm / agent output",
				Serie.of("CloseNorm", closeNorm.subList(0, agentOutput.size())),
				Serie.of("AgentOutput", agentOutput)).build().plot();
	}


	private static MarketAction generateSignal(Tree<Op<Vector>, ?> agent, List<Double> agentOutput, Vector[] agentArgs) {
		double value = Program.eval(agent, agentArgs).asMeanScalar();
		agentOutput.add(value);
		return  MarketAction.asSignal(value);
	}
	
	
	public static void analyse(MarketSimulator<Vector> market) {
		money.add(market.getCurrentMoney());
		price.add(market.getCurrentPrice());
		int idxCloseNorm = market.getTable().columnIndexOf("closeNorm");
		double closenorm = market.getCurrentRow().get(idxCloseNorm).last();
		closeNorm.add(closenorm);
	}
}
