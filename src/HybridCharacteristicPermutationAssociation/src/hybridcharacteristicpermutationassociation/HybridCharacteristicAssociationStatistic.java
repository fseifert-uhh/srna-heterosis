package hybridcharacteristicpermutationassociation;

import java.util.Arrays;
import java.util.HashMap;
import jsc.distributions.Binomial;

public class HybridCharacteristicAssociationStatistic {
    public HybridCharacteristicAssociationStatistic(HashMap<Integer,Double> hybridCharacteristicValueHashMap, HashMap<Integer,Boolean> hybridParentDifferentialExpressionFlag) {
        this.hybridCharacteristicValueHashMap = hybridCharacteristicValueHashMap;
        
        double[] sortedHybridCharacteristicValues = new double[hybridCharacteristicValueHashMap.size()];
        int arrayIndex = 0;
        for(double hybricCharacteristicValue : hybridCharacteristicValueHashMap.values()) {
            sortedHybridCharacteristicValues[arrayIndex] = hybricCharacteristicValue;
            
            arrayIndex++;
        }
        
        Arrays.sort(sortedHybridCharacteristicValues);
        lowHybridCharacteristicGroupBorder = sortedHybridCharacteristicValues[sortedHybridCharacteristicValues.length / 2];
        
        this.hybridParentDifferentialExpressionFlagHashMap = hybridParentDifferentialExpressionFlag;
    }
    
    public double getStatistic(double minExpressionThreshold, double minFoldChangeThreshold) {
        int lowCharacteristicDifferentialCount = 0;
        int highCharacteristicDifferentialCount = 0;                

        for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
            if(hybridParentDifferentialExpressionFlagHashMap.get(hybridIndex)) {
                if(hybridCharacteristicValueHashMap.get(hybridIndex) < lowHybridCharacteristicGroupBorder) {
                    lowCharacteristicDifferentialCount++;
                }
                else {
                    highCharacteristicDifferentialCount++;
                }
            }
        }

        double probability = 1.0;
        if((lowCharacteristicDifferentialCount > 0) || (highCharacteristicDifferentialCount > 0)) {
            Binomial binomialDistribution = new Binomial((highCharacteristicDifferentialCount + lowCharacteristicDifferentialCount), 0.5);
            
            probability = 0;
            
            boolean positiveAssociation = true;
            int massIndexStart = highCharacteristicDifferentialCount;
            if(highCharacteristicDifferentialCount < lowCharacteristicDifferentialCount) {
                massIndexStart = lowCharacteristicDifferentialCount;
                positiveAssociation = false;
            }
            
            for(int probabilityMassIndex = massIndexStart; probabilityMassIndex <= (highCharacteristicDifferentialCount + lowCharacteristicDifferentialCount); probabilityMassIndex++) {
                probability += binomialDistribution.pdf(probabilityMassIndex);
            }
            
            if(!positiveAssociation) {
                probability *= -1;
            }
        }
        
        return probability;
    }
    
    private double lowHybridCharacteristicGroupBorder;

    private HashMap<Integer,Boolean> hybridParentDifferentialExpressionFlagHashMap;
    private HashMap<Integer,Double> hybridCharacteristicValueHashMap;
}
