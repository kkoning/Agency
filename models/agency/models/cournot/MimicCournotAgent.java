package agency.models.cournot;

import java.util.Optional;

public class MimicCournotAgent extends DirectlyEncodedCournotAgent {

  @Override
  public double produceQty(CournotMarketInfo cmi) {
    Optional<Double> lastStepAvgQty = cmi.averageOtherQty(this, 1);
    if (!lastStepAvgQty.isPresent())
      return super.produceQty(cmi);
    return lastStepAvgQty.get();
  }

}
