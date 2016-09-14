package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PopulationGroup implements Serializable, XMLConfigurable {

  private static final long serialVersionUID = -4325119773362195331L;

  String                    id;
  List<Population>          populations;
  Integer                   totalPopulationSize;

  public PopulationGroup() {
    populations = new ArrayList<>();
  }

  @Override
  public void readXMLConfig(Element e) {

    id = e.getAttribute("id");
    try {
      totalPopulationSize = Integer.parseInt(e.getAttribute("totalSize"));
    } catch (NumberFormatException nfe) {
      throw new UnsupportedOperationException("PopulationGroup must have a totalSize=<Integer>");
    }
    

    NodeList nl = e.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element child = (Element) node;
        XMLConfigurable xc = Config.initializeXMLConfigurable(child);

        if (xc instanceof Population) {
          populations.add((Population) xc);
        } else {
          throw new UnsupportedOperationException("Unrecognized element in Population");
        }

      }
    }

  }

  public void aggregateFitnesses() {
    for (Population pop : populations) {
      pop.aggregateFitnesses(); // Parallelization inside here
    }
  }
  
  public void reproduce() {
    for (Population pop : populations) {
      pop.reproduce();
    }
  }
  
  public List<Population> getPopulations() {
    return populations;
  }
  
  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("id", id);
    e.setAttribute("totalSize", totalPopulationSize.toString());
    for (Population pop : populations) {
      Element subPop = Config.createUnnamedElement(d, pop);
      e.appendChild(subPop);
    }
    
  }

}
