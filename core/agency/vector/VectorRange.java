package agency.vector;

import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agency.Config;
import agency.XMLConfigurable;
import agency.util.HasRange;

public class VectorRange implements XMLConfigurable, HasRange<Integer> {

	Integer startGenomePosition;
	Integer endGenomePosition;
	double mutationProbability = 0d;

	ValueInitializer<?> vi;
	ValueMutator<?> vm;
	ValueLimiter<?> vl;

	@Override
	public void readXMLConfig(Element e) {
		// TODO Auto-generated method stub
		String startPositionString = e.getAttribute("start");
		if (startPositionString == null || startPositionString.isEmpty())
			throw new RuntimeException("VectorRange must have a start position.");
		try {
			startGenomePosition = Integer.parseInt(startPositionString);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("VectorRange had non-integer start position?");
		}
		if (startGenomePosition < 0)
			throw new RuntimeException("VectorRange start cannot be negative.");

		String endPositionString = e.getAttribute("end");
		if (endPositionString == null || endPositionString.isEmpty())
			throw new RuntimeException("VectorRange must have a end position.");
		try {
			endGenomePosition = Integer.parseInt(endPositionString);
		} catch (NumberFormatException nfe) {
			if (endPositionString.equalsIgnoreCase("last"))
				endGenomePosition = Integer.MAX_VALUE;
			else
				throw new RuntimeException("VectorRange had non-integer end position other than 'last'?");
		}

		String mutationProbabilityString = e.getAttribute("mutationProbability");
		if (mutationProbabilityString != null && !mutationProbabilityString.isEmpty())
			try {
				mutationProbability = Double.parseDouble(mutationProbabilityString);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(
						"mutationProbability '" + mutationProbabilityString + "' cannot be parsed as a Double");
			}

		// TODO: Better error handling and reporting

		Optional<Element> viElement = Config.getChildElementWithTag(e, "ValueInitializer");
		Optional<Element> vmElement = Config.getChildElementWithTag(e, "ValueMutator");
		Optional<Element> vlElement = Config.getChildElementWithTag(e, "ValueLimiter");

		if (!viElement.isPresent())
			throw new RuntimeException("A VectorRange _must_ have a ValueInitializer.");
		vi = (ValueInitializer<?>) Config.initializeXMLConfigurable(viElement.get());
		if (vmElement.isPresent())
			vm = (ValueMutator<?>) Config.initializeXMLConfigurable(vmElement.get());
		if (vlElement.isPresent())
			vl = (ValueLimiter<?>) Config.initializeXMLConfigurable(vlElement.get());

	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		// Describe the range
		e.setAttribute("start", startGenomePosition.toString());
		if (endGenomePosition == Integer.MAX_VALUE)
			e.setAttribute("end", "last");
		else
			e.setAttribute("end", endGenomePosition.toString());

		// Describe how the range is to be configured
		Element viElement = Config.createNamedElement(d, vi, "ValueInitializer");
		e.appendChild(viElement);
		if (vm != null) {
			Element vmElement = Config.createNamedElement(d, vm, "ValueMutator");
			e.appendChild(vmElement);
		}
		if (vl != null) {
			Element vlElement = Config.createNamedElement(d, vl, "ValueLimiter");
			e.appendChild(vlElement);
		}

	}

	public Integer getStartGenomePosition() {
		return startGenomePosition;
	}

	public void setStartGenomePosition(Integer startGenomePosition) {
		this.startGenomePosition = startGenomePosition;
	}

	public Integer getEndGenomePosition() {
		return endGenomePosition;
	}

	public void setEndGenomePosition(Integer endGenomePosition) {
		this.endGenomePosition = endGenomePosition;
	}

	public ValueInitializer<?> getVi() {
		return vi;
	}

	public void setVi(ValueInitializer<?> vi) {
		this.vi = vi;
	}

	public ValueMutator<?> getVm() {
		return vm;
	}

	public void setVm(ValueMutator<?> vm) {
		this.vm = vm;
	}

	public ValueLimiter<?> getVl() {
		return vl;
	}

	public void setVl(ValueLimiter<?> vl) {
		this.vl = vl;
	}

	@Override
	public Range<Integer> getRange() {
		Range<Integer> range = Range.between(startGenomePosition, endGenomePosition);
		return range;
	}

}
