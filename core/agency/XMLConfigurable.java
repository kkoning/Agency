package agency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLConfigurable {
/**
 * Initialize this object based on an XML element
 *
 * @param e
 */
public void readXMLConfig(Element e);

/**
 * Add information to the given element sufficient to recreate this object
 *
 * @param e
 */
public void writeXMLConfig(Element e);

public void resumeFromCheckpoint();

public default String getTagName() {
  return this.getClass().getSimpleName();
}
}
