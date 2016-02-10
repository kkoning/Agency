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
public class IntegerLimiter implements ValueLimiter<Integer> {
	static {
		Config.registerClassXMLTag(IntegerLimiter.class);
	}

	Integer floor;
	Integer ceiling;

	public IntegerLimiter() {
		super();
	}

	public IntegerLimiter(Integer floor, Integer ceiling) {
		super();
		this.floor = floor;
		this.ceiling = ceiling;
	}

	@Override
	public void readXMLConfig(Element e) {
		try {
			floor = Integer.parseInt(e.getAttribute("floor"));
		} catch (Exception ex) {
			// TODO
		}
		try {
			ceiling = Integer.parseInt(e.getAttribute("ceiling"));
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
	public Integer limit(Integer value) {
		if (ceiling != null)
			if (value > ceiling)
				return ceiling;
		if (floor != null)
			if (value < floor)
				return floor;
		return value;
	}

	public Integer getFloor() {
		return floor;
	}

	public void setFloor(Integer floor) {
		this.floor = floor;
	}

	public Integer getCeiling() {
		return ceiling;
	}

	public void setCeiling(Integer ceiling) {
		this.ceiling = ceiling;
	}

}
