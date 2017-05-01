package agency.vector;

/**
 * Created by liara on 11/8/16.
 */

import agency.util.RangedVector;
import agency.util.VectorRange;
import org.apache.commons.lang3.Range;

/**
 * <p>This class is intended to be used by a VectorLimitationPipeline, and is
 * responsible for limiting individuals which are provided to it by the
 * pipeline's source.  Each of the VectorLimiter's children should be
 * VectorValueLimiters, which are responsible for performing the limitation
 * on a given range of the individual's genome.</p>
 *
 * <p>The function limit() calls each of these VectorValueLimiters in turn, and
 * returns the modified individual for output by the VectorLimitationPipeline.</p>
 */
public class VectorLimiter extends RangedVector {

/**
 * <p>Calls each of the child VectorValueLimiters in turn, and returns the
 * modified individual for output by the VectorLimitationPipeline.</p>
 *
 * @param individual
 * @return
 */
public VectorIndividual<Object> limit(VectorIndividual<Object> individual) {
  for (VectorRange<?> vr : vectorRangeList) {
    // Each vector range should also be a VectorValueMutator.
    VectorValueLimiter vvl = (VectorValueLimiter) vr;

    Range<Integer> range = vr.getRange();
    for (int pos = range.getMinimum(); pos <= range.getMaximum(); pos++) {
      vvl.limit(individual.getGenome(), pos);
    }
  }
  return individual;
}


}
