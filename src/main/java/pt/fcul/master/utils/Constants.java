package pt.fcul.master.utils;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.jenetics.prog.ProgramGene;
import io.jenetics.util.RandomRegistry;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.vgp.util.Vector;

public final class Constants {
	
	public final static Random RAND = RandomRegistry.random();
	
	public final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static final EngineConfiguration<ProgramGene<Vector>, Double> CONF = EngineConfiguration.standart();
	
	private Constants() {}
}
