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
		Tree<Op<Vector>, ?> agent = (Tree<Op<Vector>, ?>) IO.object.read("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\17-04-2022 17_42_32\\individual.gp");
		VectorTable table = VectorTable.fromCsv(new File("C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\17-04-2022 17_42_32\\data.csv").toPath());
		ProfitSeekingVGP problem = new ProfitSeekingVGP(table, null, null, 0, null, false);
		System.out.println(problem.simulateMarketWithSimulator(agent, false, null));
	}
}
