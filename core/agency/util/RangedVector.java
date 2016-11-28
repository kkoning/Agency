package agency.util;

import agency.Config;
import agency.XMLConfigurable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The purpose of this class is the specification of object vectors within
 * Agency configuration files.
 *
 */
public class RangedVector implements XMLConfigurable {

/**
 * The original VectorRanges are saved for re-writing configs.
 */
protected List<VectorRange<?>> vectorRangeList = new ArrayList<>();

/**
 * A RangeMap is used to make sure there are no overlapping ranges defined.
 */
protected RangeMap<Integer, VectorRange<?>> vectorRangeMap = new RangeMap<>();

int vectorLength;

@Override
public void readXMLConfig(Element e) {
  String className = this.getClass().getName(); // for debugging

  String lengthString = e.getAttribute("length");
  if (lengthString == null || lengthString.isEmpty())
    throw new RuntimeException("A " + className + " must have a defined length.");
  try {
    vectorLength = Integer.parseInt(lengthString);
  } catch (NumberFormatException nfe) {
    throw new RuntimeException("A " + className + " had non-integer length?  Is there a typo?");
  }
  if (vectorLength <= 0)
    throw new RuntimeException("A " + className + " length must be positive.");

  /*
   * Read VectorRanges, the only child elements allowed.  Save in both vectorRangeList and
   * vectorRangeRangeMap.  Can only contain VectorRanges.
   */
  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof VectorRange) {
        VectorRange<?> vr = (VectorRange<?>) xc;
        vectorRangeList.add(vr);
        vectorRangeMap.put(vr.getRange(), vr);
      } else {
        throw new RuntimeException("A " + className + " must contain only VectorRanges");
      }
    }
  }

  // TODO: Ensure that the ranges cover the exact size of the RangedVector overall.


}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();

  e.setAttribute("length", Integer.toString(vectorLength));
  for (VectorRange<?> vr : vectorRangeList) {
    Element newE = Config.createUnnamedElement(d,vr);
    d.appendChild(newE);
  }

}

@Override
public void resumeFromCheckpoint() {
  // Should be unnecessary
}

public Object get(int position) {
  Optional<VectorRange<?>> ovr = vectorRangeMap.get(position);
  if (!ovr.isPresent())
    throw new RuntimeException("A RangedVector attempted to access a position not defined by a child range.");

  VectorRange<?> vr = ovr.get();
  return vr.get(position);
}

public int getVectorLength() {
  return vectorLength;
}

public Object[] createVector() {
  Object[] toReturn = new Object[vectorLength];
  for (int i = 0; i < vectorLength; i++) {
    toReturn[i] = get(i);
  }
  return toReturn;
}


}
