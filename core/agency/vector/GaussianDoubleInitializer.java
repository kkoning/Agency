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
public class GaussianDoubleInitializer implements ValueInitializer<Double> {
	static {
		Config.registerClassXMLTag(GaussianDoubleInitializer.class);
	}
	
	double center;
	double sd;

	public GaussianDoubleInitializer() {
		super();
	}

	public GaussianDoubleInitializer(double center, double sd) {
		super();
		this.center = center;
		this.sd = sd;
	}

	@Override
	public void readXMLConfig(Element e) {
		center = Double.parseDouble(e.getAttribute("center"));
		sd = Double.parseDouble(e.getAttribute("sd"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("center", Double.toString(center));
		e.setAttribute("sd", Double.toString(sd));
	}

	@Override
	public Double create() {
		Random r = ThreadLocalRandom.current();
		double z = r.nextGaussian();
		return (double) (center + (z*sd));
	}


}
