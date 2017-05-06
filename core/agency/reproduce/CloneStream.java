package agency.reproduce;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
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

public class CloneStream implements Stream<Individual> {

Stream<Individual> outStream;

public CloneStream(Stream<Individual> source) {
  outStream = source.map(CloneStream::cloneIndividual);
}

static Individual cloneIndividual(Individual source) {
  Individual clone = source.copy();
  clone.setUUID(UUID.randomUUID());
  List<UUID> parentIDs = new ArrayList<>(1);
  clone.setParentIDs(parentIDs);
  return clone;
}

public Iterator<Individual> iterator() {
  return outStream.iterator();
}

public Spliterator<Individual> spliterator() {
  return outStream.spliterator();
}

public boolean isParallel() {
  return outStream.isParallel();
}

public Stream<Individual> sequential() {
  return outStream.sequential();
}

public Stream<Individual> parallel() {
  return outStream.parallel();
}

public Stream<Individual> unordered() {
  return outStream.unordered();
}

public Stream<Individual> onClose(Runnable closeHandler) {
  return outStream.onClose(closeHandler);
}

public void close() {
  outStream.close();
}

public Stream<Individual> filter(Predicate<? super Individual> predicate) {
  return outStream.filter(predicate);
}

public <R> Stream<R> map(Function<? super Individual, ? extends R> mapper) {
  return outStream.map(mapper);
}

public IntStream mapToInt(ToIntFunction<? super Individual> mapper) {
  return outStream.mapToInt(mapper);
}

public LongStream mapToLong(ToLongFunction<? super Individual> mapper) {
  return outStream.mapToLong(mapper);
}

public DoubleStream mapToDouble(ToDoubleFunction<? super Individual> mapper) {
  return outStream.mapToDouble(mapper);
}

public <R> Stream<R> flatMap(Function<? super Individual, ? extends Stream<? extends R>> mapper) {
  return outStream.flatMap(mapper);
}

public IntStream flatMapToInt(Function<? super Individual, ? extends IntStream> mapper) {
  return outStream.flatMapToInt(mapper);
}

public LongStream flatMapToLong(Function<? super Individual, ? extends LongStream> mapper) {
  return outStream.flatMapToLong(mapper);
}

public DoubleStream flatMapToDouble(Function<? super Individual, ? extends DoubleStream> mapper) {
  return outStream.flatMapToDouble(mapper);
}

public Stream<Individual> distinct() {
  return outStream.distinct();
}

public Stream<Individual> sorted() {
  return outStream.sorted();
}

public Stream<Individual> sorted(Comparator<? super Individual> comparator) {
  return outStream.sorted(comparator);
}

public Stream<Individual> peek(Consumer<? super Individual> action) {
  return outStream.peek(action);
}

public Stream<Individual> limit(long maxSize) {
  return outStream.limit(maxSize);
}

public Stream<Individual> skip(long n) {
  return outStream.skip(n);
}

public void forEach(Consumer<? super Individual> action) {
  outStream.forEach(action);
}

public void forEachOrdered(Consumer<? super Individual> action) {
  outStream.forEachOrdered(action);
}

public Object[] toArray() {
  return outStream.toArray();
}

public <A> A[] toArray(IntFunction<A[]> generator) {
  return outStream.toArray(generator);
}

public Individual reduce(Individual identity, BinaryOperator<Individual> accumulator) {
  return outStream.reduce(identity, accumulator);
}

public Optional<Individual> reduce(BinaryOperator<Individual> accumulator) {
  return outStream.reduce(accumulator);
}

public <U> U reduce(U identity, BiFunction<U, ? super Individual, U> accumulator, BinaryOperator<U> combiner) {
  return outStream.reduce(identity, accumulator, combiner);
}

public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Individual> accumulator,
                     BiConsumer<R, R> combiner) {
  return outStream.collect(supplier, accumulator, combiner);
}

public <R, A> R collect(Collector<? super Individual, A, R> collector) {
  return outStream.collect(collector);
}

public Optional<Individual> min(Comparator<? super Individual> comparator) {
  return outStream.min(comparator);
}

public Optional<Individual> max(Comparator<? super Individual> comparator) {
  return outStream.max(comparator);
}

public long count() {
  return outStream.count();
}

public boolean anyMatch(Predicate<? super Individual> predicate) {
  return outStream.anyMatch(predicate);
}

public boolean allMatch(Predicate<? super Individual> predicate) {
  return outStream.allMatch(predicate);
}

public boolean noneMatch(Predicate<? super Individual> predicate) {
  return outStream.noneMatch(predicate);
}

public Optional<Individual> findFirst() {
  return outStream.findFirst();
}

public Optional<Individual> findAny() {
  return outStream.findAny();
}


}
