package hybridcharacteristicpermutationassociation;

import java.util.HashMap;

public class DifferentialExpressedElementData {
    public DifferentialExpressedElementData(HashMap<Integer,HashMap<Integer,Boolean>> elementDifferentialExpressionFlagHashMap, HashMap<Integer,HashMap<Integer,Double>> elementExpressionHashMap) {
        this.elementDifferentialExpressionFlagHashMap = elementDifferentialExpressionFlagHashMap;
        this.elementExpressionHashMap = elementExpressionHashMap;
    }
    
    public HashMap<Integer,HashMap<Integer,Boolean>> getDifferentialFlagData() {
        return elementDifferentialExpressionFlagHashMap;
    }
    
    public HashMap<Integer,HashMap<Integer,Double>> getExpressionData() {
        return elementExpressionHashMap;
    }
    
    private HashMap<Integer,HashMap<Integer,Boolean>> elementDifferentialExpressionFlagHashMap;
    private HashMap<Integer,HashMap<Integer,Double>> elementExpressionHashMap;
}
