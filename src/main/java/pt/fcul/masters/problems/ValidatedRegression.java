package pt.fcul.masters.problems;

import static java.lang.Math.pow;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.regression.Error;
import io.jenetics.prog.regression.Regression;
import io.jenetics.prog.regression.Sample;
import io.jenetics.prog.regression.SampleBuffer;
import io.jenetics.prog.regression.Sampling;
import io.jenetics.prog.regression.Sampling.Result;
import io.jenetics.util.ISeq;

public class ValidatedRegression<T>  implements Problem<Tree<Op<T>, ?>, ProgramGene<T>, Double>, Validator<EvolutionResult<ProgramGene<T>, ?>, Double>{

	private Regression<T> regression;
	private SampleBuffer<T> validationSampling;
	private Error<T> error;

	private ValidatedRegression(Regression<T> regression, SampleBuffer<T> validationSampling,Error<T> error) {
		this.regression = regression;
		this.validationSampling = validationSampling;
		this.error = error;
		
		this.validationSampling.publish();
	}


	@Override
	public Double validate(EvolutionResult<ProgramGene<T>, ?> evolutionResult) {
		ProgramGene<T> program =  evolutionResult.bestPhenotype().genotype().gene();
		final Result<T> result = validationSampling.eval(program);
		if(result != null)
			return  error.apply(program,result.calculated(), result.expected());
		throw new IllegalArgumentException("Null result");
	}

	@Override
	public Function<Tree<Op<T>, ?>, Double> fitness() {
		return regression.fitness();
	}

	@Override
	public Codec<Tree<Op<T>, ?>, ProgramGene<T>> codec() {
		return regression.codec();
	}


	/* *************************************************************************
	 * Factory methods.
	 * ************************************************************************/

	/**
	 * Create a new regression problem instance with the given parameters.
	 *
	 * @see #codecOf(ISeq, ISeq, int)
	 * @see #codecOf(ISeq, ISeq, int, Predicate)
	 *
	 * @param <T> the operation type
	 * @param codec the problem codec to use
	 * @param error the error function
	 * @param sampling the sampling function
	 * @return a new regression problem instance
	 * @throws NullPointerException if on of the arguments is {@code null}
	 */
	public static <T> ValidatedRegression<T> of(
			final Codec<Tree<Op<T>, ?>, ProgramGene<T>> codec,
			final Error<T> error,
			final Sampling<T> sampling,
			final SampleBuffer<T> validatedSampling
			) {
		
		return new ValidatedRegression<>(Regression.of(codec, error, sampling),validatedSampling,error);
	}

	/**
	 * Create a new regression problem instance with the given parameters.
	 *
	 * @see #codecOf(ISeq, ISeq, int)
	 * @see #codecOf(ISeq, ISeq, int, Predicate)
	 *
	 * @param <T> the operation type
	 * @param codec the problem codec to use
	 * @param error the error function
	 * @param samples the sample points used for regression analysis
	 * @return a new regression problem instance
	 * @throws IllegalArgumentException if the given {@code samples} is empty
	 * @throws NullPointerException if on of the arguments is {@code null}
	 */
	public static <T> ValidatedRegression<T> ofIterable(
			final Codec<Tree<Op<T>, ?>, ProgramGene<T>> codec,
			final Error<T> error,
			final Iterable<? extends Sample<T>> samples,
					final Iterable<? extends Sample<T>> validationSamples
			) {
		if (!samples.iterator().hasNext()) 
			throw new IllegalArgumentException("Sample list must not be empty.");

		if (!validationSamples.iterator().hasNext()) 
			throw new IllegalArgumentException("Validation samples list must not be empty.");

		final List<Sample<T>> s = new ArrayList<>();
		samples.forEach(s::add);

		final List<Sample<T>> vs = new ArrayList<>();
		validationSamples.forEach(vs::add);

		return ofLists(codec,error,s,vs);
	}


	public static <T> ValidatedRegression<T>  ofLists(
			final Codec<Tree<Op<T>, ?>, ProgramGene<T>> codec,
			final Error<T> error,
			final List<Sample<T>> samples,
			final List<Sample<T>> validationSamples
			) {
		SampleBuffer<T> samplesBuffer = new SampleBuffer<>(samples.size());
		samplesBuffer.addAll(samples);
		samplesBuffer.publish();

		SampleBuffer<T> validationSamplesBuffer = new SampleBuffer<>(validationSamples.size());
		validationSamplesBuffer.addAll(validationSamples);
		samplesBuffer.publish();

		return of(codec, error, samplesBuffer,validationSamplesBuffer);
	}


	/* *************************************************************************
	 * Codec factory methods.
	 * ************************************************************************/

	/**
	 * Create a new <em>codec</em>, usable for <em>symbolic regression</em>
	 * problems, with the given parameters.
	 *
	 * @param <T> the operation type
	 * @param operations the operations used for the symbolic regression
	 * @param terminals the terminal operations of the program tree
	 * @param depth the maximal tree depth (height) of newly created program
	 *        trees
	 * @param validator the chromosome validator. A typical validator would
	 *        check the size of the tree and if the tree is too large, mark it
	 *        at <em>invalid</em>. The <em>validator</em> may be {@code null}.
	 * @return a new codec, usable for symbolic regression
	 * @throws IllegalArgumentException if the tree {@code depth} is not in the
	 *         valid range of {@code [0, 30)}
	 * @throws NullPointerException if the {@code operations} or {@code terminals}
	 *         are {@code null}
	 */
	public static <T> Codec<Tree<Op<T>, ?>, ProgramGene<T>>
	codecOf(
			final ISeq<Op<T>> operations,
			final ISeq<Op<T>> terminals,
			final int depth,
			final Predicate<? super ProgramChromosome<T>> validator
			) {
		if (depth >= 30 || depth < 0) {
			throw new IllegalArgumentException(format(
					"Tree depth out of range [0, 30): %d", depth
					));
		}

		return Codec.of(
				Genotype.of(
						ProgramChromosome.of(
								depth,
								validator,
								operations,
								terminals
								)
						),
				(g) -> g.gene()
				);
	}


	/**
	 * Create a new <em>codec</em>, usable for <em>symbolic regression</em>
	 * problems, with the given parameters.
	 *
	 * @param <T> the operation type
	 * @param operations the operations used for the symbolic regression
	 * @param terminals the terminal operations of the program tree
	 * @param depth the maximal tree depth (height) of newly created program
	 *        trees
	 * @return a new codec, usable for symbolic regression
	 * @throws IllegalArgumentException if the tree {@code depth} is not in the
	 *         valid range of {@code [0, 30)}
	 * @throws NullPointerException if the {@code operations} or {@code terminals}
	 *         are {@code null}
	 */
	public static <T> Codec<Tree<Op<T>, ?>, ProgramGene<T>>
	codecOf(
			final ISeq<Op<T>> operations,
			final ISeq<Op<T>> terminals,
			final int depth
			) {
		// Average arity of tree nodes.
		final double k = operations.stream()
				.collect(Collectors.averagingDouble(Op::arity));

		// The average node count between treeDepth and treeDepth + 1.
		// 2^(k + 1) - 1 + 2^(k + 2) - 1)/2 == 3*2^k - 1
		final int max = (int)(3*pow(k, depth) - 1);

		return codecOf(
				operations,
				terminals,
				depth,
				ch -> ch.root().size() <= max
				);
	}
}
