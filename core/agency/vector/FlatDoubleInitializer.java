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
public class FlatDoubleInitializer implements ValueInitializer<Double> {
	
	double floor;
	double ceiling;
	
	public FlatDoubleInitializer() {
	}

	public FlatDoubleInitializer(double floor, double ceiling) {
		this.floor = floor;
		this.ceiling = ceiling;
	}
	
	@Override
	public void readXMLConfig(Element e) {
		floor = Double.parseDouble(e.getAttribute("floor"));
		ceiling = Double.parseDouble(e.getAttribute("ceiling"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("floor", Double.toString(floor));
		e.setAttribute("ceiling", Double.toString(ceiling));
	}

	@Override
	public Double create() {
		Random r = ThreadLocalRandom.current();
		double range = ceiling - floor;
		double value = floor + r.nextDouble() * range;
		return value;
	}

	public double getFloor() {
		return floor;
	}

	public void setFloor(double floor) {
		this.floor = floor;
	}

	public double getCeiling() {
		return ceiling;
	}

	public void setCeiling(double ceiling) {
		this.ceiling = ceiling;
	}

}
