package agency.util;

import org.w3c.dom.Element;

/**
 * Created by liara on 11/4/16.
 */
public class DoubleList extends VectorRange<Double> {

double[] values;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);
  String valuesString = e.getTextContent();
  String[] textValues = valuesString.split("\\s"); // Split on whitespace
  values = new double[textValues.length];
  for (int i = 0; i < textValues.length; i++) {
    values[i] = Double.parseDouble(textValues[i]);
  }
  // TODO: Confirm size is correct; don't allow user to over or underspecify
  // the number of items.  Ensure it is equal to the length of the range.

}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  StringBuffer sb = new StringBuffer();
  for (double val : values) {
    sb.append(Double.toString(val));
    sb.append(" "); // use spaces for whitespace.
  }
  e.setTextContent(sb.toString().trim());
}

@Override
public Double get(int position) {
  return values[effectivePos(position)];
}

}
