package pt.fcul.masters.utils;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.jenetics.prog.ProgramGene;
import io.jenetics.util.RandomRegistry;
import pt.fcul.masters.logger.EngineConfiguration;
import pt.fcul.masters.vgp.util.ComplexVector;
import pt.fcul.masters.vgp.util.Vector;

public final class Constants {
	
	public final static Random RAND = RandomRegistry.random();
	
	public final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static final EngineConfiguration<ProgramGene<Vector>, Double> VECTORIAL_CONF = EngineConfiguration.standart();
	
	public static final EngineConfiguration<ProgramGene<ComplexVector>, Double> COMPLEX_VECTORIAL_CONF = EngineConfiguration.standart();
	
	public static final AtomicInteger GENERATION = new AtomicInteger(0);
	
	public static final int TRAIN_SLICES = 1;
	
	private Constants() {}
}
