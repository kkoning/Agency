package agency.vector;

import agency.util.RangedVector;
import agency.util.VectorRange;
import org.apache.commons.lang3.Range;

/**
 * Created by liara on 11/7/16.
 */
public class VectorMutator extends RangedVector {


public VectorIndividual<Object> mutate(VectorIndividual<Object> individual) {
  for (VectorRange<?> vr : vectorRangeList) {
    // Each vector range should also be a VectorValueMutator.
    VectorValueMutator vm = (VectorValueMutator) vr;

    Range<Integer> range = vr.getRange();
    for (int pos = range.getMinimum(); pos <= range.getMaximum(); pos++) {
      vm.mutate(individual.getGenome(),pos);
    }
  }
  return individual;
}


}
