package agency;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import agency.data.*;
import agency.eval.LocalParallelEvaluator;
import agency.reproduce.*;
import agency.util.*;
import agency.vector.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import agency.eval.LocalEvaluator;
import agency.eval.ShuffledEvaluationGroupFactory;

public class Config {
public static Map<String, Class<? extends XMLConfigurable>> classXMLTagNames;
public static Map<Class<? extends XMLConfigurable>, String> xmlTagNameClass;
static        DocumentBuilderFactory                        docFactory;
static        DocumentBuilder                               docBuilder;

/*
 * To make model configuration files less verbose, Agency allows a number of
 * classes to be specified in XML tag names by short names. The classes
 * specified in this way must implement the interface XMLConfigurable. That
 * interface, by default, uses this.getClass().getSimpleName(); So
 * agency.eval.EvaluationGroup can just use the tag name "EvaluationGroup"
 * rather than the full class name.
 *
 */
static {
  classXMLTagNames = new HashMap<>();
  xmlTagNameClass = new HashMap<>();

  try {
    docFactory = DocumentBuilderFactory.newInstance();
    docBuilder = docFactory.newDocumentBuilder();
  } catch (ParserConfigurationException e) {
    throw new RuntimeException("Could not initialize configuration system. Check classpath");
  }

  // List of short tags/classes
  registerClassXMLTag(Environment.class);
  registerClassXMLTag(Population.class);
  registerClassXMLTag(PopulationGroup.class);
  registerClassXMLTag(ElitismSelector.class);
  registerClassXMLTag(FitnessProportionalSelector.class);
  registerClassXMLTag(RandomIndividualSelector.class);
  registerClassXMLTag(TournamentSelector.class);
  registerClassXMLTag(WeightedBreedingPipeline.class);
  registerClassXMLTag(RangedVector.class);
  registerClassXMLTag(DoubleList.class);
  registerClassXMLTag(IntegerList.class);
  registerClassXMLTag(RepeatingDouble.class);
  registerClassXMLTag(RepeatingInteger.class);
  registerClassXMLTag(GaussianRandomVectorRange.class);

  registerClassXMLTag(VectorMutator.class);
  registerClassXMLTag(GaussianMutator.class);


  registerClassXMLTag(VectorLimitationPipeline.class);
  registerClassXMLTag(VectorLimiter.class);
  registerClassXMLTag(RangeLimiter.class);


  registerClassXMLTag(VectorIndividualFactory.class);
  registerClassXMLTag(DefaultAgentModelFactory.class);
  registerClassXMLTag(NullAgentFactory.class);
  registerClassXMLTag(ShuffledEvaluationGroupFactory.class);
  registerClassXMLTag(VectorCrossoverPipeline.class);
  registerClassXMLTag(VectorMutationPipeline.class);

  registerClassXMLTag(NullAgentFactory.class);
  registerClassXMLTag(DefaultAgentFactory.class);

  registerClassXMLTag(ShuffledEvaluationGroupFactory.class);
  registerClassXMLTag(LocalEvaluator.class);
  registerClassXMLTag(DefaultModelPerStepData.class);
  registerClassXMLTag(DefaultModelSummaryData.class);
  registerClassXMLTag(DefaultVectorIndividualData.class);

  registerClassXMLTag(DataOutput.class);
  registerClassXMLTag(DefaultEnvironmentStatistics.class);
  registerClassXMLTag(LocalParallelEvaluator.class);

  registerClassXMLTag(TournamentBalancer.class);

}

public static void registerClassXMLTag(Class<? extends XMLConfigurable> c) {
  try {
    XMLConfigurable xc = c.newInstance();
    String s = xc.getTagName();
    // TODO Check for collisions and throw error
    classXMLTagNames.put(s, c);
    xmlTagNameClass.put(c, s);
  } catch (Exception e) {
    throw new RuntimeException("Could not register class " + c.getName(), e);
  }
}

public static Element createUnnamedElement(Document doc, XMLConfigurable xc) {
  String tagName = getStringForClassName(xc.getClass());
  if (tagName == null)
    throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
  Element e = doc.createElement(tagName);
  xc.writeXMLConfig(e);
  return e;
}

private static String getStringForClassName(Class<? extends XMLConfigurable> c) {
  return xmlTagNameClass.get(c);
}

public static Element createNamedElement(Document doc, XMLConfigurable xc, String name) {
  String typeName = getStringForClassName(xc.getClass());
  if (typeName == null)
    throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
  Element e = doc.createElement(name);
  if (!typeName.equals(name))
    e.setAttribute("type", typeName);
  xc.writeXMLConfig(e);
  return e;
}

/**
 * @param e
 *         The parent element
 * @param tag
 *         The tag name, i.e., &lt;foo&gt;
 * @return Possibly a single child element with the specified tag. Throws an exception if there are multiple tags.
 */
public static Optional<Element> getChildElementWithTag(Element e, String tag) {
  List<Element> elements = getChildElementsWithTag(e, tag);
  if (elements.size() > 1)
    throw new RuntimeException("Cannot have multiple items of tag: " + tag);
  if (elements.size() == 0)
    return Optional.empty();
  else
    return Optional.of(elements.get(0));
}

/**
 * @param e
 *         The parent element
 * @param tag
 *         The tag name, i.e., &lt;foo&gt;
 * @return A (possibly empty) list of all child elements of the specified tag.
 */
public static List<Element> getChildElementsWithTag(Element e, String tag) {
  List<Element> toReturn = new ArrayList<>();
  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element childElement = (Element) node;
      if (childElement.getTagName().equals(tag))
        toReturn.add(childElement);
    }
  }
  return toReturn;
}

public static Document newDocument() {
  return docBuilder.newDocument();
}

public static XMLConfigurable getXMLConfigurableFromFile(String fileName) {
  File inputFile = new File(fileName);
  return getXMLConfigurableFromFile(inputFile);

}

public static XMLConfigurable getXMLConfigurableFromFile(File file) {
  XMLConfigurable toReturn = null;

  try {
    Document doc = getDocBuilder().parse(file);
    Element e = doc.getDocumentElement();
    toReturn = initializeXMLConfigurable(e);
  } catch (SAXException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }

  return toReturn;
}

public static XMLConfigurable getXMLConfigurableFromString(String xml) {
  XMLConfigurable toReturn = null;

  try {
    Document doc = getDocBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    Element e = doc.getDocumentElement();
    toReturn = initializeXMLConfigurable(e);
  } catch (SAXException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }

  return toReturn;
}


public static XMLConfigurable initializeXMLConfigurable(Element e) {
  try {
    String typeName = e.getAttribute("type");
    if (typeName == null || typeName.isEmpty()) {
      typeName = e.getTagName();
    }
    // TODO better error handling
    Class<? extends XMLConfigurable> c = getClassFromTagName(typeName);
    if (c == null)
      c = (Class<? extends XMLConfigurable>) Class.forName(typeName);

    XMLConfigurable xc = c.newInstance();
    xc.readXMLConfig(e);
    return xc;
  } catch (Exception ex) {
    throw new RuntimeException(ex);
  }
}

private static Class<? extends XMLConfigurable> getClassFromTagName(String tn) {
  return classXMLTagNames.get(tn);
}

public static DocumentBuilder getDocBuilder() {
  return docBuilder;
}

}
