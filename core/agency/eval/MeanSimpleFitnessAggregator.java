package agency.eval;

import java.util.OptionalDouble;
import java.util.stream.Stream;

import agency.Fitness;
import agency.SimpleFitness;

public class MeanSimpleFitnessAggregator implements FitnessAggregator {

	@Override
	public SimpleFitness reduce(Stream<Fitness> fitnesses) {
		OptionalDouble aggFitness = fitnesses
				.map(f -> (SimpleFitness) f)
				.mapToDouble(f -> f.getFitness())
				.average();
		
		if (aggFitness.isPresent()) {
			if (aggFitness.getAsDouble() == Double.NaN)
				throw new RuntimeException("NaN Fitness");
			else
				return new SimpleFitness(aggFitness.getAsDouble());
		} else {
			throw new RuntimeException("Fitness aggregated to null?");
		 }
		
	}

}
