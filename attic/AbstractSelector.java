package agency.reproduce;

import java.util.stream.Stream;

import agency.Individual;
import agency.Population;

public abstract class AbstractSelector implements Selector {

Stream<Individual> sourceStream;

public AbstractSelector(Population pop) {
  sourceStream = pop.allIndividuals();
}

}
