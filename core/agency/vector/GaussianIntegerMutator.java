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
public class GaussianIntegerMutator implements ValueMutator<Integer> {
	static {
		Config.registerClassXMLTag(GaussianIntegerMutator.class);
	}
	
	double sd;

	public GaussianIntegerMutator() {
		super();
	}

	public GaussianIntegerMutator(double sd) {
		super();
		this.sd = sd;
	}

	@Override
	public void readXMLConfig(Element e) {
		sd = Double.parseDouble(e.getAttribute("sd"));
	}

	@Override
	public void writeXMLConfig(Document d, Element e) {
		e.setAttribute("sd", Double.toString(sd));
	}

	@Override
	public Integer mutate(Integer value) {
		Random r = ThreadLocalRandom.current();
		double z = r.nextGaussian();
		return (int) (value + (z*sd));
	}


}
