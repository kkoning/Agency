package agency.models;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import agency.Config;
import agency.Environment;
import agency.Population;
import agency.XMLConfigurable;
import agency.eval.EvaluationGroup;

public class MaximumValueTest {

	Environment env;
	Population pop;
	
	EvaluationGroup eg;

	@Before
	public void setUp() throws Exception {

    System.out.println("Attempting to initialize from test/agency/models/MaximumValueTest.xml");
    XMLConfigurable xc = Config.getXMLConfigurableFromFile("test/agency/models/MaximumValueTest.xml");

    env = (Environment) xc;
	}
	

	@Test
	public void testEvaluatePop() {

	  System.out.println("---Start testEvaluatePop");
		ArrayList<Population> pops = env.getPopulations();

    System.out.println("---Initial Populations");
		for (Population pop : pops) {
			System.out.println(pop);
		}
		
		for (int i = 0; i < 5; i++) {
		  env.evolve();
		}

    System.out.println("---Population after 5 generations");
		
		for (Population pop : pops) {
			System.out.println(pop);
		}

    for (int i = 0; i < 95; i++) {
      env.evolve();
    }

    System.out.println("---Population after 100 generations");

    for (Population pop : pops) {
      System.out.println(pop);
    }

		
		
	}

}
