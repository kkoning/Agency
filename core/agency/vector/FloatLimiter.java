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
public class FloatLimiter implements ValueLimiter<Float> {

	Float floor;
	Float ceiling;

	public FloatLimiter() {
		super();
	}

	public FloatLimiter(Float floor, Float ceiling) {
		super();
		this.floor = floor;
		this.ceiling = ceiling;
	}

	@Override
	public void readXMLConfig(Element e) {
		try {
			floor = Float.parseFloat(e.getAttribute("floor"));
		} catch (Exception ex) {
			// TODO
		}
		try {
			ceiling = Float.parseFloat(e.getAttribute("ceiling"));
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
	public Float limit(Float value) {
		if (ceiling != null)
			if (value > ceiling)
				return ceiling;
		if (floor != null)
			if (value < floor)
				return floor;
		return value;
	}

	public Float getFloor() {
		return floor;
	}

	public void setFloor(Float floor) {
		this.floor = floor;
	}

	public Float getCeiling() {
		return ceiling;
	}

	public void setCeiling(Float ceiling) {
		this.ceiling = ceiling;
	}

}
