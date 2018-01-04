package smallrnasizedistributionbootstrap;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class SmallRNASizeDistributionBootstrap {
    public SmallRNASizeDistributionBootstrap(DatabaseLoginData databaseLoginData, String sequenceIndexFileName, double minimalExpressionThreshold) throws SQLException {
        this.databaseLoginData = databaseLoginData;
        
        this.getSequenceLibraryTitles();
        
        sequenceFileName = sequenceIndexFileName;
        
        sequenceLibraryPresenceHashMap = this.getSequenceLibraryPresenceData(minimalExpressionThreshold);
        sequenceLengthHashMap = this.getSequenceLengthData(sequenceLibraryPresenceHashMap.keySet());
    }
    
    public void bootstrapAnalysis(int minSequenceLength, int maxSequenceLength, int bootstrapNumber) throws IOException {
        ArrayList<Integer> referenceSequenceSubsetIndexArrayList = this.getSequenceSubsetIndices(sequenceFileName);
        
        int[][][] sequenceLengthSubsetCount = new int[bootstrapNumber + 1][sequenceLibraryTitles.length][maxSequenceLength - minSequenceLength + 1];
        
        for(int sequenceIndex : referenceSequenceSubsetIndexArrayList) {
            for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
                if(sequenceLibraryPresenceHashMap.get(sequenceIndex)[libraryIndex]) {
                    sequenceLengthSubsetCount[0][libraryIndex][sequenceLengthHashMap.get(sequenceIndex) - minSequenceLength]++;
                }
            }
        }
        
        Random random = new Random();
        
        int[] sequenceIndices = new int[sequenceLengthHashMap.size()];
        int arrayIndex = 0;
        for(int sequenceIndex : sequenceLengthHashMap.keySet()) {
            sequenceIndices[arrayIndex] = sequenceIndex;
        }
        
        for(int bootstrapIndex = 0; bootstrapIndex < bootstrapNumber; bootstrapIndex++) {
            ArrayList<Integer> bootstrapSequenceIndexArrayList = new ArrayList();
            
            do {
                int randomIndex = random.nextInt(sequenceIndices.length);
                
                int sequenceLength = sequenceLengthHashMap.get(sequenceIndices[randomIndex]);
                if(!bootstrapSequenceIndexArrayList.contains(sequenceIndices[randomIndex]) && (sequenceLength >= minSequenceLength) && (sequenceLength <= maxSequenceLength)) {
                    bootstrapSequenceIndexArrayList.add(sequenceIndices[randomIndex]);
                }
            } while(bootstrapSequenceIndexArrayList.size() < referenceSequenceSubsetIndexArrayList.size());
            
            for(int sequenceIndex : bootstrapSequenceIndexArrayList) {
                for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
                    if(sequenceLibraryPresenceHashMap.get(sequenceIndex)[libraryIndex]) {
                        sequenceLengthSubsetCount[bootstrapIndex + 1][libraryIndex][sequenceLengthHashMap.get(sequenceIndex) - minSequenceLength]++;
                    }
                }
            }
        }
        
        System.out.println("reference set distribution:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();
                
        for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
            System.out.print(sequenceLibraryTitles[libraryIndex]);
           
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
                System.out.print("\t" + sequenceLengthSubsetCount[0][libraryIndex][sequenceLengthIndex]);
            }
            System.out.println();
        }
        System.out.println();

        double[][] depletionProbability = new double[sequenceLibraryTitles.length][maxSequenceLength - minSequenceLength + 1];
        double[][] enrichmentProbability = new double[sequenceLibraryTitles.length][maxSequenceLength - minSequenceLength + 1];
        
        System.out.println("bootstrap set distribution:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();

        double[][] bootstrapSequenceSetSize = new double[bootstrapNumber][sequenceLibraryTitles.length];
        for(int bootstrapIndex = 0; bootstrapIndex < (bootstrapNumber + 1); bootstrapIndex++) {
            for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
                for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
                    bootstrapSequenceSetSize[bootstrapIndex][libraryIndex] += sequenceLengthSubsetCount[0][libraryIndex][sequenceLengthIndex];
                }
            }
        }
        
        for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
            System.out.print(sequenceLibraryTitles[libraryIndex]);
            
            for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
                double bootstrapDistribution = 0;
                
                for(int bootstrapIndex = 1; bootstrapIndex < (bootstrapNumber + 1); bootstrapIndex++) {
                    if((sequenceLengthSubsetCount[bootstrapIndex][libraryIndex][sequenceLengthIndex] / bootstrapSequenceSetSize[bootstrapIndex][libraryIndex]) <= (sequenceLengthSubsetCount[0][libraryIndex][sequenceLengthIndex] / bootstrapSequenceSetSize[0][libraryIndex])) {
                        enrichmentProbability[libraryIndex][sequenceLengthIndex] += (1.0 / bootstrapNumber);
                    }
                    else if((sequenceLengthSubsetCount[bootstrapIndex][libraryIndex][sequenceLengthIndex] / bootstrapSequenceSetSize[bootstrapIndex][libraryIndex]) >= (sequenceLengthSubsetCount[0][libraryIndex][sequenceLengthIndex] / bootstrapSequenceSetSize[0][libraryIndex])) {
                        depletionProbability[libraryIndex][sequenceLengthIndex] += (1.0 / bootstrapNumber);
                    }
                    
                    bootstrapDistribution += ((double) sequenceLengthSubsetCount[bootstrapIndex][libraryIndex][sequenceLengthIndex] / (double) bootstrapIndex);
                }
                
                System.out.print("\t" + bootstrapDistribution);
            }
            System.out.println();
        }
        System.out.println();
            
        System.out.println("enrichment probability:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();

        for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
            System.out.print(sequenceLibraryTitles[libraryIndex]);

            for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
                System.out.print("\t" + enrichmentProbability[libraryIndex][sequenceLengthIndex]);
            }
            System.out.println();
        }
        System.out.println();
            
        System.out.println("depletion probability:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();

        for(int libraryIndex = 0; libraryIndex < sequenceLibraryTitles.length; libraryIndex++) {
            System.out.print(sequenceLibraryTitles[libraryIndex]);

            for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
                System.out.print("\t" + depletionProbability[libraryIndex][sequenceLengthIndex]);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public void combinedBootstrapAnalysis(int minSequenceLength, int maxSequenceLength, int bootstrapNumber) throws IOException {
        ArrayList<Integer> referenceSequenceSubsetIndexArrayList = this.getSequenceSubsetIndices(sequenceFileName);
        
        ArrayList<Integer> bootstrapSequenceSetArrayList = referenceSequenceSubsetIndexArrayList;
        
        Random random = new Random();
        
        int[] sequenceIndices = new int[sequenceLengthHashMap.size()];
        int arrayIndex = 0;
        for(int sequenceIndex : sequenceLengthHashMap.keySet()) {
            sequenceIndices[arrayIndex] = sequenceIndex;
            
            arrayIndex++;
        }
        
        int[][] sequenceLengthSubsetCount = new int[bootstrapNumber + 1][maxSequenceLength - minSequenceLength + 1];

        for(int bootstrapIndex = 0; bootstrapIndex < (bootstrapNumber + 1); bootstrapIndex++) {
            if(bootstrapIndex > 0) {
                bootstrapSequenceSetArrayList = new ArrayList();

                do {
                    int randomIndex = random.nextInt(sequenceIndices.length);

                    int sequenceLength = sequenceLengthHashMap.get(sequenceIndices[randomIndex]);

                    if((sequenceLength >= minSequenceLength) && (sequenceLength <= maxSequenceLength)) {
                        bootstrapSequenceSetArrayList.add(sequenceIndices[randomIndex]);
                    }
                } while(bootstrapSequenceSetArrayList.size() < referenceSequenceSubsetIndexArrayList.size());
            }
            
            for(int sequenceIndex : bootstrapSequenceSetArrayList) {
                sequenceLengthSubsetCount[bootstrapIndex][sequenceLengthHashMap.get(sequenceIndex) - minSequenceLength]++;
            }
        }
        
        System.out.println("reference set distribution:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();
                
        System.out.print("reference");
           
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + sequenceLengthSubsetCount[0][sequenceLengthIndex]);
        }
        System.out.println();

        double[] depletionProbability = new double[maxSequenceLength - minSequenceLength + 1];
        double[] enrichmentProbability = new double[maxSequenceLength - minSequenceLength + 1];
        
        System.out.print("bootstrap");

        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            double bootstrapDistribution = 0;

            for(int bootstrapIndex = 1; bootstrapIndex < (bootstrapNumber + 1); bootstrapIndex++) {
                if(sequenceLengthSubsetCount[bootstrapIndex][sequenceLengthIndex] > sequenceLengthSubsetCount[0][sequenceLengthIndex]) {
                    enrichmentProbability[sequenceLengthIndex] += (1.0 / bootstrapNumber);
                }
                else if(sequenceLengthSubsetCount[bootstrapIndex][sequenceLengthIndex] < sequenceLengthSubsetCount[0][sequenceLengthIndex]) {
                    depletionProbability[sequenceLengthIndex] += (1.0 / bootstrapNumber);
                }

                bootstrapDistribution += ((double) sequenceLengthSubsetCount[bootstrapIndex][sequenceLengthIndex] / (double) bootstrapNumber);
            }

            System.out.print("\t" + bootstrapDistribution);
        }
        System.out.println();
            
        System.out.println("enrichment probability:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();

        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + enrichmentProbability[sequenceLengthIndex]);
        }
        System.out.println();
            
        System.out.println("depletion probability:");
        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + (minSequenceLength + sequenceLengthIndex));
        }
        System.out.println();

        for(int sequenceLengthIndex = 0; sequenceLengthIndex < (maxSequenceLength - minSequenceLength + 1); sequenceLengthIndex++) {
            System.out.print("\t" + depletionProbability[sequenceLengthIndex]);
        }
        System.out.println();
    }
    
    private HashMap<Integer,Boolean[]> getSequenceLibraryPresenceData(double minimalExpressionThreshold) throws SQLException {
        HashMap<Integer,Boolean[]> sequencePresenceHashMap = new HashMap();

        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_library_expression");
        
        while(sequenceDataResultSet.next()) {
            int sequenceIndex = sequenceDataResultSet.getInt("sequence_id");
            
            boolean sequenceLibrarySubsetPresence = false;

            Boolean[] sequenceLibraryPresence = new Boolean[sequenceLibraryTitles.length];
            for(int libraryArrayIndex = 0; libraryArrayIndex < sequenceLibraryTitles.length; libraryArrayIndex++) {
                sequenceLibraryPresence[libraryArrayIndex] = ((sequenceDataResultSet.getDouble(sequenceLibraryTitles[libraryArrayIndex]) >= minimalExpressionThreshold) ? true : false);

                sequenceLibrarySubsetPresence = true;
            }

            if(sequenceLibrarySubsetPresence) {
                sequencePresenceHashMap.put(sequenceIndex, sequenceLibraryPresence);
            }
        }
        
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
        
        return sequencePresenceHashMap;
    }
    
    private HashMap<Integer,Integer> getSequenceLengthData(Set<Integer> sequenceIndexSet) throws SQLException {
        HashMap<Integer,Integer> sequenceLengthHashMap = new HashMap();
        
        DatabaseConnection sequenceDataDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceDataStatement = sequenceDataDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet sequenceDataResultSet = sequenceDataStatement.executeQuery("SELECT * FROM srna_sequence");
        
        while(sequenceDataResultSet.next()) {
            int sequenceIndex = sequenceDataResultSet.getInt("sequence_id");
            
            if(sequenceIndexSet.contains(sequenceIndex)) {
                int sequenceLength = sequenceDataResultSet.getInt("length");
                
                sequenceLengthHashMap.put(sequenceIndex, sequenceLength);
            }
        }
        sequenceDataResultSet.close();
        sequenceDataStatement.close();
        sequenceDataDatabaseConnection.close();
        
        return sequenceLengthHashMap;
    }
    
    public final String[] getSequenceLibraryTitles() throws SQLException {
        ArrayList<String> sequenceLibraryTitleList = new ArrayList();

        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement sequenceLibraryStatement = databaseConnection.getConnection().createStatement();
        ResultSet libraryResultSet = sequenceLibraryStatement.executeQuery("SELECT library_title FROM srna_libraries WHERE library_id<=20");
        while(libraryResultSet.next()) {
            sequenceLibraryTitleList.add(libraryResultSet.getString("library_title"));
        }
        libraryResultSet.close();
        sequenceLibraryStatement.close();
        databaseConnection.close();

        sequenceLibraryTitles = new String[sequenceLibraryTitleList.size()];
        sequenceLibraryTitleList.toArray(sequenceLibraryTitles);

        return sequenceLibraryTitles;
    }
    
    public ArrayList<Integer> getSequenceSubsetIndices(String fileName) throws IOException {
        ArrayList<Integer> sequenceSubsetIndexArrayList = new ArrayList();
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String fileLine;
        while((fileLine = bufferedReader.readLine()) != null) {
            if(!fileLine.isEmpty()) {
                sequenceSubsetIndexArrayList.add(Integer.valueOf(fileLine));
            }
        }
        bufferedReader.close();
        
        return sequenceSubsetIndexArrayList;
    }
    
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String sequenceFileName = null;
        
        int bootstrapNumber = 1000;
        
        if(args.length == 0) {
            System.out.println("Usage: ");
            System.out.println("-bootstrap <number of bootstrap runs>");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.out.println("-filename <filename>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("bootstrap")) {
                        bootstrapNumber = Integer.valueOf(argumentValue);
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    if(argumentTitle.equals("filename")) {
                        sequenceFileName = argumentValue;
                    }
                }
            }
        }
        
        if((databaseUser == null) || (databasePassword == null) || (sequenceFileName == null)) {
            System.out.println("Usage: ");
            System.out.println("-bootstrap <number of bootstrap runs>");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.out.println("-filename <filename>");
            System.exit(1);
        }
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
           
            SmallRNASizeDistributionBootstrap smallRNASizeDistributionBootstrap = new SmallRNASizeDistributionBootstrap(databaseLoginData, sequenceFileName, 0.5);
            smallRNASizeDistributionBootstrap.combinedBootstrapAnalysis(18, 28, bootstrapNumber);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    

    private final DatabaseLoginData databaseLoginData;

    private final HashMap<Integer,Boolean[]> sequenceLibraryPresenceHashMap;

    private final HashMap<Integer,Integer> sequenceLengthHashMap;
    
    private final String sequenceFileName;
    private String[] sequenceLibraryTitles;
}
