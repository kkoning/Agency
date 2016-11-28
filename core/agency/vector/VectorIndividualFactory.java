package agency.vector;

import agency.util.RangedVector;
import agency.IndividualFactory;

public class VectorIndividualFactory extends RangedVector
        implements IndividualFactory<VectorIndividual<Object>> {

/* The following method must be synchronized because it depends on the state of
 * the vector, which is changed each time create() is called with initialize().  This
 * shouldn't be too much of a performance hit because (1) paralellism can take place
 * at a higher level (across populations) and this is only called at initialization.
 */

public synchronized VectorIndividual<Object> create() {

  VectorIndividual<Object> ind = new VectorIndividual<Object>(getVectorLength());
  ind.genome = createVector();
  ind.setFitness(null);
  return ind;
}


}
