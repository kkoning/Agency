package agency;

import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.OptionalDouble;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.eval.ShuffledEvaluationGroupFactory;
import agency.models.simple.MaximumValue;
import agency.reproduce.ElitismSelector;
import agency.reproduce.FitnessProportionalSelector;
import agency.reproduce.RandomIndividualSelector;
import agency.reproduce.TournamentSelector;
import agency.reproduce.VectorMutationPipeline;
import agency.reproduce.WeightedBreedingPipeline;
import agency.vector.FlatIntegerInitializer;
import agency.vector.GaussianIntegerMutator;
import agency.vector.IntegerLimiter;
import agency.vector.ValueInitializer;
import agency.vector.ValueLimiter;
import agency.vector.VectorIndividual;
import agency.vector.VectorIndividualFactory;
import agency.vector.VectorRange;

public class ConfigurationTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testBasics() throws Exception {
    System.out.println("------------- Start testBasics");

    // IntegerVectorIndividualFactory ivif = new
    // IntegerVectorIndividualFactory(10);
    // ivif.setInitializeMinimum(0);
    // ivif.setInitializeMaximum(10);
    // ivif.setMaximumValue(15);

    // Population pop = new Population(ivif, new NullAgentFactory(), 10);

    System.out.println("Testing breeding pipeline");
    WeightedBreedingPipeline wbp = new WeightedBreedingPipeline();
    wbp.addPipeline(new TournamentSelector(), 4.5f);
    wbp.addPipeline(new FitnessProportionalSelector(), 2f);
    wbp.addPipeline(new RandomIndividualSelector(), 1f);
    ElitismSelector es = new ElitismSelector();
    es.setProportionElites(0.3f);
    wbp.addPipeline(es, 0.5f);
    
    VectorMutationPipeline<Integer> vmp = new VectorMutationPipeline<>();
    GaussianIntegerMutator gim = new GaussianIntegerMutator(5);
    vmp.addMutator(0, 19, 0.05, gim);
    vmp.setSource(wbp);

    Document doc = Config.newDocument();
    Element e = Config.createUnnamedElement(doc, vmp);
    String xmlString = toXml(e);

    System.out.println(xmlString);
    Element e2 = getRootElementFrom(xmlString);

    XMLConfigurable xc = Config.initializeXMLConfigurable(e2);
    System.out.println("This should be an equivilent config");
    Document d2 = Config.newDocument();
    Element e3 = Config.createUnnamedElement(d2, xc);
    xmlString = toXml(e3);
    System.out.println(xmlString);

    System.out.println("Testing a population");
    VectorIndividualFactory<Integer> vifi = new VectorIndividualFactory<>(20);
    ValueInitializer<Integer> vii = new FlatIntegerInitializer(-100, 100);
    ValueLimiter<Integer> vll = new IntegerLimiter(-50, 50);
    vifi.addInitializer(0, 19, vii);
    vifi.addLimiter(0, 19, vll);

    AgentFactory af = new NullAgentFactory();

    Population p = new Population(vifi, af, vmp, 20);
    System.out.println("Printing complete population configuration XML");

    Document d3 = Config.newDocument();
    Element e4 = Config.createUnnamedElement(d3, p);
    xmlString = toXml(e4);
    System.out.println(xmlString);
    Element e5 = getRootElementFrom(xmlString);

    Population p2 = (Population) Config.initializeXMLConfigurable(e5);
    System.out.println("This should be an equivilent config");
    Document d5 = Config.newDocument();
    Element e6 = Config.createUnnamedElement(d5, p2);
    xmlString = toXml(e6);
    System.out.println(xmlString);

    // Does this population evolve? Correctly?
    Environment env = new Environment();
    env.addPopulation(p2);
    env.setEvaluationGroupFactory(new ShuffledEvaluationGroupFactory(10, 1));
    env.setAgentModelFactory(new DefaultAgentModelFactory(MaximumValue.class));
    
    Document d4 = Config.newDocument();
    Element e7 = Config.createUnnamedElement(d4, env);
    xmlString = toXml(e7);
    System.out.println(xmlString);


    System.out.println("Population before evolution");

    env.evaluationManager.evaluate(env);

    p2.allIndividuals().forEach(System.out::println);


    int gensToEvolve = 100;
    for (int i = 0; i < gensToEvolve; i++) {
      env.evolve();
      OptionalDouble avgFitness = p2.allIndividuals().mapToDouble((ind) -> {
        SimpleFitness sf = (SimpleFitness) ind.getFitness().get();
        return sf.fitness;
      }).average();
      System.out.println("Average Fitness in gen " + i + " is " + avgFitness.getAsDouble());
    }
    
    
    // IntStream.range(0, 10).forEach((i) -> env.evolve());

    System.out.println("Population after " + gensToEvolve + " generations of evolution");
    p2.allIndividuals().forEach(System.out::println);

    
    
    
    System.out.println("------------- End testBasics");

  }

  @Test
  public void testVectorRange() throws Exception {
    VectorRange vr = new VectorRange();
    vr.setStartGenomePosition(0);
    vr.setEndGenomePosition(Integer.MAX_VALUE);
    FlatIntegerInitializer fivi = new FlatIntegerInitializer();
    fivi.setFloor(0);
    fivi.setCeiling(10);
    vr.setVi(fivi);
    vr.setVl(new IntegerLimiter());

    Document doc = Config.newDocument();
    Element e = Config.createUnnamedElement(doc, vr);
    String xmlString = toXml(e);
    System.out.println(xmlString);

  }

  @Test
  public void testCreateVectorIndividual() throws Exception {
    VectorIndividual<Integer> vi = new VectorIndividual<>(5);
    System.out.println(vi);
  }

  @Test
  public void testVectorIndividualFactory() throws Exception {
    System.out.println("------------- Start testVectorIndividualFactory");

    VectorIndividualFactory<Integer> vifi = new VectorIndividualFactory<>(5);
    ValueInitializer<Integer> vii = new FlatIntegerInitializer(-100, 100);
    ValueLimiter<Integer> vll = new IntegerLimiter(-50, 50);

    try {
      vifi.addInitializer(-1, 4, vii);
      fail("Out of range exception should have been thrown");
    } catch (Exception e) {
      // should throw exception
    }

    try {
      vifi.addInitializer(0, 5, vii);
      fail("Out of range exception should have been thrown");
    } catch (Exception e) {
      // should throw exception
    }

    try {
      vifi.addLimiter(-1, 4, vll);
      fail("Out of range exception should have been thrown");
    } catch (Exception e) {
      // should throw exception
    }

    try {
      vifi.addLimiter(0, 5, vll);
      fail("Out of range exception should have been thrown");
    } catch (Exception e) {
      // should throw exception
    }

    vifi.addInitializer(0, 4, vii);
    vifi.addLimiter(0, 4, vll);

    Document doc = Config.newDocument();
    Element e = Config.createUnnamedElement(doc, vifi);
    String xmlString = toXml(e);
    System.out.println("Test output of XMLConconfigurable");
    System.out.println(xmlString);

    VectorIndividualFactory<Integer> vifi2 = (VectorIndividualFactory<Integer>) getRootXMLConfigurable(
        xmlString);

    System.out.println("This configuration should be equivilent to one above");
    printConfig(vifi2);

    System.out.println("Generating 10 individuals from first configuration");
    for (int i = 0; i < 10; i++) {
      VectorIndividual<Integer> vi = vifi.create();
      System.out.println(vi);
    }

    System.out.println("Generating 10 individuals from second configuration");
    for (int i = 0; i < 10; i++) {
      VectorIndividual<Integer> vi = vifi2.create();
      System.out.println(vi);
    }

    System.out.println("------------- End testVectorIndividualFactory");
  }

  static String toXml(Element e) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        "2");

    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(e);
    transformer.transform(source, result);

    String xmlString = result.getWriter().toString();
    return xmlString;
  }

  static Element getRootElementFrom(String xmlString) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
    Document d2 = Config.getDocBuilder().parse(bais);
    Element e2 = d2.getDocumentElement();
    return e2;
  }

  static XMLConfigurable getRootXMLConfigurable(String xml) throws Exception {
    return Config.initializeXMLConfigurable(getRootElementFrom(xml));
  }

  public static void printConfig(XMLConfigurable xc) throws Exception {
    Document doc = Config.newDocument();
    Element e = Config.createUnnamedElement(doc, xc);
    String xmlString = toXml(e);
    System.out.println(xmlString);
  }

}
