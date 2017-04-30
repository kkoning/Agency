package agency.vector;

import static agency.Agency.log;

import java.util.logging.Level;

import agency.AbstractIndividual;

public class VectorIndividual<T> extends AbstractIndividual {
private static final long serialVersionUID = -5477712512507153440L;

T[] genome;

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
