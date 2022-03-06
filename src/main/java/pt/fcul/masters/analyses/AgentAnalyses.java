package pt.fcul.masters.analyses;

import java.io.File;
import java.io.IOException;

import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.Op;
import io.jenetics.util.IO;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.problems.ProfitSeekingVGP;
import pt.fcul.masters.vgp.util.Vector;

public class AgentAnalyses {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		Tree<Op<Vector>, ?> agent = (Tree<Op<Vector>, ?>) IO.object.read("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\06-03-2022 12_06_47\\individual.gp");
		VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\USD_JPY H1 2019_1_1_ 0_0 VGP_13 DynamicDerivativeNormalizer_2500.csv").toPath());
		ProfitSeekingVGP problem = new ProfitSeekingVGP(table, null, null, 0, null, false);
		System.out.println(problem.simulateMarket(agent, true, null));
	}
}
