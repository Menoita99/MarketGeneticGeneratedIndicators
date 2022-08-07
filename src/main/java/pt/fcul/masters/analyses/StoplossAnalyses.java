package pt.fcul.masters.analyses;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.plotter.gui.Plotter;

import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.IO;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.util.Vector;

public class StoplossAnalyses {

	private final static String  folder = "C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\07-07-2022 13_13_19\\";
	
	@SuppressWarnings("unchecked")
	public static void main(String[] a) throws IOException {
		Tree<Op<Vector>, ?> agent = (Tree<Op<Vector>, ?>) IO.object.read(folder+"individual.gp");
		VectorTable table = VectorTable.fromCsv(new File(folder+"data.csv").toPath());
		
		List<Double> stoploss = new LinkedList<>();
		List<Double> takeprofit = new LinkedList<>();
		boolean useTrainData = false;
		
		for(double d = 0.01; d < 1; d+=0.01) {
			MarketSimulator<Vector> market = MarketSimulator.<Vector>builder(table)
					.penalizerRate(0.1)
					.compoundMode(true)
					.stoploss(d)
					.trainSlice(table.getTrainSet())
					.build();
			
			stoploss.add(market.simulateMarket(args -> MarketAction.asSignal(Program.eval(agent, args).asMeanScalar()), useTrainData, null));
		}
		
		for(double d = 0.01; d < 1; d+=0.01) {
			MarketSimulator<Vector> market = MarketSimulator.<Vector>builder(table)
					.penalizerRate(0.1)
					.compoundMode(true)
					.takeprofit(d)
					.trainSlice(table.getTrainSet())
					.build();
			
			takeprofit.add(market.simulateMarket(args -> MarketAction.asSignal(Program.eval(agent, args).asMeanScalar()), useTrainData, null));
		}
		
		Plotter.builder().lineChart(stoploss, "stoploss").build().plot();
		Plotter.builder().lineChart(takeprofit, "takeprofit").build().plot();
	}
}
