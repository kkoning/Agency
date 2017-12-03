package agency.models.cournot;

import java.util.Optional;

import agency.SimpleFirm;
import agency.vector.VectorIndividual;

public class EvolvingTriggerCournotAgent extends
SimpleFirm<VectorIndividual<Double>, CournotGame> implements CournotAgent {
  
public static final int POS_INITIAL_QTY = 0;
public static final int POS_THRESHOLD_QTY = 1;
public static final int POS_BELOW_QTY = 2;
public static final int POS_ABOVE_QTY = 3;


@Override
public void init() {
  // Nothing required.
}

@Override
public void step(CournotGame model, int step, Optional<Double> substep) {
  // Nothing required; see produceQty()

}

@Override
public double produceQty(CournotMarketInfo cmi) {

  Optional<Double> lastStepAvgQty = cmi.averageOtherQty(this, 1);
  if (!lastStepAvgQty.isPresent())
    return getManager().gene(POS_INITIAL_QTY);
  
  double threshold = getManager().gene(POS_THRESHOLD_QTY);

  if (lastStepAvgQty.get() > threshold)
    return getManager().gene(POS_ABOVE_QTY);
  else
    return getManager().gene(POS_BELOW_QTY);
  
}



}
