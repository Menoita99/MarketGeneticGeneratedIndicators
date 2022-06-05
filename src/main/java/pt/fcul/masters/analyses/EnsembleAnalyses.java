package pt.fcul.masters.analyses;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.plotter.gui.Plotter;
import com.plotter.gui.model.Serie;

import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Program;
import io.jenetics.util.IO;
import pt.fcul.masters.data.normalizer.DynamicStepNormalizer;
import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.db.model.Market;
import pt.fcul.masters.db.model.TimeFrame;
import pt.fcul.masters.logger.ValidationMetric;
import pt.fcul.masters.market.MarketAction;
import pt.fcul.masters.market.MarketSimulator;
import pt.fcul.masters.market.Transaction;
import pt.fcul.masters.table.DoubleTable;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.utils.ColumnUtil;
import pt.fcul.masters.utils.Pair;
import pt.fcul.masters.vgp.util.Vector;

public class EnsembleAnalyses {

	private final static List<String> AGENTS_GP_1_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\05-06-2022 17_06_02\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_37_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_50_22\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 20_24_24\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 21_29_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 22_51_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 00_58_58\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 19_37_44\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 21_07_47\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 23_21_17\\individual.gp"
			);

	private final static List<String> AGENTS_GP_3_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 12_41_10\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 13_07_26\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 13_45_51\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 14_21_39\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 14_48_16\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 15_27_18\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 15_46_44\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 16_34_27\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 16_52_58\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 17_23_14\\individual.gp"
			);


	private final static List<String> AGENTS_VGP_1_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\05-06-2022 17_06_02\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_37_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_50_22\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 20_24_24\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 21_29_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 22_51_31\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 00_58_58\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 19_37_44\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 21_07_47\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 23_21_17\\individual.gp"
			);

	private final static List<String> AGENTS_VGP_3_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\22-05-2022 20_02_28\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 12_43_05\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 14_57_50\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 17_30_09\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 15_29_07\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 15_49_08\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 16_50_18\\individual.gp",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 17_33_50\\individual.gp"
			//			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 17_33_50\\individual.gp",
			//			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\22-05-2022 20_02_28\\individual.gp"
			);

	public static void main(String[] cmdargs) {
		Map<ValidationMetric,List<Double>> validation = new EnumMap<>(ValidationMetric.class);
		validation.putAll(Map.of(
				ValidationMetric.PRICE, new LinkedList<>(),
				ValidationMetric.MONEY, new LinkedList<>(),
				ValidationMetric.TRANSACTION, new LinkedList<>(),
				ValidationMetric.AGENT_OUTPUT, new LinkedList<>(),
				ValidationMetric.NORMALIZATION_CLOSE, new LinkedList<>()));

		//		double result = getGPResult(validation);
		double result = getVGPResult(validation);

		List<Double> output = validation.get(ValidationMetric.AGENT_OUTPUT).stream().map(d -> d.isInfinite() ? (d > 0 ? 1D : -1D) : d).toList();
		output.forEach(System.out::println);

		Serie<Integer, Double> agent = Serie.of("Agent", output);
		Serie<Integer, Double> price = Serie.of("Price", validation.get(ValidationMetric.PRICE));
		Serie<Integer, Double> money = Serie.of("Money ", validation.get(ValidationMetric.MONEY));
		Serie<Integer, Double> transaction = Serie.of("Transaction", validation.get(ValidationMetric.TRANSACTION));
		Serie<Integer, Double> normalization = Serie.of("Normalization", validation.get(ValidationMetric.NORMALIZATION_CLOSE));

		Plotter.builder().lineChart("Agent", agent).build().plot();
		Plotter.builder().multiLineChart("Price/Money", price, money).build().plot();
		Plotter.builder().multiLineChart("Price/Transaction", price, transaction).build().plot();
		Plotter.builder().multiLineChart("Money/Transaction", money, transaction).build().plot();
		Plotter.builder().multiLineChart("Normalization/price", normalization, price).build().plot();
		Plotter.builder().multiLineChart("Normalization/Transaction", normalization, transaction).build().plot();

		System.out.println(result);
	}

	private static double getGPResult(Map<ValidationMetric, List<Double>> validation) {
		DoubleTable table = getGPTable();
		MarketSimulator<Double> market = MarketSimulator.<Double>builder(table).penalizerRate(0.1).compoundMode(true).stoploss(0.025).takeprofit(0.5).trainSlice(new Pair<>(0, table.getHBuffer().size())).build();
		List<Tree<Op<Double>, ?>> agents = getGPAgents();
		return market.simulateMarket((args)->{
			double eval = 0;
			for (Tree<Op<Double>, ?> agent : agents) {
				Double agentOutput =  Program.eval(agent, args);
				agentOutput = agentOutput.isNaN() ? 0D : agentOutput;
				agentOutput =  agentOutput.isInfinite() ? (agentOutput > 0 ? 1D : -1D) : agentOutput;
				eval+= agentOutput;
			}

			validation.get(ValidationMetric.AGENT_OUTPUT).add(eval);
			return MarketAction.asSignal(eval);
		}, true, m -> {
			validation.get(ValidationMetric.NORMALIZATION_CLOSE).add(m.getCurrentRow().get(m.getTable().columnIndexOf("closeNorm")));
			validation.get(ValidationMetric.MONEY).add(m.getCurrentMoney());
			validation.get(ValidationMetric.PRICE).add(m.getCurrentPrice());
			Transaction currentTransaction = m.getCurrentTransaction();
			validation.get(ValidationMetric.TRANSACTION).add(currentTransaction == null || currentTransaction.isClose() ? 0D : currentTransaction.getType() == MarketAction.BUY ? 1D : -1D);
		});
	}

	@SuppressWarnings("unchecked")
	private static List<Tree<Op<Double>, ?>> getGPAgents() {
		List<Tree<Op<Double>, ?>> agents = new LinkedList<>();
		try {
			for (String path : AGENTS_VGP_1_SLICE_PATH) 
				agents.add((Tree<Op<Double>, ?>) IO.object.read(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return agents;
	}

	private static DoubleTable getGPTable() {
		Normalizer normalizer = new DynamicStepNormalizer(25*6); //6 meses
		DoubleTable table = new DoubleTable(Market.SBUX,TimeFrame.D,LocalDateTime.of(2012, 1, 1, 0, 0));
		table.addColumn(normalizer.apply(table.getColumn("close")), "closeNorm");
		ColumnUtil.addEma(table,"closeNorm",200);
		ColumnUtil.addEma(table,"closeNorm",50);
		ColumnUtil.addEma(table,"closeNorm",13);
		ColumnUtil.addEma(table,"closeNorm",5);
		table.createValueFrom((row, index) -> row.get(table.columnIndexOf("ema5")) - row.get(table.columnIndexOf("ema13")), "smallEmaDiff");
		table.createValueFrom((row, index) -> row.get(table.columnIndexOf("ema50")) - row.get(table.columnIndexOf("ema200")), "bigEmaDiff");
		table.removeRows(0, 200);
		return table;
	}


	private static double getVGPResult(Map<ValidationMetric, List<Double>> validation) {
		VectorTable table = getVGPTable();
		MarketSimulator<Vector> market = MarketSimulator.<Vector>builder(table).penalizerRate(0.1).compoundMode(true).stoploss(0.025).takeprofit(0.5).trainSlice(new Pair<>(0, table.getHBuffer().size())).build();
		List<Tree<Op<Vector>, ?>> agents = getVGPAgents();
		return market.simulateMarket((args)->{
			double eval = 0;
			for (Tree<Op<Vector>, ?> agent : agents) {
				Double agentOutput =  Program.eval(agent, args).asMeanScalar();
				agentOutput = agentOutput.isNaN() ? 0D : agentOutput;
				agentOutput =  agentOutput.isInfinite() ? (agentOutput > 0 ? 1D : -1D) : agentOutput;
				eval+= agentOutput;
			}

			validation.get(ValidationMetric.AGENT_OUTPUT).add(eval);
			return MarketAction.asSignal(eval);
		}, true, m -> {
			validation.get(ValidationMetric.NORMALIZATION_CLOSE).add(m.getCurrentRow().get(m.getTable().columnIndexOf("closeNorm")).last());
			validation.get(ValidationMetric.MONEY).add(m.getCurrentMoney());
			validation.get(ValidationMetric.PRICE).add(m.getCurrentPrice());
			Transaction currentTransaction = m.getCurrentTransaction();
			validation.get(ValidationMetric.TRANSACTION).add(currentTransaction == null || currentTransaction.isClose() ? 0D : currentTransaction.getType() == MarketAction.BUY ? 1D : -1D);
		});
	}

	@SuppressWarnings("unchecked")
	private static List<Tree<Op<Vector>, ?>> getVGPAgents() {
		List<Tree<Op<Vector>, ?>> agents = new LinkedList<>();
		try {
			for (String path : AGENTS_VGP_3_SLICE_PATH) 
				agents.add((Tree<Op<Vector>, ?>) IO.object.read(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return agents;
	}

	private static VectorTable getVGPTable() {
		VectorTable table = new VectorTable(Market.SBUX,TimeFrame.D,LocalDateTime.of(2012, 1, 1, 0, 0),21, new DynamicStepNormalizer(25*6));
		ColumnUtil.addEma(table,"closeNorm",200,21);
		ColumnUtil.addEma(table,"closeNorm",50,21);
		ColumnUtil.addEma(table,"closeNorm",13,21);
		ColumnUtil.addEma(table,"closeNorm",5,21);
		ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema5")).last() - row.get(table.columnIndexOf("ema13")).last(), "smallEmaDiff");
		ColumnUtil.add(table, 21, (row,index) -> row.get(table.columnIndexOf("ema50")).last() - row.get(table.columnIndexOf("ema200")).last(), "bigEmaDiff");
		table.removeRows(0, 200);
		return table;
	}
}
