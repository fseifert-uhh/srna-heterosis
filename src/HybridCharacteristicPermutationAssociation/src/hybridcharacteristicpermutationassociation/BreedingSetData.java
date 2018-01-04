package hybridcharacteristicpermutationassociation;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class BreedingSetData {
    public BreedingSetData(DatabaseLoginData databaseLoginData, String[][] germplasmInbredLineTitles, HybridCharacteristic hybridCharacteristic, boolean permutationFlag) throws SQLException {
        this.databaseLoginData = databaseLoginData;
        this.hybridCharacteristic = hybridCharacteristic;
        
        this.permutationFlag = permutationFlag;
        
        this.generateInbredLineHash(germplasmInbredLineTitles);
        this.generateHybridHash(germplasmInbredLineTitles);
        this.collectHybridCharacteristicData();
        
        germplasmInbredLineEstimationSetFlagHashMap = new HashMap[2];
        for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
            germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex] = new HashMap();

            for(int inbredIndex = 0; inbredIndex < germplasmInbredLineTitles[germplasmIndex].length; inbredIndex++) {
                germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].put(getInbredIndex(germplasmInbredLineTitles[germplasmIndex][inbredIndex]), Boolean.TRUE);
            }
        }
    }

    public BreedingSetData(DatabaseLoginData databaseLoginData, String[][] germplasmInbredLineTitles, boolean[][] germplasmInbredLineEstimationSetFlag, HybridCharacteristic hybridCharacteristic, boolean permutationFlag) throws SQLException {
        this(databaseLoginData, germplasmInbredLineTitles, hybridCharacteristic, permutationFlag);
        
        for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
            germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex] = new HashMap();

            for(int inbredIndex = 0; inbredIndex < germplasmInbredLineTitles[germplasmIndex].length; inbredIndex++) {
                if(germplasmInbredLineEstimationSetFlag[germplasmIndex][inbredIndex]) {
                    germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].put(getInbredIndex(germplasmInbredLineTitles[germplasmIndex][inbredIndex]), Boolean.TRUE);
                }
                else {
                    germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].put(getInbredIndex(germplasmInbredLineTitles[germplasmIndex][inbredIndex]), Boolean.FALSE);
                }
            }
        }
    }

    private void collectHybridCharacteristicData() throws SQLException {
        hybridCharacteristicValueHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = null;
        Statement hybridDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
            hybridDataStatement = databaseConnection.getConnection().createStatement();
            
            String hybridCharacteristicSqlColumn;
            switch(hybridCharacteristic) {
                case HYBRID_PERFORMANCE:
                    hybridCharacteristicSqlColumn = "hybrid_performance";

                    break;
                case MID_PARENT_HETEROSIS:
                    hybridCharacteristicSqlColumn = "mid_parent_heterosis_r";

                    break;
                case BEST_PARENT_HETEROSIS:
                    hybridCharacteristicSqlColumn = "best_parent_heterosis_r";

                    break;
                default:
                    hybridCharacteristicSqlColumn = "mid_parent_heterosis_r";
            }

            for(int hybridIndex : hybridParentIndicesHashMap.keySet()) {
                ResultSet hybridCharacteristicResultSet = hybridDataStatement.executeQuery("SELECT " + hybridCharacteristicSqlColumn + " FROM hybrid_field_data WHERE hybrid_id=" + hybridIndex);
                if(hybridCharacteristicResultSet.next()) {
                    hybridCharacteristicValueHashMap.put(hybridIndex, hybridCharacteristicResultSet.getDouble(hybridCharacteristicSqlColumn));
                }
                else {
                    hybridCharacteristicValueHashMap.put(hybridIndex, 0.0);
                }

                hybridCharacteristicResultSet.close();
            }
            
            if(permutationFlag) {
                ArrayList<Integer> hybridIndexArrayList = new ArrayList();
                ArrayList<Double> hybridCharacteristicValueArrayList = new ArrayList();

                for(int hybridIndex : hybridCharacteristicValueHashMap.keySet()) {
                    hybridIndexArrayList.add(hybridIndex);
                    hybridCharacteristicValueArrayList.add(hybridCharacteristicValueHashMap.get(hybridIndex));
                }
                
                while(!hybridIndexArrayList.isEmpty()) {
                    int hybridIndex = hybridIndexArrayList.remove((int) (Math.random() * hybridIndexArrayList.size()));
                    double hybridCharacteristicValue = hybridCharacteristicValueArrayList.remove((int) (Math.random() * hybridCharacteristicValueArrayList.size()));
                    
                    hybridCharacteristicValueHashMap.put(hybridIndex, hybridCharacteristicValue);
                }
            }
        }
        finally {
            if(databaseConnection != null) {
                databaseConnection.close();

                if(hybridDataStatement != null) {
                    hybridDataStatement.close();
                }
            }
        }
    }
    
    private void generateHybridHash(String[][] germplasmInbredLineTitles) throws SQLException {
        hybridParentIndicesHashMap = new HashMap();
        inbredParentIndicesToHybridIndexHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = null;
        Statement hybridDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
            hybridDataStatement = databaseConnection.getConnection().createStatement();
            
            for(String germplasm0InbredTitle : germplasmInbredLineTitles[0]) {
                int germplasm0InbredIndex = inbredLineTitleToIndexHashMap.get(germplasm0InbredTitle);
                
                for(String germplasm1InbredTitle : germplasmInbredLineTitles[1]) {
                    int germplasm1InbredIndex = inbredLineTitleToIndexHashMap.get(germplasm1InbredTitle);
                    
                    if(!inbredParentIndicesToHybridIndexHashMap.containsKey(germplasm0InbredIndex)) {
                        inbredParentIndicesToHybridIndexHashMap.put(germplasm0InbredIndex, new HashMap());
                    }
                    
                    ResultSet hybridDataResultSet = hybridDataStatement.executeQuery("SELECT hybrid_id FROM hybrid_inbred_pairs WHERE parent1_id=" + germplasm0InbredIndex + " AND parent2_id=" + germplasm1InbredIndex);
                    if(hybridDataResultSet.next()) {
                        int hybridIndex = hybridDataResultSet.getInt("hybrid_id");
                        
                        Integer[] inbredIndices = {germplasm0InbredIndex, germplasm1InbredIndex};
                        hybridParentIndicesHashMap.put(hybridIndex, inbredIndices);
                        inbredParentIndicesToHybridIndexHashMap.get(germplasm0InbredIndex).put(germplasm1InbredIndex, hybridIndex);
                    }
                    else {
                        inbredParentIndicesToHybridIndexHashMap.get(germplasm0InbredIndex).put(germplasm1InbredIndex, -1);
                    }

                    hybridDataResultSet.close();
                }
            }
        }
        finally {
            if(databaseConnection != null) {
                databaseConnection.close();
    
                if(hybridDataStatement != null) {
                    hybridDataStatement.close();
                }
            }
        }
    }
    
    private void generateInbredLineHash(String[][] germplasmInbredLineTitles) throws SQLException {
        germplasmInbredLineTitleHashMap = new HashMap[2];
        germplasmInbredLineTitleHashMap[0] = new HashMap();
        germplasmInbredLineTitleHashMap[1] = new HashMap();
        
        inbredLineTitleToIndexHashMap = new HashMap();
        inbredIndexToTitleHashMap = new HashMap();
                
        DatabaseConnection databaseConnection = null;
        Statement inbredDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
            inbredDataStatement = databaseConnection.getConnection().createStatement();
        
            for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
                for(int inbredIndex = 0; inbredIndex < germplasmInbredLineTitles[germplasmIndex].length; inbredIndex++) {
                    ResultSet inbredDataResultSet = inbredDataStatement.executeQuery("SELECT library_id FROM srna_libraries WHERE library_title=\"" + germplasmInbredLineTitles[germplasmIndex][inbredIndex] + "\"");
                    if(inbredDataResultSet.next()) {
                        int libraryIndex = inbredDataResultSet.getInt("library_id");
                        
                        germplasmInbredLineTitleHashMap[germplasmIndex].put(libraryIndex, germplasmInbredLineTitles[germplasmIndex][inbredIndex]);
                        inbredIndexToTitleHashMap.put(libraryIndex, germplasmInbredLineTitles[germplasmIndex][inbredIndex]);
                        inbredLineTitleToIndexHashMap.put(germplasmInbredLineTitles[germplasmIndex][inbredIndex], libraryIndex);
                    }

                    inbredDataResultSet.close();
                }
            }
        }
        finally {
            if(databaseConnection != null) {
                databaseConnection.close();

                if(inbredDataStatement != null) {
                    inbredDataStatement.close();        
                }
            }
        }
    }
    
    public HybridCharacteristic getHybridCharacteristic() {
        return hybridCharacteristic;
    }
    
    public double getHybridCharacteristicValue(int hybridIndex) {
        if(hybridCharacteristicValueHashMap.containsKey(hybridIndex)) {
            return hybridCharacteristicValueHashMap.get(hybridIndex);
        }
        
        return 0;
    }
    
    public HashMap<Integer,Double> getHybridCharacteristicValues() {
        return hybridCharacteristicValueHashMap;
    }

    public int getHybridCount() {
        return hybridParentIndicesHashMap.size();
    }
    
    public int getHybridIndex(int parent1Index, int parent2Index) {
        if(inbredParentIndicesToHybridIndexHashMap.containsKey(parent1Index) && inbredParentIndicesToHybridIndexHashMap.get(parent1Index).containsKey(parent2Index)) {
            return inbredParentIndicesToHybridIndexHashMap.get(parent1Index).get(parent2Index);
        }
        
        return -1;
    }

    public int[] getHybridIndices() {
        int[] hybridIndices = new int[hybridParentIndicesHashMap.size()];
        int arrayIndex = 0;
        for(int inbredIndex : hybridParentIndicesHashMap.keySet()) {
            hybridIndices[arrayIndex++] = inbredIndex;
        }
        
        return hybridIndices;
    }

    public int[] getHybridParentIndices(int hybridIndex) {
        if(hybridParentIndicesHashMap.containsKey(hybridIndex)) {
            int[] inbredIndices = new int[2];
            inbredIndices[0] = hybridParentIndicesHashMap.get(hybridIndex)[0];
            inbredIndices[1] = hybridParentIndicesHashMap.get(hybridIndex)[1];
            
            return inbredIndices;
        }
        
        return null;
    }
    
    public int getInbredCount() {
        return inbredIndexToTitleHashMap.size();
    }
    
    public int getInbredIndex(String inbredTitle) {
        if(inbredLineTitleToIndexHashMap.containsKey(inbredTitle)) {
            return inbredLineTitleToIndexHashMap.get(inbredTitle);
        }
        
        return -1;
    }
    
    public int[] getInbredIndices() {
        int[] inbredIndices = new int[inbredIndexToTitleHashMap.size()];
        int arrayIndex = 0;
        for(int inbredIndex : inbredIndexToTitleHashMap.keySet()) {
            inbredIndices[arrayIndex++] = inbredIndex;
        }
        
        return inbredIndices;
    }
    
    public String getInbredTitle(int inbredIndex) {
        if(inbredIndexToTitleHashMap.containsKey(inbredIndex)) {
            return inbredIndexToTitleHashMap.get(inbredIndex);
        }
        
        return null;
    }
    
    public HashMap<Integer,String> getInbredTitleHashMap() {
        return inbredIndexToTitleHashMap;
    }

    public boolean isSelectedInbred(int germplasmIndex, int parentIndex) {
        if(germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].containsKey(parentIndex)) {
            return germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].get(parentIndex);
        }
        
        return false;
    }

    private boolean permutationFlag;
    
    private DatabaseLoginData databaseLoginData;

    private HashMap<Integer,Boolean>[] germplasmInbredLineEstimationSetFlagHashMap;
    private HashMap<Integer,Double> hybridCharacteristicValueHashMap;
    private HashMap<Integer,Integer[]> hybridParentIndicesHashMap;
    private HashMap<Integer,HashMap<Integer,Integer>> inbredParentIndicesToHybridIndexHashMap;
    private HashMap<String,Integer> inbredLineTitleToIndexHashMap;
    private HashMap<Integer,String> inbredIndexToTitleHashMap;
    
    private HybridCharacteristic hybridCharacteristic;

    private HashMap<Integer,String>[] germplasmInbredLineTitleHashMap;
}
