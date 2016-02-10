package agency.vector;

import static agency.Agency.log;
import java.util.logging.Level;
import agency.AbstractIndividual;

public class VectorIndividual<T> extends AbstractIndividual {
  private static final long serialVersionUID = -5477712512507153440L;

  T[] genome;

  @SuppressWarnings("unchecked")
  public VectorIndividual(int genomeSize) {
    genome = (T[]) new Object[genomeSize];
  }

  public Object[] getGenome() {
    return genome;
  }
  
  public int getGenomeLength() {
    if (genome != null)
      return genome.length;
    return 0;
  }
  
  public T get(int pos) {
    // TODO add optional bounds checking
    return genome[pos];
  }
  
  public void set(int pos, Object object) {
    // TODO add optional bounds checking
    genome[pos] = (T) object;
  }

  public T getGenomeAt(int position) {
    T toReturn = null;
    try {
      Object[] g = getGenome();
      toReturn = (T) g[position];
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
        sb.append(getGenomeAt(i));
        if ((i + 1) < getGenomeLength())
          sb.append(",");
      }
    }

    sb.append("]");
    if (this.getFitness().isPresent())
      sb.append(",fit=" + this.getFitness());

    return sb.toString();
  }

}
