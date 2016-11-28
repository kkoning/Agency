package agency.vector;

import agency.Config;
import agency.XMLConfigurable;
import agency.util.VectorRange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Optional;

/**
 * Created by liara on 11/20/16.
 */
public class RangeLimiter extends VectorRange<Number> implements VectorValueLimiter {

VectorRange<Number> maximums;
VectorRange<Number> minimums;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);

  Optional<Element> oe = Config.getChildElementWithTag(e, "Max");
  if (oe.isPresent()) {
    Element maxE = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(maxE);
    maximums = (VectorRange<Number>) xc;
  }
  oe = Config.getChildElementWithTag(e, "Min");
  if (oe.isPresent()) {
    Element minE = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(minE);
    minimums = (VectorRange<Number>) xc;
  }

  // Both a minimum and maximum are optional, but either one or the other must
  // be specified or there is no purpose to this element.
  if (maximums == null)
    if (minimums == null)
      throw new UnsupportedOperationException("RangeLimiter must have either a " +
        "maximum or a minimum");

  // TODO: Add check to make sure that the minimum is less than the maximum for all
  // values?

  // TODO:
}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);

  Document d = e.getOwnerDocument();
  if (maximums != null) {
    Element maxE = Config.createNamedElement(d,maximums, "Max");
    d.appendChild(maxE);
  }
  if (minimums != null) {
    Element minE = Config.createNamedElement(d,minimums, "Min");
    d.appendChild(minE);
  }

}

@Override
public void limit(Object[] genome, int pos) {

  double min = Double.MIN_VALUE;
  double max = Double.MAX_VALUE;

  if (minimums != null)
    min = minimums.get(pos).doubleValue();
  if (maximums != null)
    max = maximums.get(pos).doubleValue();

  Number genomeValue = (Number) genome[pos];

  double currentValue = (double) genome[pos];
  Double newValue = null;

  if (currentValue < min)
    newValue = min;
  if (currentValue > max)
    newValue = max;

  // It changed
  if (newValue != null) {
    if (genomeValue instanceof Byte) {
      genome[pos] = newValue.byteValue();
    } else if (genomeValue instanceof Short) {
      genome[pos] = newValue.shortValue();
    } else if (genomeValue instanceof Integer) {
      genome[pos] = newValue.intValue();
    } else if (genomeValue instanceof Long) {
      genome[pos] = newValue.longValue();
    } else if (genomeValue instanceof Float) {
      genome[pos] = newValue.floatValue();
    } else if (genomeValue instanceof Double) {
      genome[pos] = newValue;
    } else {
      throw new RuntimeException("RangeLimiter attempting to limit an unknown Numeric type?");
    }

  }


}

@Override
public Number get(int position) {
  return null;
}

}
