package agency.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import agency.Individual;
import agency.Population;

public class OtherTournamentSelector extends AbstractSelector {

	Stream<Individual> tournamentStream;
	int touramentSize;
	int topIndividuals = 1;
	Population pop;

	public OtherTournamentSelector(Population pop, int tournamentSize) {
		super(pop);
		this.pop = pop;
		this.touramentSize = tournamentSize;
		connectTournamentStream();
	}

	public OtherTournamentSelector(Population pop, int tournamentSize, int topIndividuals) {
		super(pop);
		this.pop = pop;
		this.touramentSize = tournamentSize;
		this.topIndividuals = topIndividuals;
		connectTournamentStream();
	}

	void connectTournamentStream() {
		tournamentStream = Stream.generate(new TournamentSupplier(pop,touramentSize, topIndividuals));
		
	}
	
	class TournamentSupplier implements Supplier<Individual> {
		int tournamentSize;
		int topIndividuals = 1;
		Queue<Individual> queue;
		Population population;
		
		public TournamentSupplier(Population pop, int tournamentSize, int topIndividuals) {
			super();
			if (topIndividuals >= tournamentSize)
				throw new RuntimeException("Tourament selection must choose fewer individuals (" +
						topIndividuals + ") than the tournament size (" +
						tournamentSize + ")");
			
			this.tournamentSize = tournamentSize;
			this.topIndividuals = topIndividuals;
			this.population = pop;
			queue = new ArrayBlockingQueue<>(tournamentSize);
		}

		@Override
		public Individual get() {
			if (queue.size() > 0)
				return queue.remove();
			
			ArrayList<Individual> tournament = new ArrayList<>(tournamentSize);
			population.randomIndividuals()
				.limit(tournamentSize)
				.forEach(ind -> tournament.add(ind));
			
			Collections.sort(tournament, new FitnessComparator());
			for (int i = 1; i < topIndividuals; i++) {
				queue.add(tournament.get(i));
			}
			return tournament.get(0);
		}
		
	}

	public Iterator<Individual> iterator() {
		return tournamentStream.iterator();
	}

	public Spliterator<Individual> spliterator() {
		return tournamentStream.spliterator();
	}

	public boolean isParallel() {
		return tournamentStream.isParallel();
	}

	public Stream<Individual> sequential() {
		return tournamentStream.sequential();
	}

	public Stream<Individual> parallel() {
		return tournamentStream.parallel();
	}

	public Stream<Individual> unordered() {
		return tournamentStream.unordered();
	}

	public Stream<Individual> onClose(Runnable closeHandler) {
		return tournamentStream.onClose(closeHandler);
	}

	public void close() {
		tournamentStream.close();
	}

	public Stream<Individual> filter(Predicate<? super Individual> predicate) {
		return tournamentStream.filter(predicate);
	}

	public <R> Stream<R> map(Function<? super Individual, ? extends R> mapper) {
		return tournamentStream.map(mapper);
	}

	public IntStream mapToInt(ToIntFunction<? super Individual> mapper) {
		return tournamentStream.mapToInt(mapper);
	}

	public LongStream mapToLong(ToLongFunction<? super Individual> mapper) {
		return tournamentStream.mapToLong(mapper);
	}

	public DoubleStream mapToDouble(ToDoubleFunction<? super Individual> mapper) {
		return tournamentStream.mapToDouble(mapper);
	}

	public <R> Stream<R> flatMap(Function<? super Individual, ? extends Stream<? extends R>> mapper) {
		return tournamentStream.flatMap(mapper);
	}

	public IntStream flatMapToInt(Function<? super Individual, ? extends IntStream> mapper) {
		return tournamentStream.flatMapToInt(mapper);
	}

	public LongStream flatMapToLong(Function<? super Individual, ? extends LongStream> mapper) {
		return tournamentStream.flatMapToLong(mapper);
	}

	public DoubleStream flatMapToDouble(Function<? super Individual, ? extends DoubleStream> mapper) {
		return tournamentStream.flatMapToDouble(mapper);
	}

	public Stream<Individual> distinct() {
		return tournamentStream.distinct();
	}

	public Stream<Individual> sorted() {
		return tournamentStream.sorted();
	}

	public Stream<Individual> sorted(Comparator<? super Individual> comparator) {
		return tournamentStream.sorted(comparator);
	}

	public Stream<Individual> peek(Consumer<? super Individual> action) {
		return tournamentStream.peek(action);
	}

	public Stream<Individual> limit(long maxSize) {
		return tournamentStream.limit(maxSize);
	}

	public Stream<Individual> skip(long n) {
		return tournamentStream.skip(n);
	}

	public void forEach(Consumer<? super Individual> action) {
		tournamentStream.forEach(action);
	}

	public void forEachOrdered(Consumer<? super Individual> action) {
		tournamentStream.forEachOrdered(action);
	}

	public Object[] toArray() {
		return tournamentStream.toArray();
	}

	public <A> A[] toArray(IntFunction<A[]> generator) {
		return tournamentStream.toArray(generator);
	}

	public Individual reduce(Individual identity, BinaryOperator<Individual> accumulator) {
		return tournamentStream.reduce(identity, accumulator);
	}

	public Optional<Individual> reduce(BinaryOperator<Individual> accumulator) {
		return tournamentStream.reduce(accumulator);
	}

	public <U> U reduce(U identity, BiFunction<U, ? super Individual, U> accumulator, BinaryOperator<U> combiner) {
		return tournamentStream.reduce(identity, accumulator, combiner);
	}

	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Individual> accumulator,
			BiConsumer<R, R> combiner) {
		return tournamentStream.collect(supplier, accumulator, combiner);
	}

	public <R, A> R collect(Collector<? super Individual, A, R> collector) {
		return tournamentStream.collect(collector);
	}

	public Optional<Individual> min(Comparator<? super Individual> comparator) {
		return tournamentStream.min(comparator);
	}

	public Optional<Individual> max(Comparator<? super Individual> comparator) {
		return tournamentStream.max(comparator);
	}

	public long count() {
		return tournamentStream.count();
	}

	public boolean anyMatch(Predicate<? super Individual> predicate) {
		return tournamentStream.anyMatch(predicate);
	}

	public boolean allMatch(Predicate<? super Individual> predicate) {
		return tournamentStream.allMatch(predicate);
	}

	public boolean noneMatch(Predicate<? super Individual> predicate) {
		return tournamentStream.noneMatch(predicate);
	}

	public Optional<Individual> findFirst() {
		return tournamentStream.findFirst();
	}

	public Optional<Individual> findAny() {
		return tournamentStream.findAny();
	}
	
	
	
	

}
