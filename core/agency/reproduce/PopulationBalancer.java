package agency.reproduce;

import agency.Population;
import agency.XMLConfigurable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 Calculates the relative target sizes of populations (i.e., multiple
 populations in the same PopulationGroup) based on some fitness/selection
 criteria.
 */
public interface PopulationBalancer extends XMLConfigurable {

/**
 @param populations the populations among which to balance
 @return an array of the relative # of individuals that each population
 should contain based on the fitness/selection criteria of the
 implementation.  E.g., tournament selection.
 */
double[] balance(List<Population> populations, Random random);


/**
 The same as balance(Population[] populations, Random random), just with a
 default source of randomness.

 @param populations
 @return
 */
default double[] balance(List<Population> populations) {
  return balance(populations, ThreadLocalRandom.current());
}


}
