package hybridcharacteristicsinglenucleotidepolymorphismassociationdatabase;

import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class HybridCharacteristicAssociationStatistic {
    public HybridCharacteristicAssociationStatistic(HashMap<Integer,Double> hybridCharacteristicValueHashMap) {
        this.hybridCharacteristicValueHashMap = hybridCharacteristicValueHashMap;
        
        double[] sortedHybridCharacteristicValues = new double[hybridCharacteristicValueHashMap.size()];
        int arrayIndex = 0;
        for(double hybridCharacteristicValue : hybridCharacteristicValueHashMap.values()) {
            sortedHybridCharacteristicValues[arrayIndex] = hybridCharacteristicValue;
            
            arrayIndex++;
        }
        
        Arrays.sort(sortedHybridCharacteristicValues);
        lowHybridCharacteristicGroupBorder = sortedHybridCharacteristicValues[sortedHybridCharacteristicValues.length / 2];
    }
    
    public double getStatistic(HashMap<Integer,Boolean> hybridParentDifferentialExpressionFlagHashMap) {
        int lowCharacteristicDifferentialCount = 0;
        int highCharacteristicDifferentialCount = 0;                

        for(int hybridIndex : hybridParentDifferentialExpressionFlagHashMap.keySet()) {
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
            BinomialDistribution binomialDistribution = new BinomialDistribution((highCharacteristicDifferentialCount + lowCharacteristicDifferentialCount), 0.5);

            boolean positiveAssociation = true;
            int massIndexStart = highCharacteristicDifferentialCount;
            int massIndexEnd = (highCharacteristicDifferentialCount + lowCharacteristicDifferentialCount);
            
            if(highCharacteristicDifferentialCount < lowCharacteristicDifferentialCount) {
                massIndexStart = lowCharacteristicDifferentialCount;

                positiveAssociation = false;
            }
            
            probability = 0;
            for(int probabilityMassIndex = massIndexStart; probabilityMassIndex <= massIndexEnd; probabilityMassIndex++) {
                probability += ((positiveAssociation ? 1 : -1) * binomialDistribution.probability(probabilityMassIndex));
            }
        }
        
        return probability;
    }

    private final HashMap<Integer,Double> hybridCharacteristicValueHashMap;
    
    private final double lowHybridCharacteristicGroupBorder;
}
