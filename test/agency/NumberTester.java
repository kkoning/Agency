package agency;

import java.util.Set;
import java.util.TreeSet;

public class NumberTester {
	
	Set<Number> valuesSeen = new TreeSet<>();

	public void clear() {
		valuesSeen.clear();
	}

	public boolean add(Number e) {
		return valuesSeen.add(e);
	}

	public boolean seen(Number i) {
		return valuesSeen.contains(i);
	}

	public boolean allWithinRange(Number lowerBound, Number upperBound) {
		for (Number val : valuesSeen) {
			if (val.doubleValue() < lowerBound.doubleValue())
				return false;
			if (val.doubleValue() > upperBound.doubleValue())
				return false;
		}
		return true;
	}

	public boolean someOutsideRange(Number lowerBound, Number upperBound) {
		boolean seenLow = false;
		boolean seenHigh = false;
		
		for (Number val : valuesSeen) {
			if (val.doubleValue() < lowerBound.doubleValue())
				seenLow = true;
			if (val.doubleValue() > upperBound.doubleValue())
				seenHigh = true;
		}
		
		if (seenLow && seenHigh)
			return true;
		else
			return false;
	}

}
