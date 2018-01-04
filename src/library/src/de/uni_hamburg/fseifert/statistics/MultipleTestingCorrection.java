package de.uni_hamburg.fseifert.statistics;

import java.util.Arrays;

public class MultipleTestingCorrection {
    public MultipleTestingCorrection(double[] testProbabilitiesSubset, int testSetSize) {
        this(testProbabilitiesSubset);
        this.testSetSize = testSetSize;
    }
    
    public MultipleTestingCorrection(double[] testProbabilities) {
        this.testProbabilities = testProbabilities;
        this.testSetSize = testProbabilities.length;

        this.sortedTestProbabilities = getSortedTestProbabilities();
    }
    
    public double getCorrectedAlphaErrorThreshold(MultipleTestingCorrectionMethod multipleTestingCorrectionMethod, double alphaErrorThreshold) {
        double correctedAlphaErrorThreshold = 0;
        
        if(getTestSize() > 0) {
            switch(multipleTestingCorrectionMethod) {
                case FWER_BONFERRONI:
                    correctedAlphaErrorThreshold = (alphaErrorThreshold / (double) getTestSize()); 
                    
                    break;
                case FWER_BONFERRONI_HOLM:
                    for(int probabilityIndex = 0; probabilityIndex < getTestSize(); probabilityIndex++) {
                        if(getProbabilityOfSortedTests(probabilityIndex) <= (alphaErrorThreshold / (double) (getTestSize() - probabilityIndex))) {
                            correctedAlphaErrorThreshold = getProbabilityOfSortedTests(probabilityIndex);
                        }
                        else {
                            break;
                        }
                    }
                    
                    break;
                case NO_CORRECTION:
                    correctedAlphaErrorThreshold = alphaErrorThreshold;
                    
                    break;
                case FDR_SIDAK:
                    for(int probabilityIndex = getTestSize(); probabilityIndex > 0; probabilityIndex--) {
                        if(getProbabilityOfSortedTests(probabilityIndex - 1) <= (1.0 - Math.pow((1.0 - 0.05), probabilityIndex))) {
                            correctedAlphaErrorThreshold = getProbabilityOfSortedTests(probabilityIndex - 1);
                        }
                        else {
                            break;
                        }
                    }
                    
                    break;
                case FDR_BENJAMINI_HOCHBERG:
                default:
                    for(int probabilityIndex = 0; probabilityIndex < getTestSize(); probabilityIndex++) {
                        if(getProbabilityOfSortedTests(probabilityIndex) <= ((double) (probabilityIndex + 1) * alphaErrorThreshold / (double) getTestSize())) {
                            correctedAlphaErrorThreshold = getProbabilityOfSortedTests(probabilityIndex);
                        }
                        else {
/*                            System.out.println("DEBUG (BH-correction ends at):" + ((double) (probabilityIndex + 1) * alphaErrorThreshold / (double) getTestSize()));
                            System.out.println("lowest p-value achieved: " + getProbabilityOfSortedTests(0));*/
                            break;
                        }
                    }
            }
        }
        
        return correctedAlphaErrorThreshold;
    }
    
    private int getNumberOfSignificantTests(double correctedAlphaErrorThreshold) {
        int numberOfSignificantTests = 0;
        
        if(getTestSize() > 0) {
            for(; sortedTestProbabilities[numberOfSignificantTests] < correctedAlphaErrorThreshold; numberOfSignificantTests++);
        }
        
        return numberOfSignificantTests;
    }
    
    public double getProbabilityOfSortedTests(int index) {
        if(index >= sortedTestProbabilities.length) {
            return 1.0;
        }
        
        return sortedTestProbabilities[index];
    }
    
    private double[] getSortedTestProbabilities() {
        double[] sortedTestProbabilities = new double[getTestSize()];
        
        System.arraycopy(testProbabilities, 0, sortedTestProbabilities, 0, testProbabilities.length);
        Arrays.sort(sortedTestProbabilities);

        return sortedTestProbabilities;
    }
    
    private int getTestSize() {
        return testSetSize;
    }
    
    private double[] testProbabilities;
    private double[] sortedTestProbabilities;
    
    private int testSetSize = 0;
}