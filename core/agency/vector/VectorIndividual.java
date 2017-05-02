package agency.vector;

import static agency.Agency.log;

import java.util.logging.Level;

import agency.AbstractIndividual;

public class VectorIndividual<T> extends AbstractIndividual {
private static final long serialVersionUID = 1;

private T[] genome;

@SuppressWarnings("unchecked")
public VectorIndividual(int genomeSize) {
  // T is guaranteed to be an object, not a primitive
  genome = (T[]) new Object[genomeSize];
}

public T[] getGenome() {
  return genome;
}

public int getGenomeLength() {
  if (genome != null)
    return genome.length;
  return 0;
}

public void changeGene(int position, T value) {
  genome[position] = value;
}

public T gene(int position) {
  T toReturn = null;
  try {
    T[] g = getGenome();
    toReturn = g[position];
  } catch (NullPointerException npe) {
    log.log(Level.SEVERE, "Attempt to access null genome", npe);
  } catch (ArrayIndexOutOfBoundsException aoobe) {
    log.log(Level.SEVERE, "Attempt to access past end of genome", aoobe);
  }
  return toReturn;
}

/**
 * This is a convenience method. It assumes the genome extends Number, and
 * returns Math.exp() of that gene.
 * 
 * @param position
 * @return
 */
public double e(int position) {
  T gene = gene(position);
  double toReturn = Double.NaN;
  try {
    Number n = (Number) gene;
    toReturn = Math.exp(n.doubleValue());
  } catch (ClassCastException cce) {
    throw new RuntimeException("Math.exp() requires a genome that inherits from Numeric");
  }
  return toReturn;
}

public void replaceGenome(T[] newGenome) {
  genome = newGenome;
}

/**
 * This is a convenience method that uses a region of the genome to evolve
 * coefficients in a linear equation over variables collected from the
 * simulation environment.
 * 
 * This version uses double exponentiated terms for both the constant and
 * coefficients. It is useful for when the scale of the ideal values is not or
 * cannot be known beforehand.
 * 
 * It uses (environmentVariables+1)*2 genome positions.
 * 
 * @param start
 * @param environmentVariables
 * @return
 */
public double linearEqExp(int start, double[] environmentVariables) {

  /*
   * To start, convert the genome into the required doubles.
   */
  // Allocate variables
  double constPos;
  double constNeg;
  double[] coefsPos = new double[environmentVariables.length];
  double[] coefsNeg = new double[environmentVariables.length];

  // Grab Constants
  constPos = e(start);
  constNeg = e(start + 1);

  // Grab Coefficients
  int pos = start + 2;
  for (int i = 0; i < environmentVariables.length; i++) {
    coefsPos[i] = e(pos++);
    coefsNeg[i] = e(pos++);
  }

  // Do multiplications and sum
  double toReturn = 0d;
  toReturn += constPos;
  toReturn -= constNeg;
  for (int i = 0; i < environmentVariables.length; i++) {
    toReturn += coefsPos[i] * environmentVariables[i];
    toReturn -= coefsNeg[i] * environmentVariables[i];
  }
  return toReturn;
}

/**
 * This is a convenience method that uses a region of the genome to evolve
 * coefficients in a linear equation over variables collected from the
 * simulation environment.
 * 
 * This is the simple version that uses the genome values for coefficients
 * directly. This requires the genome and mutation to have a scale that is
 * appropriate for the problem. E.g., if the mutation level is very small, but
 * the best coefficients are very large, it will take a very large number of
 * generations for these values to evolve.
 *
 * It requires environmentVariables + 1 positions on the genome; one for each
 * coefficient and one for the constant.
 * 
 * @param start
 * @param environmentVariables
 * 
 * @return constant + Sum(Coef_n * Var_n)
 */
public double linearEq(int start, double[] environmentVariables) {

  /*
   * To start, convert the genome into the required doubles.
   */
  // Allocate variables
  double constant = 0d;
  double[] coefs = new double[environmentVariables.length];

  // Grab Constants
  constant += ((Number) gene(start)).doubleValue();

  // Grab Coefficients
  for (int i = 0; i < environmentVariables.length; i++) {
    coefs[i] += ((Number) gene(start + 1 + i)).doubleValue();
  }

  // Do multiplications and sum
  double toReturn = 0d;
  toReturn += constant;
  for (int i = 0; i < environmentVariables.length; i++) {
    toReturn += coefs[i] * environmentVariables[i];
  }
  return toReturn;
}

/**
 * This is a utility function to associate a block of the genome with (1) a set
 * of tests based on environment variables and (2) a set of loci that will be
 * used when those conditions have a certain value.
 * 
 * This large block is broken up into two smaller blocks. The first small block
 * contains threshold values that will be compared against the values provided
 * by the environment. Each one of these thresholds is treated as a binary
 * digit, and together they provide the condition index. However, it is likely
 * that more than one loci will be used for each condition.
 * 
 * The number of genome locations this block takes is N + (2^N * Y), where N is
 * the number of conditions and Y is the number of loci per condition.
 * 
 * @param observations
 *          A set of observations from the environment.
 * @param conditionsPos
 *          The position in the genome this block of genes starts at.
 * @param lociPerCondition
 *          The number of genes associated with each condition.
 * @return the position in the genome indexing the set of genes for this
 *         condition.
 */
public int conditionIndexHelper(double[] observations,
                                int conditionsPos,
                                int lociPerCondition) {

  // Get thresholds from the genome.
  double[] thresholds = new double[observations.length];
  for (int i = 0; i < observations.length; i++) {
    Number threshold = Double.NaN;
    try {
      threshold = (Number) gene(conditionsPos + i);
    } catch (ClassCastException cce) {
      throw new RuntimeException("Cannot use "
          + "VectorIndividual.conditionIndexHelper on non-Numeric Values");
    }
    thresholds[i] = threshold.doubleValue();
  }

  int conditionIndex = conditionIndexer(thresholds, observations);
  int lociBlockOffset = conditionIndex * lociPerCondition;
  int totalOffset = conditionsPos + observations.length + lociBlockOffset;

  return totalOffset;
}

/**
 * This is a utility function to associate a block of the genome with (1) a set
 * of tests based on environment variables and (2) a set of loci that will be
 * used when those conditions have a certain value.
 * 
 * This large block is broken up into three smaller blocks. The first two are
 * related to determining the combination of conditions. The larger third block
 * contains <i>lociPerCondition</i> genes for every possible combination of
 * conditions. Each of the first two block is exactly <i>observations.length</i>
 * long. Each is exponentiated (raised to e^x) and multiplied by the values in
 * the observations[] array. The values from the second block are subtracted
 * from the first block, and the resulting numbers are considered the threshold
 * values. Each unique combination has lociPerCondition genes associated with
 * it. The function returns the index to that block of genes.
 * 
 * The number of genome locations this block takes is 2N + (2^N * M), where N is
 * the number of conditions (observations.length) and M is lociPerCondition.
 * 
 * @param observations
 *          A set of observations from the environment.
 * @param conditionsPos
 *          The position in the genome this block of genes starts at.
 * @param lociPerCondition
 *          The number of genes associated with each condition.
 * @return the position in the genome indexing the set of genes for this
 *         condition.
 */
public int conditionIndexHelperExp(double[] observations,
                                   int conditionsPos,
                                   int lociPerCondition) {

  // Get thresholds from the genome.
  double[] posThresholds = new double[observations.length];
  for (int i = 0; i < observations.length; i++) {
    Number threshold = Double.NaN;
    try {
      threshold = (Number) gene(conditionsPos + i);
    } catch (ClassCastException cce) {
      throw new RuntimeException("Cannot use "
          + "VectorIndividual.conditionIndexHelper on non-Numeric Values");
    }
    posThresholds[i] = threshold.doubleValue();
  }

  double[] negThresholds = new double[observations.length];
  for (int i = 0; i < observations.length; i++) {
    Number threshold = Double.NaN;
    try {
      threshold = (Number) gene(conditionsPos + observations.length + i);
    } catch (ClassCastException cce) {
      throw new RuntimeException("Cannot use "
          + "VectorIndividual.conditionIndexHelper on non-Numeric Values");
    }
    negThresholds[i] = threshold.doubleValue();
  }

  // Sum components of thresholds
  double[] thresholds = new double[observations.length];
  for (int i = 0; i < observations.length; i++) {
    thresholds[i] = Math.exp(posThresholds[i]) - Math.exp(negThresholds[i]);
  }

  int conditionIndex = conditionIndexer(thresholds, observations);
  int lociBlockOffset = conditionIndex * lociPerCondition;
  int totalOffset = conditionsPos + (observations.length * 2) + lociBlockOffset;

  return totalOffset;
}

static int conditionIndexer(double[] thresholds,
                            double[] observations) {

  // Sanity Checks
  if (thresholds.length != observations.length)
    throw new RuntimeException("The # of thresholds (" + thresholds.length
        + ") must always match the number of observations ("
        + observations.length + ")");
  // Sanity Checks
  if (thresholds.length > 30)
    throw new RuntimeException("Too many thresholds");

  int index = 0x0;
  for (int i = 0; i < thresholds.length; i++) {
    if (observations[i] > thresholds[i])
      index = index | (0x1 << (thresholds.length - 1 - i));
  }

  return index;
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("VectorIndividual#" + hashCode() + ":[");

  if (getGenome() == null) {
    sb.append("null");
  } else {
    for (int i = 0; i < getGenomeLength(); i++) {
      sb.append(gene(i));
      if ((i + 1) < getGenomeLength())
        sb.append(",");
    }
  }

  sb.append("]");
  if (this.getFitness() != null)
    sb.append(",fit=" + this.getFitness());

  return sb.toString();
}

}
