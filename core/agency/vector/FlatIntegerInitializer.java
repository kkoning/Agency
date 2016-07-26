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
public class FlatIntegerInitializer implements ValueInitializer<Integer> {
	
	Integer floor;
	Integer ceiling;

	public FlatIntegerInitializer() {
		super();
	}

	public FlatIntegerInitializer(Integer floor, Integer ceiling) {
		super();
		this.floor = floor;
		this.ceiling = ceiling;
	}

	@Override
	public void readXMLConfig(Element e) {
		floor = Integer.parseInt(e.getAttribute("floor"));
		ceiling = Integer.parseInt(e.getAttribute("ceiling"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("floor", floor.toString());
		e.setAttribute("ceiling", ceiling.toString());
	}

	@Override
	public Integer create() {
		Random r = ThreadLocalRandom.current();
		int range = ceiling - floor;
		int value = floor + r.nextInt(range);
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
