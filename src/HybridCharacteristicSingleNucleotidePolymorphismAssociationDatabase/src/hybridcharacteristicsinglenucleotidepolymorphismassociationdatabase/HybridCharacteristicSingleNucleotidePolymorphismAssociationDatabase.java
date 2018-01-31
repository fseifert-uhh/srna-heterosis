package hybridcharacteristicsinglenucleotidepolymorphismassociationdatabase;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrection;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrectionMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class HybridCharacteristicSingleNucleotidePolymorphismAssociationDatabase {
    public HybridCharacteristicSingleNucleotidePolymorphismAssociationDatabase(DatabaseLoginData databaseLoginData, MultipleTestingCorrectionMethod multipleTestingCorrectionMethod) throws SQLException {
        this.databaseLoginData = databaseLoginData;
        
        this.loadHybridInbredParentIdentifiers();
        this.loadInbredTitles();
        
        this.multipleTestingCorrectionMethod = multipleTestingCorrectionMethod;
        
        markerInbredParentNucleotideHashMap = new HashMap();
    }
    
    public HashMap<Integer,Boolean> getHybridInbredParentDifferingSingleNucleotidePolymorphism(int markerIndex) {
        HashMap<Integer,Boolean> hybridInbredParentDifferentSNPFlagHashMap = new HashMap();
        
        inbredParentNucleotideHashMap = markerInbredParentNucleotideHashMap.get(markerIndex);
        
        for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
            String firstParentSingleNucleotidePolymorphism = inbredParentNucleotideHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[0]);
            String secondParentSingleNucleotidePolymorphism = inbredParentNucleotideHashMap.get(hybridInbredParentIndicesHashMap.get(hybridIndex)[1]);
           
            if(!firstParentSingleNucleotidePolymorphism.equals(secondParentSingleNucleotidePolymorphism)) {
                hybridInbredParentDifferentSNPFlagHashMap.put(hybridIndex, true);
            }
            else{
                hybridInbredParentDifferentSNPFlagHashMap.put(hybridIndex, false);
            }
        }
        
        return hybridInbredParentDifferentSNPFlagHashMap;
    }

    public HashMap<Integer,Double> getAssociatedSingleNucleotidePolymorphisms(HybridCharacteristic hybridCharacteristic, double alphaError) throws SQLException {
        HashMap<Integer,Double> putativeAssociatedSequencePolymorphismHashMap = new HashMap();
        HashMap<Integer,Double> significantlyAssociatedSequencePolymorphismHashMap = new HashMap();

        HashMap<Integer,Double> hybridCharacteristicValueHashMap = getHybridCharacteristicValues(hybridCharacteristic);

        int sequenceCount = 0;
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequencePolymorphismStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequencePolymorphismStatement.setFetchSize(Integer.MIN_VALUE);

        ResultSet sequencePolymorphismResultSet = sequencePolymorphismStatement.executeQuery("SELECT * FROM snp_library_data");
        while(sequencePolymorphismResultSet.next()) {
            int markerIndex = sequencePolymorphismResultSet.getInt("marker_id");
            boolean fullDatasetFlag = true;

            inbredParentNucleotideHashMap = new HashMap();
            for(int inbredIndex : inbredTitleHashMap.keySet()) {
                String inbredParentNucleotide = sequencePolymorphismResultSet.getString(inbredTitleHashMap.get(inbredIndex));

                if(inbredParentNucleotide.equals("NA") || inbredParentNucleotide.equals("XX")) {
                    fullDatasetFlag = false;
                }

                inbredParentNucleotideHashMap.put(inbredIndex, inbredParentNucleotide);
            }

            if(!fullDatasetFlag) {
                continue;
            }

            markerInbredParentNucleotideHashMap.put(markerIndex, inbredParentNucleotideHashMap);

            HashMap<Integer,Boolean> hybridParentDifferentialMarkerSingleNucleotidePolymorphismHashMap = getHybridInbredParentDifferingSingleNucleotidePolymorphism(markerIndex);
            for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
                if(hybridParentDifferentialMarkerSingleNucleotidePolymorphismHashMap.get(hybridIndex)) {
                    sequenceCount++;

                    break;
                }
            }

            HybridCharacteristicAssociationStatistic hybridCharacteristicAssociationStatistic = new HybridCharacteristicAssociationStatistic(hybridCharacteristicValueHashMap);
            double sequenceAssociationProbability = hybridCharacteristicAssociationStatistic.getStatistic(hybridParentDifferentialMarkerSingleNucleotidePolymorphismHashMap);

            if(Math.abs(sequenceAssociationProbability) < alphaError) {
                putativeAssociatedSequencePolymorphismHashMap.put(markerIndex, sequenceAssociationProbability);
            }
        }
        sequencePolymorphismResultSet.close();
        sequencePolymorphismStatement.close();
        databaseConnection.close();

        System.out.print(sequenceCount + ";");

        if(putativeAssociatedSequencePolymorphismHashMap.size() > 0) {
            double[] candidateProbabilities = new double[putativeAssociatedSequencePolymorphismHashMap.size()];            
            int probabilityIndex = 0;
            for(double probabilityValue : putativeAssociatedSequencePolymorphismHashMap.values()) {
                candidateProbabilities[probabilityIndex] = Math.abs(probabilityValue);

                probabilityIndex++;
            }

            MultipleTestingCorrection multipleTestingCorrection = new MultipleTestingCorrection(candidateProbabilities, sequenceCount);
            double multipleTestingCorrectedAlphaError = multipleTestingCorrection.getCorrectedAlphaErrorThreshold(multipleTestingCorrectionMethod, alphaError);
            System.out.print(multipleTestingCorrectedAlphaError + ";");

            for(int candidateSingleNucleotidePolymorphismIndex : putativeAssociatedSequencePolymorphismHashMap.keySet()) {
                if(Math.abs(putativeAssociatedSequencePolymorphismHashMap.get(candidateSingleNucleotidePolymorphismIndex)) <= multipleTestingCorrectedAlphaError) {
                    significantlyAssociatedSequencePolymorphismHashMap.put(candidateSingleNucleotidePolymorphismIndex, putativeAssociatedSequencePolymorphismHashMap.get(candidateSingleNucleotidePolymorphismIndex));
                }
            }
        }

        
        System.out.println(significantlyAssociatedSequencePolymorphismHashMap.size());
        
        return significantlyAssociatedSequencePolymorphismHashMap;
    }
    
    private void loadInbredTitles() throws SQLException {
        inbredTitleHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredTitleStatement = databaseConnection.getConnection().createStatement();
        ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE germplasm=0 OR germplasm=1 order by library_id");
        while(inbredTitleResultSet.next()) {
            int inbredId = inbredTitleResultSet.getInt("library_id");
            String inbredTitle = inbredTitleResultSet.getString("library_title");
            
            inbredTitleHashMap.put(inbredId, inbredTitle);
        }
        inbredTitleResultSet.close();
        inbredTitleStatement.close();
        databaseConnection.close();
    }
    
    private HashMap<Integer,Double> getHybridCharacteristicValues(HybridCharacteristic hybridCharacteristic) throws SQLException {
        String hybridCharacteristicDatabaseColumn;
        switch(hybridCharacteristic) {
            case BEST_PARENT_HETEROSIS:
                hybridCharacteristicDatabaseColumn = "best_parent_heterosis_r";
                break;
            case HYBRID_PERFORMANCE:
                hybridCharacteristicDatabaseColumn = "hybrid_performance";
                break;
            case MID_PARENT_HETEROSIS:
            default:
                hybridCharacteristicDatabaseColumn = "mid_parent_heterosis_r";
        }
        
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement hybridCharacteristicStatement = databaseConnection.getConnection().createStatement();
        
        ResultSet hybridCharacteristicResultSet = hybridCharacteristicStatement.executeQuery("SELECT * FROM hybrid_field_data");
        while(hybridCharacteristicResultSet.next()) {
            int hybridIndex = hybridCharacteristicResultSet.getInt("hybrid_id");
            double hybridCharacteristicValue = hybridCharacteristicResultSet.getDouble(hybridCharacteristicDatabaseColumn);

            hybridCharacteristicValueHashMap.put(hybridIndex, hybridCharacteristicValue);
        }

        hybridCharacteristicResultSet.close();
        hybridCharacteristicStatement.close();
        databaseConnection.close();
        
        return hybridCharacteristicValueHashMap;
    }
    
    private void loadHybridInbredParentIdentifiers() throws SQLException {
        hybridInbredParentIndicesHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement hybridParentsStatement = databaseConnection.getConnection().createStatement();
        ResultSet hybridParentsResultSet = hybridParentsStatement.executeQuery("SELECT * FROM hybrid_inbred_pairs ORDER BY hybrid_id");
        while(hybridParentsResultSet.next()) {
            int hybridIndex = hybridParentsResultSet.getInt("hybrid_id");
            
            Integer[] hybridInbredParentIndices = new Integer[2];
            hybridInbredParentIndices[0] = hybridParentsResultSet.getInt("parent1_id");
            hybridInbredParentIndices[1] = hybridParentsResultSet.getInt("parent2_id");
            
            hybridInbredParentIndicesHashMap.put(hybridIndex, hybridInbredParentIndices);
        }
        hybridParentsResultSet.close();
        hybridParentsStatement.close();
        databaseConnection.close();
    }
    
    public void performCorrelation(HybridCharacteristic hybridCharacteristic, HashMap<Integer,HashMap<Integer,String>>[] inbredSingleNucleotidePolymorphismSequenceHashMap) throws SQLException {
        if(inbredSingleNucleotidePolymorphismSequenceHashMap[0].isEmpty() && inbredSingleNucleotidePolymorphismSequenceHashMap[0].isEmpty()) {
            System.out.println("no associated SNPs available");
            
            return;
        }
      
        HashMap<Integer,Double> hybridCharacteristicValueHashMap = getHybridCharacteristicValues(hybridCharacteristic);
        
        HashMap<Integer,Double> hybridBinaryDistanceHashMap = new HashMap();

        HashMap<Integer,Double>[] binaryDistanceHashMap = new HashMap[2];
        binaryDistanceHashMap[0] = new HashMap();
        binaryDistanceHashMap[1] = new HashMap();
        
        for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
            binaryDistanceHashMap[0].put(hybridIndex, 0.0);
            binaryDistanceHashMap[1].put(hybridIndex, 0.0);
        }

        for(int associationTypeIndex = 0; associationTypeIndex < 2; associationTypeIndex++) {
            for(int markerIndex : inbredSingleNucleotidePolymorphismSequenceHashMap[associationTypeIndex].keySet()) {
                HashMap<Integer,Boolean> hybridParentDifferentialMarkerSingleNucleotidePolymorphismHashMap = getHybridInbredParentDifferingSingleNucleotidePolymorphism(markerIndex);
                
                for(int hybridIndex : hybridInbredParentIndicesHashMap.keySet()) {
                    if(hybridParentDifferentialMarkerSingleNucleotidePolymorphismHashMap.get(hybridIndex)) {
                        binaryDistanceHashMap[associationTypeIndex].put(hybridIndex, binaryDistanceHashMap[associationTypeIndex].get(hybridIndex) + 1.0);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("correlation: ");
        
        for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
            hybridBinaryDistanceHashMap.put(hybridIndex, (Math.sqrt(binaryDistanceHashMap[0].get(hybridIndex) / (double) inbredSingleNucleotidePolymorphismSequenceHashMap[0].size()) * ((double) inbredSingleNucleotidePolymorphismSequenceHashMap[0].size() / ((double) inbredSingleNucleotidePolymorphismSequenceHashMap[0].size() + (double) inbredSingleNucleotidePolymorphismSequenceHashMap[1].size()))) + ((1.0 - Math.sqrt(binaryDistanceHashMap[1].get(hybridIndex) / (double) inbredSingleNucleotidePolymorphismSequenceHashMap[1].size())) * ((double) inbredSingleNucleotidePolymorphismSequenceHashMap[1].size() / ((double) inbredSingleNucleotidePolymorphismSequenceHashMap[0].size() + (double) inbredSingleNucleotidePolymorphismSequenceHashMap[1].size()))));

            System.out.println(hybridIndex + ";"  + hybridBinaryDistanceHashMap.get(hybridIndex) + ";" + Math.sqrt(binaryDistanceHashMap[0].get(hybridIndex) + binaryDistanceHashMap[1].get(hybridIndex)) + ";" + hybridCharacteristicValueHashMap.get(hybridIndex) + ";hi:;" + binaryDistanceHashMap[0].get(hybridIndex) + ";lo;" + binaryDistanceHashMap[1].get(hybridIndex));
        }
        
        double[] hybridBinaryDistance = new double[hybridCharacteristicValueHashMap.size()];
        double[] hybridCharacteristicValues = new double[hybridCharacteristicValueHashMap.size()];

        int arrayIndex = 0;
        for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
            hybridBinaryDistance[arrayIndex] = hybridBinaryDistanceHashMap.get(hybridIndex);
            hybridCharacteristicValues[arrayIndex] = hybridCharacteristicValueHashMap.get(hybridIndex);
            
            arrayIndex++;
        }
        
        PearsonsCorrelation binaryDistanceHybridCharacteristicCorrelation = new PearsonsCorrelation();
        double binaryDistanceCorrelationCoefficient = binaryDistanceHybridCharacteristicCorrelation.correlation(hybridBinaryDistance, hybridCharacteristicValues);

        System.out.print("pos" + inbredSingleNucleotidePolymorphismSequenceHashMap[0].size() + ";neg" + inbredSingleNucleotidePolymorphismSequenceHashMap[1].size() + ";" + binaryDistanceCorrelationCoefficient);
    }
    
    public void testSequences(DatabaseLoginData databaseLoginData, HybridCharacteristic hybridCharacteristic, double alphaError) throws SQLException {
        DatabaseConnection inbredElementExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredElementExpressionStatement = inbredElementExpressionDatabaseConnection.getConnection().createStatement();
        
        HashMap<Integer,Double> midParentalHeterosisAssociatedSequencesHashMap = getAssociatedSingleNucleotidePolymorphisms(hybridCharacteristic, alphaError);

        HashMap<Integer,HashMap<Integer,String>>[] inbredSingleNucleotidePolymorphismHashMap = new HashMap[2];
        inbredSingleNucleotidePolymorphismHashMap[0] = new HashMap(); // positive association
        inbredSingleNucleotidePolymorphismHashMap[1] = new HashMap(); // negative association
        for(int inbredElementIndex : midParentalHeterosisAssociatedSequencesHashMap.keySet()) {
            int associationTypeIndex = 0;
            if(midParentalHeterosisAssociatedSequencesHashMap.get(inbredElementIndex) < 0) {
                associationTypeIndex = 1;
            }
            
            inbredSingleNucleotidePolymorphismHashMap[associationTypeIndex].put(inbredElementIndex, new HashMap());
            
            HashMap<Integer, String> inbredSingleNucleotidePolymorphismSequenceHashMap = new HashMap();

            String previousSingleNucleotidePolymorphismSequence = null;
            boolean polymorphismPopulationFlag = false;
            for(int inbredIndex : inbredTitleHashMap.keySet()) {
                String singleNucleotidePolymorphismSequence = inbredParentNucleotideHashMap.get(inbredIndex);

                if(singleNucleotidePolymorphismSequence.equals("NA") || singleNucleotidePolymorphismSequence.equals("XX")) {
                    polymorphismPopulationFlag = false;
                    
                    break;
                }

                if(previousSingleNucleotidePolymorphismSequence != null) {
                    if(!singleNucleotidePolymorphismSequence.equals(previousSingleNucleotidePolymorphismSequence)) {
                       polymorphismPopulationFlag = true;
                    }
                }

                inbredSingleNucleotidePolymorphismSequenceHashMap.put(inbredIndex, singleNucleotidePolymorphismSequence);
            }

            if(!polymorphismPopulationFlag) {
                continue;
            }

            inbredSingleNucleotidePolymorphismHashMap[associationTypeIndex].put(inbredElementIndex, markerInbredParentNucleotideHashMap.get(inbredElementIndex));
        }

        this.performCorrelation(hybridCharacteristic, inbredSingleNucleotidePolymorphismHashMap);

        System.out.println();
        System.out.println("sequence ids: ");
        for(int inbredElementIndex : midParentalHeterosisAssociatedSequencesHashMap.keySet()) {
            if(midParentalHeterosisAssociatedSequencesHashMap.get(inbredElementIndex) > 0) {
                System.out.println(inbredElementIndex + "\tpositive");
            }
            else {
                System.out.println(inbredElementIndex + "\tnegative");
            }
        }

        inbredElementExpressionStatement.close();
        inbredElementExpressionDatabaseConnection.close();
    }
            
    public static void main(String[] args) {
        HybridCharacteristic traitCharacteristic = HybridCharacteristic.HYBRID_PERFORMANCE;
            
        double alphaError = 0.05;
        
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("-characteristic {MPH|BPH|HP}");
            System.out.println("-fdrLimit <value>");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("characteristic")) {
                        if(argumentValue.equals("BPH")) {
                            System.out.println("best parent heterosis");
                            traitCharacteristic = HybridCharacteristic.BEST_PARENT_HETEROSIS;
                        }
                        if(argumentValue.equals("MPH")) {
                            System.out.println("mid parent heterosis");
                            traitCharacteristic = HybridCharacteristic.MID_PARENT_HETEROSIS;
                        }
                        else if(argumentValue.equals("HP")) {
                            System.out.println("hybrid performance");
                            traitCharacteristic = HybridCharacteristic.HYBRID_PERFORMANCE;
                        }
                    }
                    else if(argumentTitle.equals("fdrLimit")) {
                        alphaError = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
        }  
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            HybridCharacteristicSingleNucleotidePolymorphismAssociationDatabase hybridCharacteristicSequenceAssociationDatabase = new HybridCharacteristicSingleNucleotidePolymorphismAssociationDatabase(databaseLoginData, MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG);

            hybridCharacteristicSequenceAssociationDatabase.testSequences(databaseLoginData, traitCharacteristic, alphaError);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private HashMap<Integer,Integer[]> hybridInbredParentIndicesHashMap;
    
    private HashMap<Integer,String> inbredTitleHashMap;
    private HashMap<Integer,String> inbredParentNucleotideHashMap;
    private final HashMap<Integer, HashMap<Integer,String>> markerInbredParentNucleotideHashMap;
    
    private final DatabaseLoginData databaseLoginData;
    
    private final MultipleTestingCorrectionMethod multipleTestingCorrectionMethod;
}