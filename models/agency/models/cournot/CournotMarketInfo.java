package agency.models.cournot;

import java.util.Optional;

public class CournotMarketInfo {

private CournotGame model;

CournotMarketInfo(CournotGame model) {
  this.model = model;
}

public Optional<Double> averageOtherQty(CournotAgent self, int stepsAgo) {
  if (stepsAgo < 1) // Only allow inquiries for the past.
    return Optional.empty();
  
  if (stepsAgo > model.currentStep) // Can only go to beginning of time.
    return Optional.empty();
  
  // Figure out which step, specifically, we're getting info for.
  int step = model.currentStep - stepsAgo;
  
  double totalQty = 0d;
  int selfIdx = model.agents.indexOf(self);
  for (int i = 0; i < model.agents.size(); i++) {
    if (i == selfIdx)  // Don't include our own qty.
      continue;
    totalQty += model.quantities[step][i];
  }
  double averageOtherQty = totalQty / (model.agents.size() - 1);
  return Optional.of(averageOtherQty);
}
  
public Optional<Double> getPrice(int stepsAgo) {
  if (stepsAgo < 1) // Only allow inquiries for the past.
    return Optional.empty();
  
  if (stepsAgo > model.currentStep) // Can only go to beginning of time.
    return Optional.empty();
  
  // Figure out which step, specifically, we're getting info for.
  int step = model.currentStep - stepsAgo;
  
  return Optional.of(model.prices[step]);
}


}
