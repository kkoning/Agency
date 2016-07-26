package agency.eval;

import java.util.stream.Stream;

import agency.Fitness;
import agency.XMLConfigurable;

public interface FitnessAggregator extends XMLConfigurable {
	Fitness reduce(Stream<Fitness> fitnesses);
}
