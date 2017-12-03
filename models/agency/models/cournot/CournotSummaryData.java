package agency.models.cournot;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import agency.util.Statistics;

public class CournotSummaryData {

double priceMean;
double priceSD;
double qtyMean;
double qtySD;
double totalRevenue;
double hhiMean;
double hhiSD;
double consumerSurplus;

public CournotSummaryData(CournotGame model) {
  DescriptiveStatistics prices = new DescriptiveStatistics();
  DescriptiveStatistics quantities = new DescriptiveStatistics();
  DescriptiveStatistics hhis = new DescriptiveStatistics();

  for (int i = 0; i < model.maxSteps; i++) {
    for (int j = 0; j < model.agents.size(); j++) {
      quantities.addValue(model.quantities[i][j]);
      this.totalRevenue += model.revenue[i][j];
    }
    prices.addValue(model.prices[i]);
    consumerSurplus += model.consumerSurplus[i];
    double hhi = Statistics.HHI(model.quantities[i]);
    hhis.addValue(hhi);
  }

  this.priceMean = prices.getMean();
  this.priceSD = prices.getStandardDeviation();
  this.qtyMean = quantities.getMean();
  this.qtySD = quantities.getStandardDeviation();
  this.hhiMean = hhis.getMean();
  this.hhiSD = hhis.getStandardDeviation();
  
  checkIllegalValues();
}

/**
 * Does what it says on the tin. This is useful primarily for debugging.
 */
void checkIllegalValues() {
  if (Double.isNaN(priceMean))
    throw new RuntimeException();
  if (Double.isNaN(priceSD))
    throw new RuntimeException();
  if (Double.isNaN(qtyMean))
    throw new RuntimeException();
  if (Double.isNaN(qtySD))
    throw new RuntimeException();
  if (Double.isNaN(totalRevenue))
    throw new RuntimeException();
  if (Double.isNaN(hhiMean))
    throw new RuntimeException();
  if (Double.isNaN(hhiSD))
    throw new RuntimeException();
  if (Double.isNaN(consumerSurplus))
    throw new RuntimeException();

  if (Double.isInfinite(priceMean))
    throw new RuntimeException();
  if (Double.isInfinite(priceSD))
    throw new RuntimeException();
  if (Double.isInfinite(qtyMean))
    throw new RuntimeException();
  if (Double.isInfinite(qtySD))
    throw new RuntimeException();
  if (Double.isInfinite(totalRevenue))
    throw new RuntimeException();
  if (Double.isInfinite(hhiMean))
    throw new RuntimeException();
  if (Double.isInfinite(hhiSD))
    throw new RuntimeException();
  if (Double.isInfinite(consumerSurplus))
    throw new RuntimeException();

}

}
