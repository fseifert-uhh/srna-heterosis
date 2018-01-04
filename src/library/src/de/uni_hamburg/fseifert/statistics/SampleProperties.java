package de.uni_hamburg.fseifert.statistics;

public class SampleProperties {
    public SampleProperties(double[] sampleValues, boolean samplePopulationFlag) {
        this.samplePopulationFlag = samplePopulationFlag;
        this.sampleValues = sampleValues;
        
        calculateMean();
        calculateVariance();
    }
    
    private void calculateMean() {
        mean = 0;
        
        for(double sampleValue : sampleValues) {
            mean += sampleValue;
        }
        
        mean /= ((double) sampleValues.length);
    }
    
    private void calculateVariance() {
        variance = 0;
        
        for(double sampleValue : sampleValues) {
            variance += Math.pow((mean - sampleValue), 2);
        }
        
        variance /= ((double) sampleValues.length - (samplePopulationFlag ? 1.0 : 0));
    }
    
    public double mean() {
        return mean;
    }

    public double standardDeviation() {
        return Math.sqrt(variance);
    }
    
    public double variance() {
        return variance;
    }
    
    boolean samplePopulationFlag;
    
    private double mean;
    private double variance;
    private double[] sampleValues;
}
