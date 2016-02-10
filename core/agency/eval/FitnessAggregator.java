package agency.eval;

import java.util.stream.Stream;

import agency.Fitness;

public interface FitnessAggregator {
	Fitness reduce(Stream<Fitness> fitnesses);
}
