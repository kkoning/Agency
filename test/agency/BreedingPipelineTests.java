package agency;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import agency.reproduce.BreedingPipeline;
import agency.reproduce.FitnessProportionalSelector;
import agency.reproduce.RandomIndividualSelector;
import agency.reproduce.WeightedBreedingPipeline;
import agency.util.ObjectCounter;
import agency.vector.IntegerVectorIndividualFactory;
import agency.vector.VectorIndividual;

public class BreedingPipelineTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWeightedBreedingPipeline() {
		
		IntegerVectorIndividualFactory ivif1 = new IntegerVectorIndividualFactory(1);
		ivif1.setInitializeMinimum(0);
		ivif1.setInitializeMaximum(10);
		Population pop1 = new Population(ivif1, new NullAgentFactory(), 10);

		IntegerVectorIndividualFactory ivif2 = new IntegerVectorIndividualFactory(10);
		ivif2.setInitializeMinimum(-10);
		ivif2.setInitializeMaximum(0);
		Population pop2 = new Population(ivif2, new NullAgentFactory(), 10);

		BreedingPipeline s1 = new RandomIndividualSelector();
		s1.setSourcePopulation(pop1);

		BreedingPipeline s2 = new RandomIndividualSelector();
		s2.setSourcePopulation(pop2);

		WeightedBreedingPipeline wbp = new WeightedBreedingPipeline();
		wbp.addPipeline(s1, 5f);
		wbp.addPipeline(s2, 1f);

		int numSamples = 100_000;
		
		
		long totalLength = IntStream.range(0,numSamples).mapToLong((i) -> {
			VectorIndividual<?> vi = (VectorIndividual<?>) wbp.generate();
			return vi.getGenomeSize();
		}).sum();
		
		double averageLength = (double) totalLength / numSamples;

		double foo = averageLength - 2.5;
		double bar = Math.abs(foo);
		
		System.out.println("Average length should be ~2.5, is " + averageLength);
		
		if (bar > 0.05)
			fail("Discrepency between actual and expected popualtions is too large");
		
	}

	@Test
	public void testFitnessProportionalSelection() {
		
		int numInds = 10;
		int numSamples = 100_000;
		
		IntegerVectorIndividualFactory ivif1 = new IntegerVectorIndividualFactory(1);
		ivif1.setInitializeMinimum(0);
		ivif1.setInitializeMaximum(10);
		Population pop1 = new Population(ivif1, new NullAgentFactory(), numInds);
		
		List<Individual> inds = pop1.allIndividuals().collect(Collectors.toList());

		IntStream.range(0, numInds).forEach((i) -> {
			VectorIndividual<Integer> ind = (VectorIndividual<Integer>) inds.get(i);
			ind.genome.set(0, i-2);
			ind.setFitness(new SimpleFitness(i-2));
		});
		
		ObjectCounter oc = new ObjectCounter();
		FitnessProportionalSelector fps = new FitnessProportionalSelector();
		fps.setSourcePopulation(pop1);
		IntStream.range(0, numSamples).forEach((i) -> {
			Individual ind = fps.generate();
			oc.observe(ind);
		});
		
		System.out.println(oc);
		
		
	}
	
	

}
