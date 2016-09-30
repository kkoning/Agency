package agency.reproduce;

import java.util.Comparator;
import java.util.Optional;

import agency.Fitness;
import agency.Individual;

/**
 * Used to sort individuals in order of decreasing fitness.
 *
 * @author kkoning
 */
class FitnessComparator implements Comparator<Individual> {
@Override
public int compare(Individual i1, Individual i2) {
  int naturalOrder = 0;
  Optional<Fitness> f1 = i1.getFitness();
  Optional<Fitness> f2 = i2.getFitness();
  if (f1.isPresent() && f2.isPresent()) {
    naturalOrder = f1.get().compareTo(f2.get());
  } else if (f1.isPresent()) {
    naturalOrder = 1;
  } else if (f2.isPresent()) {
    naturalOrder = -1;
  }

  // but we want largest values first, so invert
  return naturalOrder * -1;
}
}