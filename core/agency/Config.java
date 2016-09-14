package agency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import agency.eval.LocalEvaluator;
import agency.eval.LocalEvaluator;
import agency.eval.MeanSimpleFitnessAggregator;
import agency.eval.ShuffledEvaluationGroupFactory;
import agency.reproduce.ElitismSelector;
import agency.reproduce.FitnessProportionalSelector;
import agency.reproduce.RandomIndividualSelector;
import agency.reproduce.TournamentSelector;
import agency.reproduce.WeightedBreedingPipeline;
import agency.vector.DoubleLimiter;
import agency.vector.FlatDoubleInitializer;
import agency.vector.FlatFloatInitializer;
import agency.vector.FlatIntegerInitializer;
import agency.vector.FloatLimiter;
import agency.vector.GaussianDoubleInitializer;
import agency.vector.GaussianDoubleMutator;
import agency.vector.GaussianFloatInitializer;
import agency.vector.GaussianFloatMutator;
import agency.vector.GaussianIntegerInitializer;
import agency.vector.GaussianIntegerMutator;
import agency.vector.IntegerLimiter;
import agency.vector.VectorCrossoverPipeline;
import agency.vector.VectorIndividualFactory;
import agency.vector.VectorMutationPipeline;
import agency.vector.VectorRange;

public class Config {
  static DocumentBuilderFactory                               docFactory;
  static DocumentBuilder                                      docBuilder;

  public static Map<String, Class<? extends XMLConfigurable>> classXMLTagNames;
  public static Map<Class<? extends XMLConfigurable>, String> xmlTagNameClass;

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
    registerClassXMLTag(VectorRange.class);

    registerClassXMLTag(VectorIndividualFactory.class);
    registerClassXMLTag(DefaultAgentModelFactory.class);
    registerClassXMLTag(NullAgentFactory.class);
    registerClassXMLTag(ShuffledEvaluationGroupFactory.class);
    registerClassXMLTag(DoubleLimiter.class);
    registerClassXMLTag(FlatDoubleInitializer.class);
    registerClassXMLTag(FlatFloatInitializer.class);
    registerClassXMLTag(FlatIntegerInitializer.class);
    registerClassXMLTag(VectorRange.class);
    registerClassXMLTag(VectorRange.class);
    registerClassXMLTag(FloatLimiter.class);
    registerClassXMLTag(GaussianDoubleInitializer.class);
    registerClassXMLTag(GaussianDoubleMutator.class);
    registerClassXMLTag(GaussianFloatInitializer.class);
    registerClassXMLTag(GaussianFloatMutator.class);
    registerClassXMLTag(GaussianIntegerInitializer.class);
    registerClassXMLTag(GaussianIntegerMutator.class);
    registerClassXMLTag(IntegerLimiter.class);
    registerClassXMLTag(VectorCrossoverPipeline.class);
    registerClassXMLTag(VectorMutationPipeline.class);

    registerClassXMLTag(MeanSimpleFitnessAggregator.class);
    registerClassXMLTag(NullAgentFactory.class);
    registerClassXMLTag(ShuffledEvaluationGroupFactory.class);
    registerClassXMLTag(LocalEvaluator.class);
    registerClassXMLTag(AgentModelReporter.class);

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

  private static Class<? extends XMLConfigurable> getClassFromTagName(String tn) {
    return classXMLTagNames.get(tn);
  }

  private static String getStringForClassName(Class<? extends XMLConfigurable> c) {
    return xmlTagNameClass.get(c);
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

  public static Element createUnnamedElement(Document doc, XMLConfigurable xc) {
    String tagName = getStringForClassName(xc.getClass());
    if (tagName == null)
      throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
    Element e = doc.createElement(tagName);
    xc.writeXMLConfig(doc, e);
    return e;
  }

  public static Element createNamedElement(Document doc, XMLConfigurable xc, String name) {
    String typeName = getStringForClassName(xc.getClass());
    if (typeName == null)
      throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
    Element e = doc.createElement(name);
    if (!typeName.equals(name))
      e.setAttribute("type", typeName);
    xc.writeXMLConfig(doc, e);
    return e;
  }

  /**
   * @param e
   *          The parent element
   * @param tag
   *          The tag name, i.e., &lt;foo&gt;
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

  /**
   * @param e
   *          The parent element
   * @param tag
   *          The tag name, i.e., &lt;foo&gt;
   * @return Possibly a single child element with the specified tag. Throws an
   *         exception if there are multiple tags.
   * 
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

  public static Document newDocument() {
    return docBuilder.newDocument();
  }

  public static DocumentBuilder getDocBuilder() {
    return docBuilder;
  }

  public static XMLConfigurable getXMLConfigurableFromFile(String fileName) {
    XMLConfigurable toReturn = null;

    try {
      File inputFile = new File(fileName);
      Document doc = getDocBuilder().parse(inputFile);
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

}
