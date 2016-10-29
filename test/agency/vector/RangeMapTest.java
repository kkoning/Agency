package agency.vector;

import org.apache.commons.lang3.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import agency.util.HasRange;
import agency.util.RangeMap;

public class RangeMapTest {

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

@Test
public void test() {
  RangeMap<Double, Foo> rm = new RangeMap<>();
  rm.put(new Foo(1, 2.0009, "foo"));
  rm.put(new Foo(5, 6, "bar"));
  rm.put(new Foo(2.1, 4.999999, "baz"));
  System.out.println(rm);


}

public class Foo implements HasRange<Double> {
  Range<Double> r;
  String        bob;

  public Foo(double low, double high, String bob) {
    r = Range.between(low, high);
    this.bob = bob;
  }

  @Override
  public Range<Double> getRange() {
    return r;
  }

  @Override
  public String toString() {
    return "Foo [r=" + r + ", bob=" + bob + "]";
  }

}


}
