package agency.vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agency.Config;

/**
 * TODO: Better error handing, more documentation
 * 
 * @author kkoning
 *
 */
public class DoubleLimiter implements ValueLimiter<Double> {
	static {
		Config.registerClassXMLTag(DoubleLimiter.class);
	}

	Double floor;
	Double ceiling;
	
	public DoubleLimiter() {
		super();
	}

	public DoubleLimiter(Double floor, Double ceiling) {
		super();
		this.floor = floor;
		this.ceiling = ceiling;
	}

	@Override
	public void readXMLConfig(Element e) {
		try {
			floor = Double.parseDouble(e.getAttribute("floor"));
		} catch (Exception ex) {
			// TODO
		}
		try {
			ceiling = Double.parseDouble(e.getAttribute("ceiling"));
		} catch (Exception ex) {
			// TODO
		}
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		if (floor != null)
			e.setAttribute("floor", floor.toString());
		if (ceiling != null)
			e.setAttribute("ceiling", ceiling.toString());
	}

	@Override
	public Double limit(Double value) {
		if (ceiling != null)
			if (value > ceiling)
				return ceiling;
		if (floor != null)
			if (value < floor)
				return floor;
		return value;
	}

	public Double getFloor() {
		return floor;
	}

	public void setFloor(Double floor) {
		this.floor = floor;
	}

	public Double getCeiling() {
		return ceiling;
	}

	public void setCeiling(Double ceiling) {
		this.ceiling = ceiling;
	}

}
