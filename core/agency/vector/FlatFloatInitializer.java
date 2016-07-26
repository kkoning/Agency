package agency.vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agency.Config;


/**
 * TODO: Better error handing, more documentation
 * 
 * @author kkoning
 *
 */
public class FlatFloatInitializer implements ValueInitializer<Float> {
	
	float floor;
	float ceiling;

	public FlatFloatInitializer() {
		super();
	}

	public FlatFloatInitializer(float floor, float ceiling) {
		super();
		this.floor = floor;
		this.ceiling = ceiling;
	}

	@Override
	public void readXMLConfig(Element e) {
		floor = Float.parseFloat(e.getAttribute("floor"));
		ceiling = Float.parseFloat(e.getAttribute("ceiling"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("floor", Float.toString(floor));
		e.setAttribute("ceiling", Float.toString(ceiling));
	}

	@Override
	public Float create() {
		Random r = ThreadLocalRandom.current();
		float range = ceiling - floor;
		float value = floor + r.nextFloat() * range;
		return value;
	}

	public float getFloor() {
		return floor;
	}

	public void setFloor(float floor) {
		this.floor = floor;
	}

	public float getCeiling() {
		return ceiling;
	}

	public void setCeiling(float ceiling) {
		this.ceiling = ceiling;
	}

}
