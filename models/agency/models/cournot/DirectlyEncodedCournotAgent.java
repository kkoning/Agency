package agency.models.cournot;

import java.util.Optional;

import agency.SimpleFirm;
import agency.vector.VectorIndividual;

public class DirectlyEncodedCournotAgent extends
    SimpleFirm<VectorIndividual<Double>, CournotGame> implements CournotAgent {

public static final int POS_QTY = 0;

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
  // Disregard any market information and produce a quantity specified in the
  // genome.
  double qty = getManager().gene(POS_QTY);
  return qty;
}

}
