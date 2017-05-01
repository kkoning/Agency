package agency.vector;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VectorIndividualTest {

/**
 * Series of statements in R, the results should match.
 * 
 * > tmp <- -5:14
 * 
 * > tmpE <- exp(tmp)
 * 
 * > tmpE [1] 6.737947e-03 1.831564e-02 4.978707e-02 1.353353e-01 3.678794e-01
 * 1.000000e+00 2.718282e+00 7.389056e+00 2.008554e+01 [10] 5.459815e+01
 * 1.484132e+02 4.034288e+02 1.096633e+03 2.980958e+03 8.103084e+03 2.202647e+04
 * 5.987414e+04 1.627548e+05 [19] 4.424134e+05 1.202604e+06
 * 
 * > options(digits=15)
 * 
 * > sum(tmpE)
 * 
 * [1] 1902491.96120861
 * 
 */
public static final double SUM_FROM_R   = 1902491.96120861d;
public static final int    genomeLength = 20;
public static final int    valueOffset  = 5;

VectorIndividual<Double> testVi;

@Before
public void setUp() throws Exception {
  testVi = new VectorIndividual<>(genomeLength);
  for (int i = 0; i < testVi.getGenomeLength(); i++) {
    testVi.changeGene(i, (double) i - valueOffset);
  }
}

@Test
public void testE() {

  double sum = 0d;
  for (int i = 0; i < testVi.getGenomeLength(); i++) {
    sum += testVi.e(i);
  }
  System.out.println("Sum is: " + sum + ", expected 1902491.96120861");

  double error = sum - SUM_FROM_R;
  assert (Math.abs(error) < 0.00000001);
}

@Test
public void testLinearEq() {

  /*
   * Expected value, calculated with R.
   * 
   * > tmp
   * 
   * [1] -5 -4 -3 -2 -1 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14
   * 
   * > vars
   * 
   * [1] 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2
   * 
   * > sum(vars*tmp)
   * 
   * [1] 180
   * 
   */
  double EXPECTED_RESULT = 180d;

  double[] vars = new double[19];
  for (int i = 0; i < vars.length; i++)
    vars[i] = 2d;

  double result = testVi.linearEq(0, vars);
  result += testVi.gene(0);
  System.out.println("LinearEq is: " + result + ", expected " + EXPECTED_RESULT);

  double error = result - EXPECTED_RESULT;
  assert (Math.abs(error) < 0.00000001);

}

@Test
public void testLinearEqExp() {

  /*
   * Expected Results, calculated with R.
   * 
   * > vars
   * 
   * [1] 1 -1 2 -2 3 -3 4 -4 5 -5 6 -6 7 -7 8 -8 9 -9 10 -10
   * 
   * > tmpE
   * 
   * [1] 6.73794699908547e-03 1.83156388887342e-02 4.97870683678639e-02
   * 1.35335283236613e-01 3.67879441171442e-01 1.00000000000000e+00 [7]
   * 2.71828182845905e+00 7.38905609893065e+00 2.00855369231877e+01
   * 5.45981500331442e+01 1.48413159102577e+02 4.03428793492735e+02 [13]
   * 1.09663315842846e+03 2.98095798704173e+03 8.10308392757538e+03
   * 2.20264657948067e+04 5.98741417151978e+04 1.62754791419004e+05 [19]
   * 4.42413392008920e+05 1.20260428416478e+06
   * 
   * > sum(vars*tmpE)
   * 
   * [1] -8654135.51663573
   * 
   * 
   */
  double EXPECTED_RESULT = -8654135.51663573d;

  double[] vars = { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  double result = testVi.linearEqExp(0, vars);

  System.out.println("LinearEqExp is: " + result + ", expected " + EXPECTED_RESULT);

  double error = result - EXPECTED_RESULT;
  assert (Math.abs(error) < 0.00000001);

}

@Test
public void testConditionIndexer() {

  double[] observations = { 1, 2, 3, 4, 5, 6, 7 };
  double[] thresholds = { 0, 3, 4, 0, 0, 9, 10 };

  int EXPECTED_RESULT = 0b1001100;
  int result = VectorIndividual.conditionIndexer(thresholds, observations);
  System.out.println("Expected " + result + "=" + EXPECTED_RESULT);
  assert(result == EXPECTED_RESULT);

  /*
   * Comparing values to themselves should always return zero, since the binary
   * indexing requires a strictly greater than result to trigger a 1 for that
   * position.
   */
  EXPECTED_RESULT = 0;
  result = VectorIndividual.conditionIndexer(thresholds, thresholds);
  System.out.println("Expected " + result + "=" + EXPECTED_RESULT);
  assert(result == EXPECTED_RESULT);
  

}

}
