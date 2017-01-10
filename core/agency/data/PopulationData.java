package agency.data;

import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

import java.io.File;

/**
 Created by liara on 10/1/16.

 TODO: Javadoc for this file.
 */
public interface PopulationData extends XMLConfigurable {
void writePopulationData(int generation, Population pop);

void fitnessLandscapeOpen(String outputFile);

void fitnessLandscapeSample(int baseGeneration, int generationOffset, int
        sampleSet, Individual ind);

void fitnessLandscapeClose();

}
