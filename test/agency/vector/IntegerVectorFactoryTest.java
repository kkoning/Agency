package agency.vector;

import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import agency.NumberTester;

public class IntegerVectorFactoryTest {
	
	IntegerVectorIndividualFactory icisf;

	NumberTester nt = new NumberTester();
	
	@Before
	public void setUp() throws Exception {
		icisf = new IntegerVectorIndividualFactory(10);
		icisf.setInitializeMinimum(0);
		icisf.setInitializeMaximum(10);
		icisf.mutationProbability = 0.5;
		icisf.mutationRange = 3;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void createTest() {
		nt.clear();
		
		IntStream.range(0, 100)
			.parallel()
			.forEach(i -> {
				VectorIndividual<Integer> ind = icisf.create();
				assert(ind.genome.size() == 10);
				for (Integer val : ind.genome) {
					assert( val != null );
					nt.add(val);
				}
			});
		
		assertTrue(nt.seen(0));
		assertTrue(nt.seen(10));
		assertTrue(nt.allWithinRange(0,10));
	}
	
	@Test
	public void sexualReproductionTest() {
		IntStream.range(0, 5)
		.mapToObj(i -> new pair(icisf.create(),icisf.create()))
		.forEach(o -> {
			System.out.println("Parents");
			System.out.println("1: " + o.one);
			System.out.println("2: " + o.two);
			System.out.println("Children");
			Stream<VectorIndividual<Integer>> childStream = icisf.childStream(o.one, o.two);
			childStream
				.limit(4)
				.forEach(c -> System.out.println("Child: " + c));
		});
	}

	@Test
	public void mutateTest() {
		nt.clear();
		
		IntStream.range(0, 100)
			.mapToObj(i -> icisf.create())
			.forEach(o -> {
				IntStream.range(0, 10).forEach(u -> icisf.mutate(o));
				for (Integer val : o.genome) {
					nt.add(val);
				}
			});
		
		assertTrue(nt.someOutsideRange(0,10));
		assertTrue(nt.allWithinRange(-50,50));

		nt.clear();
		
		icisf.setMinimumValue(-5);
		icisf.setMaximumValue(15);
		
		IntStream.range(0, 100)
		.mapToObj(i -> icisf.create())
		.forEach(o -> {
			IntStream.range(0, 10).forEach(u -> icisf.mutate(o));
			for (Integer val : o.genome) {
				nt.add(val);
			}
		});
		
		assertTrue(nt.seen(-5));
		assertTrue(nt.seen(15));
		assertTrue(nt.allWithinRange(-5,15));
		
	}
	
	class pair {
		pair(VectorIndividual<Integer> one, VectorIndividual<Integer> two) {
			this.one = one;
			this.two = two;
		}
		VectorIndividual<Integer> one;
		VectorIndividual<Integer> two;
	}
	

}
