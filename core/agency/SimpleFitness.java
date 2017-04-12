package agency;

public class SimpleFitness implements Fitness {
public static final long serialVersionUID = 1L;

int    numSamples;
double totalFitness;

public SimpleFitness(double fitness) {
  this.totalFitness = fitness;
  numSamples = 1;
}

public Double getAverageFitness() {
  return totalFitness / numSamples;
}

@Override
public int compareTo(Fitness o) {
  if (o instanceof SimpleFitness) {
    SimpleFitness sf = (SimpleFitness) o;
    return Double.compare(this.getAverageFitness(), sf.getAverageFitness());
  } else {
    throw new RuntimeException("SimpleFitness can only be compared with other SimpleFitness objects");
  }
}

@Override
public String toString() {
  return "SimpleFitness{" +
          "numSamples=" + numSamples +
          ", totalFitness=" + totalFitness +
          '}';
}

@Override
public void combine(Fitness other) {
  SimpleFitness sf = (SimpleFitness) other;
  this.numSamples += sf.numSamples;
  this.totalFitness += sf.totalFitness;
}

}
