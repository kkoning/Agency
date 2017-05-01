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
 * coefficients. It is useful for when the scale of the ideal values is not 
 * or cannot be known beforehand.
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
