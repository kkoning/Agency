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
public class GaussianFloatInitializer implements ValueInitializer<Float> {
	static {
		Config.registerClassXMLTag(GaussianFloatInitializer.class);
	}
	
	float center;
	float sd;
	
	

	public GaussianFloatInitializer() {
		super();
	}

	public GaussianFloatInitializer(float center, float sd) {
		super();
		this.center = center;
		this.sd = sd;
	}

	@Override
	public void readXMLConfig(Element e) {
		center = Float.parseFloat(e.getAttribute("center"));
		sd = Float.parseFloat(e.getAttribute("sd"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("center", Float.toString(center));
		e.setAttribute("sd", Float.toString(sd));
	}

	@Override
	public Float create() {
		Random r = ThreadLocalRandom.current();
		double z = r.nextGaussian();
		return (float) (center + (z*sd));
	}


}
