package agency.util;

import java.util.List;
import java.util.stream.Stream;

/**
 * Contains various summary statistics not available in Apache Commons Math which
 * may be useful
 *
 * @author kkoning
 */
public class Statistics {

public static double HHI(Number[] values) {
  double total = 0;
  double totalSquaredPercent = 0;

  // Start by determining total
  for (Number value : values) {
    total += value.doubleValue();
  }

  // Add squared percentages of each value
  for (Number value : values) {
    double percent = (value.doubleValue() / total) * 100;
    double percentSquared = percent * percent;
    totalSquaredPercent += percentSquared;
  }
  return totalSquaredPercent;
}

public static double HHI(List<? extends Number> values) {
  double total = 0;
  double totalSquaredPercent = 0;

  // Start by determining total
  for (Number value : values) {
    total += value.doubleValue();
  }

  // Add squared percentages of each value
  for (Number value : values) {
    double percent = (value.doubleValue() / total) * 100;
    double percentSquared = percent * percent;
    totalSquaredPercent += percentSquared;
  }
  return totalSquaredPercent;
}

public static double HHI(int[] values) {
  double total = 0;
  double totalSquaredPercent = 0;

  // Start by determining total
  for (int value : values) {
    total += value;
  }

  // Add squared percentages of each value
  for (int value : values) {
    double doubleValue = (double) value; // cast for floating point division
    double percent = (doubleValue / total) * 100;
    double percentSquared = percent * percent;
    totalSquaredPercent += percentSquared;
  }
  return totalSquaredPercent;
}

public static double HHI(double[] values) {
  double total = 0;
  double totalSquaredPercent = 0;

  // Start by determining total
  for (double value : values) {
    total += value;
  }

  // Add squared percentages of each value
  for (double value : values) {
    double percent = (value / total) * 100;
    double percentSquared = percent * percent;
    totalSquaredPercent += percentSquared;
  }
  return totalSquaredPercent;
}



}
