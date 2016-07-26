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
public class GaussianFloatMutator implements ValueMutator<Float> {
	
	float sd;

	public GaussianFloatMutator() {
		super();
	}

	public GaussianFloatMutator(float sd) {
		super();
		this.sd = sd;
	}

	@Override
	public void readXMLConfig(Element e) {
		sd = Float.parseFloat(e.getAttribute("sd"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("sd", Float.toString(sd));
	}

	@Override
	public Float mutate(Float value) {
		Random r = ThreadLocalRandom.current();
		double z = r.nextGaussian();
		return (float) (value + (z*sd));
	}


}
