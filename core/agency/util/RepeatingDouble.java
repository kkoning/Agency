package agency.util;

import org.w3c.dom.Element;

/**
 * Created by liara on 11/4/16.
 */
public class RepeatingDouble extends VectorRange<Double> {

double value;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);
  String valueString = e.getAttribute("value");
  value = Double.parseDouble(valueString);
}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  e.setAttribute("value", Double.toString(value));
}

@Override
public Double get(int position) {
  return value;
}

}
