package de.uni_hamburg.fseifert.statistics;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class BinomialGroupDifferenceProbabilityEstimation {
    public BinomialGroupDifferenceProbabilityEstimation() {}
    
    public double getGroupDifferenceProbability(int elementCountLowGroup, int elementCountHighGroup) {
        binomialDistribution = new BinomialDistribution((elementCountLowGroup + elementCountHighGroup), 0.5);
        
        double probability = 1.0;
        
        if((elementCountLowGroup > 0) || (elementCountHighGroup > 0)) {
            // ONLY SIGNIFICANT FOR HIGH GROUP???
            probability = 0.0;
            for(int probabilityMassIndex = elementCountHighGroup; probabilityMassIndex <= (elementCountLowGroup + elementCountHighGroup); probabilityMassIndex++) {
                probability += binomialDistribution.probability(probabilityMassIndex);
            }
        }
        
        return probability;
    }
    
    private BinomialDistribution binomialDistribution;
}
