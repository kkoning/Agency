package agency.util;

import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agency.XMLConfigurable;
import agency.util.HasRange;

public abstract class VectorRange<T> implements XMLConfigurable, HasRange<Integer> {

protected Integer startPosition;
protected Integer endPosition;

@Override
public void readXMLConfig(Element e) {

  String className = this.getClass().getName();

  String startPositionString = e.getAttribute("start");
  if (startPositionString == null || startPositionString.isEmpty())
    throw new RuntimeException("A " + className + " must have a start position.");
  try {
    startPosition = Integer.parseInt(startPositionString);
  } catch (NumberFormatException nfe) {
    throw new RuntimeException("A " + className + " had non-integer start position?");
  }
  if (startPosition < 0)
    throw new RuntimeException("A " + className + " start cannot be negative.");

  String endPositionString = e.getAttribute("end");
  if (endPositionString == null || endPositionString.isEmpty())
    throw new RuntimeException("A " + className + " must have a end position.");
  try {
    endPosition = Integer.parseInt(endPositionString);
  } catch (NumberFormatException nfe) {
    if (endPositionString.equalsIgnoreCase("last"))
      endPosition = Integer.MAX_VALUE;
    else
      throw new RuntimeException("A " + className + " had non-integer end position other than 'last'?");
  }

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  // Describe the range
  e.setAttribute("start", startPosition.toString());
  if (endPosition == Integer.MAX_VALUE)
    e.setAttribute("end", "last");
  else
    e.setAttribute("end", endPosition.toString());

}

public abstract T get(int position);

@Override
public void resumeFromCheckpoint() {
  // Should be unnecessary; everything here can be natively serialized.
}

@Override
public Range<Integer> getRange() {
  Range<Integer> range = Range.between(startPosition, endPosition);
  return range;
}

protected final int effectivePos(int position) {
  return position - startPosition;
}

}
