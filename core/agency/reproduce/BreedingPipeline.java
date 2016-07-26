package agency.reproduce;

import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public interface BreedingPipeline extends XMLConfigurable {
	Individual generate();
	void setSourcePopulation(Population pop);
	
}
