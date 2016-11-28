package agency.util;

import org.w3c.dom.Element;

/**
 * Created by liara on 11/4/16.
 */
public class RepeatingInteger extends VectorRange<Integer> {

int value;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);
  String valueString = e.getAttribute("value");
  value = Integer.parseInt(valueString);
}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  e.setAttribute("value", Integer.toString(value));
}

@Override
public Integer get(int position) {
  return value;
}

}
