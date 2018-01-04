package hybridcharacteristiccorrelationdatabase;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Structure handles breeding factorial data
 * - translates inbred line titles to indices in both directions
 * - provides relations of inbred line and resulting hybrid in both directions
 * - gives access to trait values of hybrids
 * 
 * @author Felix Seifert
 */
public class BreedingFactorial {
    
    /**
     * generates BreedingFactorialData object of inbred lines and trait values of hybrids
     * 
     * @param databaseLoginData object providing MySQL database login data
     * @param germplasmInbredLineTitles 2-dimensional String array, first dimension separates heterotic groups, second contains inbred line titles
     * @param hybridTraitCharacteristic hybrid trait characteristic to be analyzed
     * @throws SQLException 
     */
    public BreedingFactorial(DatabaseLoginData databaseLoginData, String[][] germplasmInbredLineTitles, HybridTraitCharacteristic hybridTraitCharacteristic) throws SQLException {
        this.databaseLoginData = databaseLoginData;
        this.hybridTraitCharacteristic = hybridTraitCharacteristic;
        
        this.generateInbredLineHash(germplasmInbredLineTitles);
        this.generateHybridHash(germplasmInbredLineTitles);
        this.loadHybridCharacteristicData();
        
        germplasmInbredLineEstimationSetFlagHashMap = new HashMap[2];
        for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
            germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex] = new HashMap();

            for(String germplasmInbredLineTitle : germplasmInbredLineTitles[germplasmIndex]) {
                germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].put(getInbredIndex(germplasmInbredLineTitle), Boolean.TRUE);
            }
        }
    }

    /**
     * generates a BreedingFactorialData object with defined estimation set
     * 
     * @param databaseLoginData object providing MySQL database login data
     * @param germplasmInbredLineTitles 2-dimensional String array, first dimension separates heterotic groups, second contains inbred line titles
     * @param germplasmInbredLineEstimationSetFlag 2-dimensional String array, first dimension separates heterotic groups, second boolean flags for lines in estimation set
     * @param hybridCharacteristic hybrid characteristic to be analyzed
     * @throws SQLException 
    */
    public BreedingFactorial(DatabaseLoginData databaseLoginData, String[][] germplasmInbredLineTitles, boolean[][] germplasmInbredLineEstimationSetFlag, HybridTraitCharacteristic hybridCharacteristic) throws SQLException {
        this(databaseLoginData, germplasmInbredLineTitles, hybridCharacteristic);
        
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

    /**
     * change hybrid trait characteristic and loads values
     * 
     * @param hybridTraitCharacteristic change HybridTraitCharacteristic value
     * @throws SQLException 
     */
    public void changeHybridCharacteristic(HybridTraitCharacteristic hybridTraitCharacteristic) throws SQLException {
        this.hybridTraitCharacteristic = hybridTraitCharacteristic;
        
        loadHybridCharacteristicData();
    }
    
    /**
     * loads line trait values of previously specified HybridCharacteristic
     * 
     * @throws SQLException 
     */
    private void loadHybridCharacteristicData() throws SQLException {
        hybridTraitCharacteristicValueHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = null;
        Statement hybridDataStatement = null;
        
        try {
            /* translate HybridCharacteristic to database table */
            
            String hybridCharacteristicSqlColumn;
            switch(hybridTraitCharacteristic) {
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

            /* establish database connection and retrieve hybrid trait data */
            databaseConnection = new DatabaseConnection(databaseLoginData);
            hybridDataStatement = databaseConnection.getConnection().createStatement();
            
            for(int hybridIndex : hybridParentIndicesHashMap.keySet()) {
                ResultSet hybridCharacteristicResultSet;
                hybridCharacteristicResultSet = hybridDataStatement.executeQuery("SELECT " + hybridCharacteristicSqlColumn + " FROM hybrid_field_data WHERE hybrid_id=" + hybridIndex);
                if(hybridCharacteristicResultSet.next()) {
                    hybridTraitCharacteristicValueHashMap.put(hybridIndex, hybridCharacteristicResultSet.getDouble(hybridCharacteristicSqlColumn));
                }
                else {
                    hybridTraitCharacteristicValueHashMap.put(hybridIndex, null);
                }

                hybridCharacteristicResultSet.close();
            }
        }
        finally {
            /* terminate database connection */
            if(databaseConnection != null) {
                databaseConnection.close();

                if(hybridDataStatement != null) {
                    hybridDataStatement.close();
                }
            }
        }
    }
    
    /**
     * generates HashMap linking inbred line indices to resulting hybrid and vice versa
     * 
     * @param germplasmInbredLineTitles 2-dimensional String array, first dimension separates heterotic groups, second contains inbred line titles
     * @throws SQLException 
     */
    private void generateHybridHash(String[][] germplasmInbredLineTitles) throws SQLException {
        hybridParentIndicesHashMap = new HashMap();
        inbredParentIndicesToHybridIndexHashMap = new HashMap();
        
        DatabaseConnection databaseConnection = null;
        Statement hybridDataStatement = null;
        
        try {
            /* establish database connection and retrieve line indices as well as hybrid indices of inbred pairs */
            databaseConnection = new DatabaseConnection(databaseLoginData);
            hybridDataStatement = databaseConnection.getConnection().createStatement();
            
            for(String germplasm0InbredTitle : germplasmInbredLineTitles[0]) {
                int germplasm0InbredIndex = inbredLineTitleToIndexHashMap.get(germplasm0InbredTitle);
                
                for(String germplasm1InbredTitle : germplasmInbredLineTitles[1]) {
                    int germplasm1InbredIndex = inbredLineTitleToIndexHashMap.get(germplasm1InbredTitle);
                    
                    if(!inbredParentIndicesToHybridIndexHashMap.containsKey(germplasm0InbredIndex)) {
                        inbredParentIndicesToHybridIndexHashMap.put(germplasm0InbredIndex, new HashMap());
                    }
                    
                    ResultSet hybridDataResultSet;
                    hybridDataResultSet = hybridDataStatement.executeQuery("SELECT hybrid_id FROM hybrid_inbred_pairs WHERE parent1_id=" + germplasm0InbredIndex + " AND parent2_id=" + germplasm1InbredIndex);
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
            /* terminate database connection */
            if(databaseConnection != null) {
                databaseConnection.close();
    
                if(hybridDataStatement != null) {
                    hybridDataStatement.close();
                }
            }
        }
    }
    
    /**
     * generates HashMap linking inbred line indices to inbred titles and vice versa
     * 
     * @param germplasmInbredLineTitles 2-dimensional String array, first dimension separates heterotic groups, second contains inbred line titles
     * @throws SQLException 
     */
    private void generateInbredLineHash(String[][] germplasmInbredLineTitles) throws SQLException {
        germplasmInbredLineTitleHashMap = new HashMap[2];
        germplasmInbredLineTitleHashMap[0] = new HashMap();
        germplasmInbredLineTitleHashMap[1] = new HashMap();
        
        inbredLineTitleToIndexHashMap = new HashMap();
        inbredIndexToTitleHashMap = new HashMap();
                
        DatabaseConnection databaseConnection = null;
        Statement inbredDataStatement = null;
        
        try {
            /* establish database connection and retrieve line indices */
            databaseConnection = new DatabaseConnection(databaseLoginData);
            inbredDataStatement = databaseConnection.getConnection().createStatement();
        
            for(int germplasmIndex = 0; germplasmIndex < 2; germplasmIndex++) {
                for(String germplasmInbredLineTitle : germplasmInbredLineTitles[germplasmIndex]) {
                    ResultSet inbredDataResultSet;
                    inbredDataResultSet = inbredDataStatement.executeQuery("SELECT library_id FROM srna_libraries WHERE library_title=\"" + germplasmInbredLineTitle + "\"");
                    if (inbredDataResultSet.next()) {
                        int libraryIndex = inbredDataResultSet.getInt("library_id");
                        germplasmInbredLineTitleHashMap[germplasmIndex].put(libraryIndex, germplasmInbredLineTitle);
                        inbredIndexToTitleHashMap.put(libraryIndex, germplasmInbredLineTitle);
                        inbredLineTitleToIndexHashMap.put(germplasmInbredLineTitle, libraryIndex);
                    }
                    inbredDataResultSet.close();
                }
            }
        }
        finally {
            /* terminate database connection */
            if(databaseConnection != null) {
                databaseConnection.close();

                if(inbredDataStatement != null) {
                    inbredDataStatement.close();        
                }
            }
        }
    }
    
    /**
     * obtain hybrid trait value for specified hybrid
     * 
     * @param hybridIndex hybrid index
     * @return trait value for specified hybrid, null if no trait data was defined/hybrid is not available
     */
    public Double getHybridTraitCharacteristicValue(int hybridIndex) {
        if(hybridTraitCharacteristicValueHashMap.containsKey(hybridIndex)) {
            return hybridTraitCharacteristicValueHashMap.get(hybridIndex);
        }
        
        return null;
    }
    
    /**
     * obtain hybrid trait characteristic values for all hybrids in a HashMap
     * 
     * @return HashMap of hybrid trait characteristic values indexed by hybrid indices
     */
    public HashMap<Integer,Double> getHybridCharacteristicValues() {
        return hybridTraitCharacteristicValueHashMap;
    }

    /**
     * obtain the number of hybrids in the breeding factorial
     * 
     * @return number of hybrids
     */
    public int getHybridCount() {
        return hybridParentIndicesHashMap.size();
    }
    
    /**
     * obtain the hybrid index for a given pair of inbred indices
     * 
     * @param parent1Index inbred index of parent line 1
     * @param parent2Index inbred index of parent line 2
     * @return hybrid index of hybrid with specified parental inbred lines, -1 if hybrid is not defined for the provided parental inbred lines
     */
    public int getHybridIndex(int parent1Index, int parent2Index) {
        if(inbredParentIndicesToHybridIndexHashMap.containsKey(parent1Index) && inbredParentIndicesToHybridIndexHashMap.get(parent1Index).containsKey(parent2Index)) {
            return inbredParentIndicesToHybridIndexHashMap.get(parent1Index).get(parent2Index);
        }
        
        return -1;
    }

    /**
     * obtain all hybrid indices
     * 
     * @return array of hybrid indices
     */
    public int[] getHybridIndices() {
        int[] hybridIndices = new int[hybridParentIndicesHashMap.size()];
        int arrayIndex = 0;
        for(int inbredIndex : hybridParentIndicesHashMap.keySet()) {
            hybridIndices[arrayIndex++] = inbredIndex;
        }
        
        return hybridIndices;
    }

    /**
     * obtain indices of parental inbred lines for specified hybrid
     * 
     * @param hybridIndex hybrid index of which parents should be returned
     * @return array of inbred line indices for the specified hybrid
     */
    public int[] getHybridParentIndices(int hybridIndex) {
        if(hybridParentIndicesHashMap.containsKey(hybridIndex)) {
            int[] inbredIndices = new int[2];
            inbredIndices[0] = hybridParentIndicesHashMap.get(hybridIndex)[0];
            inbredIndices[1] = hybridParentIndicesHashMap.get(hybridIndex)[1];
            
            return inbredIndices;
        }
        
        return null;
    }
        
    /**
     * get current chosen hybrid trait characteristic
     * 
     * @return current hybrid trait characteristic
     */
    public HybridTraitCharacteristic getHybridTraitCharacteristic() {
        return hybridTraitCharacteristic;
    }
    
    /**
     * obtain the number of inbred lines in the breeding factorial
     * 
     * @return number of inbred lines in the breeding factorial
     */
    public int getInbredCount() {
        return inbredIndexToTitleHashMap.size();
    }

    /**
     * get germplasm index for inbred line
     * 
     * @return number of inbred lines in the breeding factorial
     */
    public int getInbredGermplasm(String inbredTitle) {
        if(germplasmInbredLineTitleHashMap[0].get(this.getInbredIndex(inbredTitle)) != null) {
            return 0;
        }
        if(germplasmInbredLineTitleHashMap[1].get(this.getInbredIndex(inbredTitle)) != null) {
            return 1;
        }
        
        return -1;
    }    
    
    /**
     * obtain the inbred index from inbred line title
     * 
     * @param inbredTitle inbred title of which the index should be returned
     * @return inbred index of specified inbred, -1 if inbred line is not defined
     */
    public final int getInbredIndex(String inbredTitle) {
        if(inbredLineTitleToIndexHashMap.containsKey(inbredTitle)) {
            return inbredLineTitleToIndexHashMap.get(inbredTitle);
        }
        
        return -1;
    }
    
    /**
     * obtain an array of all inbred line indices
     * 
     * @return array of inbred indices
     */
    public int[] getInbredIndices() {
        int[] inbredIndices = new int[inbredIndexToTitleHashMap.size()];
        int arrayIndex = 0;
        for(int inbredIndex : inbredIndexToTitleHashMap.keySet()) {
            inbredIndices[arrayIndex++] = inbredIndex;
        }
        
        return inbredIndices;
    }
    
    /**
     * obtain title of inbred line specified by index
     * 
     * @param inbredIndex index of inbred line
     * @return inbred titel, null if inbred line is not defined
     */
    public String getInbredTitle(int inbredIndex) {
        if(inbredIndexToTitleHashMap.containsKey(inbredIndex)) {
            return inbredIndexToTitleHashMap.get(inbredIndex);
        }
        
        return null;
    }
    
    /**
     * obtain a HashMap of inbred line titles mapped to inbred indices
     * 
     * @return HashMap of inbred lines mapped to inbred indices
     */
    public HashMap<Integer,String> getInbredTitleHashMap() {
        return inbredIndexToTitleHashMap;
    }

    /**
     * tests if inbred line is selected in estimation set
     * 
     * @param germplasmIndex index of heterotic group/germplasm
     * @param parentIndex inbred line index
     * @return boolean value for selection in estimation set
     */
    public boolean isSelectedInbred(int germplasmIndex, int parentIndex) {
        if(germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].containsKey(parentIndex)) {
            return germplasmInbredLineEstimationSetFlagHashMap[germplasmIndex].get(parentIndex);
        }
        
        return false;
    }

    
    private final DatabaseLoginData databaseLoginData;

    private final HashMap<Integer,Boolean>[] germplasmInbredLineEstimationSetFlagHashMap;
    private HashMap<Integer,Double> hybridTraitCharacteristicValueHashMap;
    private HashMap<Integer,Integer[]> hybridParentIndicesHashMap;
    private HashMap<Integer,HashMap<Integer,Integer>> inbredParentIndicesToHybridIndexHashMap;
    private HashMap<String,Integer> inbredLineTitleToIndexHashMap;
    private HashMap<Integer,String> inbredIndexToTitleHashMap;
    
    private HybridTraitCharacteristic hybridTraitCharacteristic;

    private HashMap<Integer,String>[] germplasmInbredLineTitleHashMap;
}
