package agency.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomStream<T> {
	public Stream<T> randomStream(List<T> source) {
		Random r = ThreadLocalRandom.current();
		Stream<T> toReturn = IntStream
				.generate(() -> r.nextInt(source.size()))
				.mapToObj(i -> source.get(i));
		return toReturn;
	}
	
	public Stream<T> shuffledStream(List<T> source) {
		Random r = ThreadLocalRandom.current();
		List<T> copy = new ArrayList<>(source.size());
		copy.addAll(source);
		Collections.shuffle(copy, r);
		return copy.stream();
	}

}
