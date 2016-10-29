package agency.vector;

import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agency.Individual;

public class IntegerVectorIndividualFactory extends VectorIndividualFactory<Integer> {

public IntegerVectorIndividualFactory(int size) {
  super(size);
}

Integer mutationRange;

int initializeMinimum;
int initializeMaximum;

OptionalInt minimumValue = OptionalInt.empty();
OptionalInt maximumValue = OptionalInt.empty();

@Override
public Integer initialValue(int position) {
  Random r = ThreadLocalRandom.current();
  int range = initializeMaximum - initializeMinimum + 1;
  int value = r.nextInt(range);
  return initializeMinimum + value;
}

@Override
public void mutate(Individual ind, int position) {
  Random r = ThreadLocalRandom.current();

  VectorIndividual<Integer> ivi = (VectorIndividual<Integer>) ind;

  int mutationAmount = r.nextInt(mutationRange * 2);
  mutationAmount -= mutationRange;
  Integer newValue = ivi.genome.get(position) + mutationAmount;
  ivi.genome.set(position, newValue);
  if (minimumValue.isPresent())
    if (newValue < minimumValue.getAsInt())
      ivi.genome.set(position, minimumValue.getAsInt());
  if (maximumValue.isPresent())
    if (newValue > maximumValue.getAsInt())
      ivi.genome.set(position, maximumValue.getAsInt());
}

public Integer getMutationRange() {
  return mutationRange;
}

public void setMutationRange(Integer mutationRange) {
  this.mutationRange = mutationRange;
}

public int getInitializeMinimum() {
  return initializeMinimum;
}

public void setInitializeMinimum(int initializeMinimum) {
  this.initializeMinimum = initializeMinimum;
}

public int getInitializeMaximum() {
  return initializeMaximum;
}

public void setInitializeMaximum(int initializeMaximum) {
  this.initializeMaximum = initializeMaximum;
}

public OptionalInt getMinimumValue() {
  return minimumValue;
}

public void setMinimumValue(OptionalInt minimumValue) {
  this.minimumValue = minimumValue;
}

public OptionalInt getMaximumValue() {
  return maximumValue;
}

public void setMaximumValue(OptionalInt maximumValue) {
  this.maximumValue = maximumValue;
}
}
