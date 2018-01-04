package hybridcharacteristiccorrelationdatabase;

import java.io.Serializable;
import java.util.HashMap;

/**
 * data structure links differential expression flags to expression elements (transcripts, sRNAs, SNPs ...)
 * 
 * @author Felix Seifert
 */

public class HybridExpressionElementData implements Serializable {
    public HybridExpressionElementData(HashMap<String,HashMap<Integer,Boolean>> elementDifferentialExpressionFlagHashMap, HashMap<String,HashMap<Integer,Double>> elementExpressionHashMap) {
        this.elementDifferentialExpressionFlagHashMap = elementDifferentialExpressionFlagHashMap;
        this.elementExpressionHashMap = elementExpressionHashMap;
    }
    
    /**
     * obtains differential expression data for hybrid parental inbred lines for a set of expression elements
     * 
     * @return HashMap of expression elements containing a HashMap of differential expression flags mapped by hybrid indices
     */
    public HashMap<String,HashMap<Integer,Boolean>> getDifferentialFlagData() {
        return elementDifferentialExpressionFlagHashMap;
    }
    
    /**
     * obtains expression data for hybrid parental inbred lines for a set of expression elements
     * 
     * @return HashMap of expression elements containing a HashMap of expression data mapped by hybrid indices
     */
    public HashMap<String,HashMap<Integer,Double>> getExpressionData() {
        return elementExpressionHashMap;
    }
    
    /**
     * obtains expression data for hybrid parental inbred lines for a set of expression elements
     * 
     * @param elementIndex element index
     */
    public void removeElement(int elementIndex) {
        if(elementDifferentialExpressionFlagHashMap.get(elementIndex) != null) {
            elementDifferentialExpressionFlagHashMap.remove(elementIndex);
            elementExpressionHashMap.remove(elementIndex);
        }
    }
    
    private final HashMap<String,HashMap<Integer,Boolean>> elementDifferentialExpressionFlagHashMap;
    private final HashMap<String,HashMap<Integer,Double>> elementExpressionHashMap;
}
