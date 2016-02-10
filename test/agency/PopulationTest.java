package agency;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import agency.util.ObjectCounter;
import agency.vector.IntegerVectorIndividualFactory;

public class PopulationTest {

	IntegerVectorIndividualFactory indFactory;
	AgentFactory agentFactory;

	NumberTester nt = new NumberTester();

	@Before
	public void setUp() throws Exception {
		indFactory = new IntegerVectorIndividualFactory(10);
		indFactory.setInitializeMinimum(0);
		indFactory.setInitializeMaximum(10);
		agentFactory = new NullAgentFactory();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPopulation() {

		Population testPop = new Population(indFactory, agentFactory, 10);
		
		ObjectCounter oc = new ObjectCounter();

		testPop.randomIndividuals().limit(100_000).forEach(o -> oc.observe(o));

		 System.out.println(oc);

		if (oc.size() != 10)
			fail("Some individuals missing");

		for (Integer count : oc.values()) {
			if (count < 9_000)
				fail("Distribution of objects seems uneven, re-run test");
			if (count > 11_000)
				fail("tDistribution of objects seems uneven, re-run test");
		}

	}

}
