package agency;

import org.junit.Before;
import org.junit.Test;

import agency.eval.EvaluationGroup;
import agency.models.simple.MaximumValue;
import agency.vector.IntegerVectorIndividualFactory;

public class EvaluationGroupTest {

	Population pop;
	
	EvaluationGroup eg;

	IntegerVectorIndividualFactory ivif = new IntegerVectorIndividualFactory(10);
	
	@Before
	public void setUp() throws Exception {
		ivif.setInitializeMinimum(0);
		ivif.setInitializeMaximum(10);
		ivif.setMaximumValue(15);
		
		pop = new Population(ivif, new NullAgentFactory(), 10);
		
		eg = new EvaluationGroup();
		eg.setModel(new MaximumValue());

		pop.shuffledAgents().forEach(a -> eg.addAgent(a));
		
	}
	
	EvaluationGroup generateEvalGroup() {
		return null;
	}

	@Test
	public void test() {
		System.out.println(eg);
		eg.run();

		System.out.println(eg);

		
	}

}
