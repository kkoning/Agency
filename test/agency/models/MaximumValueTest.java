package agency.models;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.Before;
import org.junit.Test;

import agency.AgentModelFactory;
import agency.DefaultAgentModelFactory;
import agency.Environment;
import agency.NullAgentFactory;
import agency.Population;
import agency.eval.EvaluationGroup;
import agency.eval.EvaluationGroupFactory;
import agency.eval.EvaluationManager;
import agency.eval.ShuffledEvaluationGroupFactory;
import agency.models.simple.MaximumValue;
import agency.reproduce.TournamentSelector;
import agency.vector.IntegerVectorIndividualFactory;

public class MaximumValueTest {

	Environment env;
	Population pop;
	
	EvaluationGroup eg;

	IntegerVectorIndividualFactory ivif = new IntegerVectorIndividualFactory(10);
	
	@Before
	public void setUp() throws Exception {
		ivif.setInitializeMinimum(0);
		ivif.setInitializeMaximum(10);
		ivif.setMaximumValue(15);
		ivif.setMinimumValue(0);
		ivif.setMutationProbability(0.05);
		ivif.setMutationRange(3);
		
		pop = new Population(ivif, new NullAgentFactory(), 10);

		env = new Environment();
		
		env.addPopulation(pop);
		EvaluationGroupFactory egf = new ShuffledEvaluationGroupFactory(3,1);
		AgentModelFactory<?> amf = new DefaultAgentModelFactory(MaximumValue.class);
		
		env.setEvaluationGroupFactory(egf);
		env.setAgentModelFactory(amf);
		
	}
	

	@Test
	public void testEvaluatePop() {
		EvaluationManager em = new EvaluationManager();

		ArrayList<Population> pops = env.getPopulations();
		em.evaluate(env);
		for (Population pop : pops) {
			System.out.println(pop);
		}
		
		for (int i = 0; i < 500; i++) {
			for (Population pop : pops) {
//				System.out.println("Before Reproduction:");
//				System.out.println(pop);
				pop.reproduce();
//				System.out.println("After Reproduction:");
//				System.out.println(pop);
			}
			em.evaluate(env);
		}
		
		for (Population pop : pops) {
			System.out.println(pop);
			TournamentSelector ts = new TournamentSelector(pop,2,1);
			ts.limit(6).forEach(ind -> System.out.println(ind + "->" + ind.getFitness()));
			ts.close();
		}
		
		
		
	}
	

}
