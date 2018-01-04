package hybridcharacteristicpermutationassociation;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrectionMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import jsc.datastructures.PairedData;
import jsc.regression.PearsonCorrelation;

public class HybridCharacteristicPermutationAssociation {
    public HybridCharacteristicPermutationAssociation(DatabaseLoginData databaseLoginData, BreedingSetData breedingSetData) {
        this.breedingSetData = breedingSetData;
        this.databaseLoginData = databaseLoginData;
    }

    public DifferentialExpressedElementData getDifferentialExpressedSequences(SequenceMappingStatus sequenceMappingStatus, String startCharacter, int sequenceLength, double minExpressionThreshold, double minFoldChange) throws SQLException {
        HashMap<Integer,HashMap<Integer,Boolean>> differentialExpressedSequenceFlagHashMap = new HashMap();
        HashMap<Integer,HashMap<Integer,Double>> differentialExpressedSequenceExpressionHashMap = new HashMap();

        DatabaseConnection databaseConnection = null;
        Statement inbredLineStatement = null;
        Statement sequenceDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
        
            if(databaseConnection.isActive()) {
                String mappingStatusQuery = "";
                switch(sequenceMappingStatus) {
                    case MAPPED:
                        mappingStatusQuery = " AND srna_mapping_status.mapping_count>=1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                        break;
                    case UNMAPPED:
                        mappingStatusQuery = " AND srna_mapping_status.mapping_count=0 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                        break;
                    case SINGLE_LOCI:
                        mappingStatusQuery = " AND srna_mapping_status.mapping_count=1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                        break;
                    case MULTIPLE_LOCI:
                        mappingStatusQuery = " AND srna_mapping_status.mapping_count>1 AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";

                        break;
                    case ALL:
                    default:
                        mappingStatusQuery = " AND srna_library_expression.sequence_id=srna_mapping_status.sequence_id";
                }

                String sqlSequenceLengthConstraint = " AND srna_sequence.length>= 18 AND srna_sequence.length<=28";
                if(sequenceLength > 0) {
                    sqlSequenceLengthConstraint = " AND srna_sequence.length=" + sequenceLength;
                }

                String sqlStartCharacterConstraint = "";
                if(!startCharacter.isEmpty()) {
                    sqlStartCharacterConstraint = " AND sequence LIKE \"" + startCharacter + "%\"";
                }

                HashMap<Integer,String> inbredLineHashMap = new HashMap();
                
                sequenceDataStatement = databaseConnection.getConnection().createStatement();
                ResultSet inbredLineResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_libraries");
                while(inbredLineResultSet.next()) {
                    int inbredIndex = inbredLineResultSet.getInt("library_id");
                    String inbredTitle = inbredLineResultSet.getString("library_title");
                    
                    inbredLineHashMap.put(inbredIndex, inbredTitle);
                }
                inbredLineResultSet.close();
                sequenceDataStatement.close();
                
                sequenceDataStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);

                ResultSet sequenceExpressionResultSet = sequenceDataStatement.executeQuery("SELECT srna_library_expression.* FROM  srna_library_expression, srna_mapping_status, srna_sequence WHERE srna_library_expression.sequence_id=srna_sequence.sequence_id " + mappingStatusQuery + sqlSequenceLengthConstraint + sqlStartCharacterConstraint);
                while(sequenceExpressionResultSet.next()) {
                    int sequenceIndex = sequenceExpressionResultSet.getInt("sequence_id");

                    HashMap<Integer,Double> inbredSequenceExpressionHashMap = new HashMap();
                    
                    for(int inbredIndex : inbredLineHashMap.keySet()) {
                        inbredSequenceExpressionHashMap.put(inbredIndex, sequenceExpressionResultSet.getDouble(inbredLineHashMap.get(inbredIndex)));
                    }
                    
                    HashMap<Integer,Boolean> hybridDifferentialExpressionFlag = this.getHybridInbredParentDifferentialExpression(inbredSequenceExpressionHashMap, minExpressionThreshold, minFoldChange);
                    for(Integer hybridIndex : hybridDifferentialExpressionFlag.keySet()) {
                        if(hybridDifferentialExpressionFlag.get(hybridIndex)) {
                            differentialExpressedSequenceFlagHashMap.put(sequenceIndex, hybridDifferentialExpressionFlag);
                            differentialExpressedSequenceExpressionHashMap.put(sequenceIndex, inbredSequenceExpressionHashMap);
                            
                            break;
                        }
                    }
                }
            }
        }
        finally {
            if(databaseConnection != null) {
                if(inbredLineStatement != null) {
                    inbredLineStatement.close();
                }
                if(sequenceDataStatement != null) {
                    sequenceDataStatement.close();
                }
            
                databaseConnection.close();
            }
        }
        
        DifferentialExpressedElementData differentialExpressedElementData = new DifferentialExpressedElementData(differentialExpressedSequenceFlagHashMap, differentialExpressedSequenceExpressionHashMap);
        
        return differentialExpressedElementData;
    }
    
    public ArrayList<Integer> getBreedingSetDifferentialExpressedSequences(HashMap<Integer,HashMap<Integer,Boolean>> hybridDifferentialExpressionHashMap) throws SQLException {
        ArrayList<Integer> differentialExpressedSequences = new ArrayList();
        
        for(int sequenceIndex : hybridDifferentialExpressionHashMap.keySet()) {
            HashMap<Integer,Boolean> hybridDifferentialExpressionFlag = hybridDifferentialExpressionHashMap.get(sequenceIndex);
            
            for(int hybridIndex : breedingSetData.getHybridIndices()) {
                int[] inbredParentIds = breedingSetData.getHybridParentIndices(hybridIndex);
                
                if(hybridDifferentialExpressionFlag.get(hybridIndex)) {
                    differentialExpressedSequences.add(sequenceIndex);
                    
                    break;
                }
            }
        }
        
        return differentialExpressedSequences;
    }
    
    public void getAssociatedMinimumPValue(HashMap<Integer,HashMap<Integer,Boolean>> differentialExpressedSequencesHashMap, double minExpressionThreshold, double minFoldChangeThreshold, double alphaError, MultipleTestingCorrectionMethod multipleTestingCorrectionMethod) throws SQLException {
        HashMap<Integer,Double>[] putativeAssociatedSequences = new HashMap[2];
        putativeAssociatedSequences[0] = new HashMap();
        putativeAssociatedSequences[1] = new HashMap();
        HashMap<Integer,Double>[] associatedSequences = new HashMap[2];
        associatedSequences[0] = new HashMap();
        associatedSequences[1] = new HashMap();

        HashMap<Integer,Double> hybridCharacteristicValueHashMap = breedingSetData.getHybridCharacteristicValues();

        
        int[] inbredIndices;
        HashMap<Integer,Double> associationHybridCharacteristicValueHashMap = new HashMap();

        double minPValue = 1.0; 
        for(int sequenceIndex : differentialExpressedSequencesHashMap.keySet()) {
            HashMap<Integer,Boolean> hybridDifferentialExpressionFlagHashMap = differentialExpressedSequencesHashMap.get(sequenceIndex);

            for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
                if(hybridDifferentialExpressionFlagHashMap.get(hybridIndex)) {
                    HybridCharacteristicAssociationStatistic hybridCharacteristicAssociationStatistic = new HybridCharacteristicAssociationStatistic(associationHybridCharacteristicValueHashMap, hybridDifferentialExpressionFlagHashMap);
                    double sequenceAssociationProbability = hybridCharacteristicAssociationStatistic.getStatistic(minExpressionThreshold, minFoldChangeThreshold);

                    if(Math.abs(sequenceAssociationProbability) < minPValue) {
                        minPValue = Math.abs(sequenceAssociationProbability);
                    }
                    
                    if(Math.abs(sequenceAssociationProbability) < alphaError) {
                        putativeAssociatedSequences[(sequenceAssociationProbability > 0) ? 0 : 1].put(sequenceIndex, sequenceAssociationProbability);
                    }

                    break;
                }
            }
        }     
        System.out.print("\t" + minPValue);
    }
    
    public HashMap<Integer,Boolean> getHybridInbredParentDifferentialExpression(HashMap<Integer,Double> inbredParentExpression, double minExpressionThreshold, double minFoldChangeThreshold) {
        HashMap<Integer,Boolean> hybridInbredParentDifferentialExpressionFlag = new HashMap();
        
        for(int hybridIndex : breedingSetData.getHybridIndices()) {
            int[] hybridParentInbredIndices = breedingSetData.getHybridParentIndices(hybridIndex);
            double lowInbredParentExpression = Math.min(inbredParentExpression.get(hybridParentInbredIndices[0]), inbredParentExpression.get(hybridParentInbredIndices[1]));
            double highInbredParentExpression = Math.max(inbredParentExpression.get(hybridParentInbredIndices[0]), inbredParentExpression.get(hybridParentInbredIndices[1]));

            hybridInbredParentDifferentialExpressionFlag.put(hybridIndex, false);
            
            if(highInbredParentExpression < minExpressionThreshold) {
                continue;
            }
            
            if(minExpressionThreshold > 0) {
                if(lowInbredParentExpression < minExpressionThreshold) {
                    if(highInbredParentExpression >= (minExpressionThreshold * minFoldChangeThreshold)) {
                        hybridInbredParentDifferentialExpressionFlag.put(hybridIndex, true);
                    }
                }
                else if((highInbredParentExpression / lowInbredParentExpression) >= minFoldChangeThreshold) {
                    hybridInbredParentDifferentialExpressionFlag.put(hybridIndex, true);
                }
            }
            else {
                if(lowInbredParentExpression == 0) {
                    if(highInbredParentExpression >= minExpressionThreshold * minFoldChangeThreshold) {
                        hybridInbredParentDifferentialExpressionFlag.put(hybridIndex, true);
                    }
                }
                else if((highInbredParentExpression / lowInbredParentExpression) >= minFoldChangeThreshold) {
                    hybridInbredParentDifferentialExpressionFlag.put(hybridIndex, true);
                }
            }
        }
        
        return hybridInbredParentDifferentialExpressionFlag;
    }
    
    public HashMap<Integer,HashMap<Integer,Double>>[] getInbredSequenceExpressionData(ArrayList<Integer>[] sequenceIndexArrayList, HashMap<Integer,HashMap<Integer,Double>> sequenceExpressionDataHashMap) throws SQLException {
        HashMap<Integer,HashMap<Integer,Double>>[] inbredSequenceExpressionHashMap = new HashMap[2];
        inbredSequenceExpressionHashMap[0] = new HashMap();
        inbredSequenceExpressionHashMap[1] = new HashMap();
    
        for(int associationTypeIndex = 0; associationTypeIndex < 2; associationTypeIndex++) {
            for(int sequenceIndex : sequenceIndexArrayList[associationTypeIndex]) {
                inbredSequenceExpressionHashMap[associationTypeIndex].put(sequenceIndex, sequenceExpressionDataHashMap.get(sequenceIndex));
            }
        }
        
        return inbredSequenceExpressionHashMap;
    }
    
    public PearsonCorrelation performCorrelation(HashMap<Integer,HashMap<Integer,Double>>[] associatedElementInbredExpression, double minExpressionThreshold, double minFoldChangeThreshold, double alphaError) {
        if(associatedElementInbredExpression[0].isEmpty() || associatedElementInbredExpression[1].isEmpty()) {
            return null;
        }
        
        HashMap<Integer,Double>[] elementBinaryDistanceHashMap = new HashMap[2];
        elementBinaryDistanceHashMap[0] = new HashMap();
        elementBinaryDistanceHashMap[1] = new HashMap();
        HashMap<Integer,Double>[] elementEuclideanDistanceHashMap = new HashMap[2];
        elementEuclideanDistanceHashMap[0] = new HashMap();
        elementEuclideanDistanceHashMap[1] = new HashMap();

        for(int associationTypeIndex = 0; associationTypeIndex < 2; associationTypeIndex++) {
            for(int elementIndex : associatedElementInbredExpression[associationTypeIndex].keySet()) {
                HashMap<Integer,Boolean> hybridInbredParentDifferentialExpression = getHybridInbredParentDifferentialExpression(associatedElementInbredExpression[associationTypeIndex].get(elementIndex), minExpressionThreshold, minFoldChangeThreshold);

                for(int hybridIndex : breedingSetData.getHybridIndices()) {
                    if(!elementBinaryDistanceHashMap[associationTypeIndex].containsKey(hybridIndex)) {
                        elementBinaryDistanceHashMap[associationTypeIndex].put(hybridIndex, 0.0);
                        elementEuclideanDistanceHashMap[associationTypeIndex].put(hybridIndex, 0.0);
                    }

                    int[] hybridParentIndices = breedingSetData.getHybridParentIndices(hybridIndex);

                    if(hybridInbredParentDifferentialExpression.get(hybridIndex)) {
                        double currentBinaryDistance = elementBinaryDistanceHashMap[associationTypeIndex].get(hybridIndex);
                        elementBinaryDistanceHashMap[associationTypeIndex].put(hybridIndex, (currentBinaryDistance + 1.0));
                    }

                    double lowParentExpression = Math.min(associatedElementInbredExpression[associationTypeIndex].get(elementIndex).get(hybridParentIndices[0]), associatedElementInbredExpression[associationTypeIndex].get(elementIndex).get(hybridParentIndices[1]));
                    double highParentExpression = Math.max(associatedElementInbredExpression[associationTypeIndex].get(elementIndex).get(hybridParentIndices[0]), associatedElementInbredExpression[associationTypeIndex].get(elementIndex).get(hybridParentIndices[1]));

                    double currentEuclideanDistance = elementEuclideanDistanceHashMap[associationTypeIndex].get(hybridIndex);
                    elementEuclideanDistanceHashMap[associationTypeIndex].put(hybridIndex, (currentEuclideanDistance + Math.pow((highParentExpression - lowParentExpression), 2)));
                }
            }
        }
        
        HashMap<Integer,Double> hybridBinaryDistanceHashMap = new HashMap();
        HashMap<Integer,Double> hybridEuclideanDistanceHashMap = new HashMap();
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = new HashMap();

        boolean euclideanDistanceNonConstant = false;
        
        double binaryDistanceReferenceValue = 0;
        for(int hybridIndex : breedingSetData.getHybridIndices()) {
            hybridBinaryDistanceHashMap.put(hybridIndex, (Math.sqrt(elementBinaryDistanceHashMap[0].get(hybridIndex) / (double) associatedElementInbredExpression[0].size()) * ((double) associatedElementInbredExpression[0].size() / ((double) associatedElementInbredExpression[0].size() + (double) associatedElementInbredExpression[1].size()))) + ((1.0 - Math.sqrt(elementBinaryDistanceHashMap[1].get(hybridIndex) / (double) associatedElementInbredExpression[1].size())) * ((double) associatedElementInbredExpression[1].size() / ((double) associatedElementInbredExpression[0].size() + (double) associatedElementInbredExpression[1].size()))));
            hybridEuclideanDistanceHashMap.put(hybridIndex, (Math.sqrt(elementEuclideanDistanceHashMap[0].get(hybridIndex) + Math.sqrt(elementEuclideanDistanceHashMap[1].get(hybridIndex)))));

            if(hybridBinaryDistanceHashMap.size() == 1) {
                binaryDistanceReferenceValue = hybridBinaryDistanceHashMap.get(hybridIndex);
            }
            else {
                if(hybridBinaryDistanceHashMap.get(hybridIndex) != binaryDistanceReferenceValue) {
                    euclideanDistanceNonConstant = true;
                }
            }
            
            hybridCharacteristicValueHashMap.put(hybridIndex, breedingSetData.getHybridCharacteristicValue(hybridIndex));
        }
        
        if(euclideanDistanceNonConstant) {
            double[] hybridBinaryDistance = new double[hybridCharacteristicValueHashMap.size()];
            double[] hybridEuclideanDistance = new double[hybridCharacteristicValueHashMap.size()];
            double[] hybridCharacteristicValues = new double[hybridCharacteristicValueHashMap.size()];
            
            int arrayIndex = 0;
            for(int hybridIndex : breedingSetData.getHybridIndices()) {
                hybridBinaryDistance[arrayIndex] = hybridBinaryDistanceHashMap.get(hybridIndex);
                hybridEuclideanDistance[arrayIndex] = hybridEuclideanDistanceHashMap.get(hybridIndex);
                hybridCharacteristicValues[arrayIndex] = hybridCharacteristicValueHashMap.get(hybridIndex);
                
                arrayIndex++;
            }

            PearsonCorrelation binaryDistanceHybridCharacteristicCorrelation = new PearsonCorrelation(new PairedData(hybridBinaryDistance, hybridCharacteristicValues));
            PearsonCorrelation euclideanDistanceHybridCharacteristicCorrelation = new PearsonCorrelation(new PairedData(hybridEuclideanDistance, hybridCharacteristicValues));
        
            return binaryDistanceHybridCharacteristicCorrelation;
        }
        
        return null;
    }
    
    public static void main(String[] args) throws SQLException {
        double alphaError = 0.05;
        double minExpressionThreshold = 0.5;
        double minFoldChange = 2.0;
        
        int sequenceLength = 0;
        
        HybridCharacteristic hybridCharacteristic = null;
        
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-alphaError <value>");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.out.println("-foldChange <value>");
            System.out.println("-minExpression <value>");
            System.out.println("-sequenceLength <value>");
            System.out.println("-traitCharacteristic {MPH|BPH|HP}");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("alphaError")) {
                        alphaError = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    else if(argumentTitle.equals("foldChange")) {
                        minFoldChange = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("minExpression")) {
                        minExpressionThreshold = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("sequenceLength")) {
                        sequenceLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("traitCharacteristic")) {
                        if(argumentValue.equals("BPH")) {
                            hybridCharacteristic = HybridCharacteristic.BEST_PARENT_HETEROSIS;
                        }
                        else if(argumentValue.equals("HP")) {
                            hybridCharacteristic = HybridCharacteristic.HYBRID_PERFORMANCE;
                        }
                        else {
                            hybridCharacteristic = HybridCharacteristic.MID_PARENT_HETEROSIS;
                        }
                    }
                }
            }
        }
        
        if((databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-alphaError <value>");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.out.println("-foldChange <value>");
            System.out.println("-minExpression <value>");
            System.out.println("-sequenceLength <value>");
            System.out.println("-traitCharacteristic {MPH|BPH|HP}");
            System.exit(1);
        }
                
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);

            boolean permutationFlag = true;
            String[][] germplasmInbredLines = {{"f037", "f039", "f043", "f047", "l024", "l035", "l043"}, {"p033", "p040", "p046", "p048", "p063", "p066", "s028", "s036", "s044", "s046", "s049", "s050", "s058", "s067"}};
            BreedingSetData breedingSetData = new BreedingSetData(databaseLoginData, germplasmInbredLines, hybridCharacteristic, permutationFlag);
            
            HybridCharacteristicPermutationAssociation hybridCharacteristicPrediction = new HybridCharacteristicPermutationAssociation(databaseLoginData, breedingSetData);
            
            String sequence5PrimeNucleotide = "";

            DifferentialExpressedElementData differentialExpressedSequenceData = hybridCharacteristicPrediction.getDifferentialExpressedSequences(SequenceMappingStatus.ALL, sequence5PrimeNucleotide, sequenceLength, minExpressionThreshold, minFoldChange);
            
            int[] germplasmEstimationSelectionSize = {5, 14};

            Random randomNumberGenerator = new Random();

            for(int crossValidationRun = 0; crossValidationRun < 1000; crossValidationRun++) {
                System.out.print(crossValidationRun);
                
                boolean[][] selectedEstimationGermplasmInbredLines = {new boolean[germplasmInbredLines[0].length], new boolean[germplasmInbredLines[1].length]};
                for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
                    int selectedInbredsCount = 0;
                    
                    do {
                        int randomInbredIndex = randomNumberGenerator.nextInt(germplasmInbredLines[germplasmIndex].length);
                        if(!selectedEstimationGermplasmInbredLines[germplasmIndex][randomInbredIndex]) {
                            selectedEstimationGermplasmInbredLines[germplasmIndex][randomInbredIndex] = true;

                            selectedInbredsCount++;
                        }
                    } while(selectedInbredsCount < germplasmEstimationSelectionSize[germplasmIndex]);
                }
                
                BreedingSetData crossValidationEstimationBreedingSetData = new BreedingSetData(databaseLoginData, germplasmInbredLines, selectedEstimationGermplasmInbredLines, hybridCharacteristic, permutationFlag);

                HybridCharacteristicPermutationAssociation crossValidationHybridCharacteristicPrediction = new HybridCharacteristicPermutationAssociation(databaseLoginData, crossValidationEstimationBreedingSetData);
                
                ArrayList<Integer> crossValidationDifferentialExpressedSequenceIndexArrayList = crossValidationHybridCharacteristicPrediction.getBreedingSetDifferentialExpressedSequences(differentialExpressedSequenceData.getDifferentialFlagData());

                HashMap<Integer,HashMap<Integer,Boolean>> crossValidationDifferentialExpressionFlagHashMap = new HashMap();
                for(int sequenceIndex : crossValidationDifferentialExpressedSequenceIndexArrayList) {
                    crossValidationDifferentialExpressionFlagHashMap.put(sequenceIndex, differentialExpressedSequenceData.getDifferentialFlagData().get(sequenceIndex));
                }
                
                crossValidationHybridCharacteristicPrediction.getAssociatedMinimumPValue(crossValidationDifferentialExpressionFlagHashMap, minExpressionThreshold, minFoldChange, alphaError, MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG);
            }         
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    private final BreedingSetData breedingSetData;
    
    private final DatabaseLoginData databaseLoginData;
}
