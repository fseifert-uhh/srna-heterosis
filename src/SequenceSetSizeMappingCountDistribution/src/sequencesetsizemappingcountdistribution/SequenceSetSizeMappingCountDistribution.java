package sequencesetsizemappingcountdistribution;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SequenceSetSizeMappingCountDistribution {
    public SequenceSetSizeMappingCountDistribution(String sequenceSetFileName, DatabaseLoginData databaseLoginData) throws IOException, SQLException {
        int minSequenceLength = 18;
        int maxSequenceLength = 28;
        int sequenceCount = 0;

        int[] mappingCountBinLowerBorders = {0, 1, 2, 11, 101};
        
        HashMap<Integer,Integer[]> sequenceLengthDistributionHashMap = new HashMap();

        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        
        if(sequenceSetFileName != null) {
            Statement statement = databaseConnection.getConnection().createStatement();
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(sequenceSetFileName));
            String sequenceIndex = null;
            if((sequenceIndex = bufferedReader.readLine()) != null) {
                ResultSet sequenceResultSet = statement.executeQuery("SELECT srna_sequence.length,srna_mapping_status_refgen4.mapping_count FROM srna_sequence,srna_mapping_status_refgen4 WHERE srna_sequence.sequence_id=" + sequenceIndex + " AND srna_sequence.sequence_id=srna_mapping_status_refgen4.sequence_id");
                if(sequenceResultSet.next()) {
                    sequenceCount++;
                    int sequenceLength = sequenceResultSet.getInt("srna_sequence.length");
                    int mappingCount = sequenceResultSet.getInt("srna_mapping_status_refgen4.mapping_count");

                    if((minSequenceLength == 0) || (minSequenceLength > sequenceLength)) {
                        minSequenceLength = sequenceLength;
                    }
                    if((maxSequenceLength == 0) || (maxSequenceLength < sequenceLength)) {
                        maxSequenceLength = sequenceLength;
                    }

                    int currentMappingCountBin = 0;
                    for(int mappingCountBinLowerBorder : mappingCountBinLowerBorders) {
                        if(mappingCountBinLowerBorder == mappingCountBinLowerBorders[mappingCountBinLowerBorders.length - 1]) {
                            break;
                        }
                        else if(mappingCount > mappingCountBinLowerBorder) {
                            currentMappingCountBin++;
                        }
                        else {
                            break;
                        }
                    }

                    if(sequenceLengthDistributionHashMap.get(sequenceLength) == null) {
                        Integer[] mappingCountBins = new Integer[7];
                        for(int mappingCountBinIndex = 0; mappingCountBinIndex < mappingCountBins.length; mappingCountBinIndex++) {
                            mappingCountBins[mappingCountBinIndex] = 0;
                        }

                        sequenceLengthDistributionHashMap.put(sequenceLength, mappingCountBins);
                    }

                    Integer[] currentMappingCountBins = sequenceLengthDistributionHashMap.get(sequenceLength);
                    currentMappingCountBins[currentMappingCountBin]++;

                    sequenceLengthDistributionHashMap.put(sequenceLength, currentMappingCountBins);
                }
                sequenceResultSet.next();
            }
            bufferedReader.close();
            
            statement.close();
        }
        else {
            Statement statement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(Integer.MIN_VALUE);
            
            ResultSet sequenceResultSet = statement.executeQuery("SELECT srna_sequence.length,srna_mapping_status_refgen4.mapping_count FROM srna_sequence,srna_mapping_status_refgen4 WHERE srna_sequence.length>=18 AND srna_sequence.length<=28 AND srna_sequence.sequence_id=srna_mapping_status_refgen4.sequence_id");
            while(sequenceResultSet.next()) {
                sequenceCount++;
                int sequenceLength = sequenceResultSet.getInt("srna_sequence.length");
                int mappingCount = sequenceResultSet.getInt("srna_mapping_status_refgen4.mapping_count");

                if((minSequenceLength == 0) || (minSequenceLength > sequenceLength)) {
                    minSequenceLength = sequenceLength;
                }
                if((maxSequenceLength == 0) || (maxSequenceLength < sequenceLength)) {
                    maxSequenceLength = sequenceLength;
                }

                int currentMappingCountBin = 0;
                for(int mappingCountBinLowerBorder : mappingCountBinLowerBorders) {
                    if(mappingCountBinLowerBorder == mappingCountBinLowerBorders[mappingCountBinLowerBorders.length - 1]) {
                        break;
                    }
                    else if(mappingCount > mappingCountBinLowerBorder) {
                        currentMappingCountBin++;
                    }
                    else {
                        break;
                    }
                }

                if(sequenceLengthDistributionHashMap.get(sequenceLength) == null) {
                    Integer[] mappingCountBins = new Integer[7];
                    for(int mappingCountBinIndex = 0; mappingCountBinIndex < mappingCountBins.length; mappingCountBinIndex++) {
                        mappingCountBins[mappingCountBinIndex] = 0;
                    }

                    sequenceLengthDistributionHashMap.put(sequenceLength, mappingCountBins);
                }

                Integer[] currentMappingCountBins = sequenceLengthDistributionHashMap.get(sequenceLength);
                currentMappingCountBins[currentMappingCountBin]++;

                sequenceLengthDistributionHashMap.put(sequenceLength, currentMappingCountBins);
            }
            
            statement.close();
        }
        
        
        databaseConnection.close();

        System.out.print("sequence length");
        for(int mappingCountBinLowerBorder : mappingCountBinLowerBorders) {
            System.out.print("\t" + mappingCountBinLowerBorder);
        }
        System.out.println();
        
        int[] sequenceLengthReadCount = new int[mappingCountBinLowerBorders.length + 1];
        for(int sequenceLength = minSequenceLength; sequenceLength <= maxSequenceLength; sequenceLength++) {
            if(sequenceLengthDistributionHashMap.get(sequenceLength) != null) {
                System.out.print(sequenceLength);
                
                for(int mappingCountBinIndex = 0; mappingCountBinIndex < mappingCountBinLowerBorders.length; mappingCountBinIndex++) {
                     System.out.print("\t" + sequenceLengthDistributionHashMap.get(sequenceLength)[mappingCountBinIndex]);
                     
                     sequenceLengthReadCount[mappingCountBinIndex] += sequenceLengthDistributionHashMap.get(sequenceLength)[mappingCountBinIndex];
                }
                
                System.out.println();
            }
        }
        
        System.out.println();
        System.out.print("sequence length");
        for(int mappingCountBinLowerBorder : mappingCountBinLowerBorders) {
            System.out.print("\t" + mappingCountBinLowerBorder);
        }
        System.out.println();
        
        for(int sequenceLength = minSequenceLength; sequenceLength <= maxSequenceLength; sequenceLength++) {
            if(sequenceLengthDistributionHashMap.get(sequenceLength) != null) {
                System.out.print(sequenceLength);
                
                for(int mappingCountBinIndex = 0; mappingCountBinIndex < mappingCountBinLowerBorders.length; mappingCountBinIndex++) {
                     System.out.print("\t" + ((double) sequenceLengthDistributionHashMap.get(sequenceLength)[mappingCountBinIndex] * 100.0 / (double) sequenceLengthReadCount[mappingCountBinIndex]));
                }
                
                System.out.println();
            }
        }
    }
    
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String sequenceSetFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-fileName <filename> \tfile containing sRNA indices (one per line)");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("fileName")) {
                        sequenceSetFileName = argumentValue;
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
        }

        if((sequenceSetFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-fileName <filename> \tfile containing sRNA indices (one per line)");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }

        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);

        try {
            SequenceSetSizeMappingCountDistribution sequenceSetSizeMappingCountDistribution = new SequenceSetSizeMappingCountDistribution(sequenceSetFileName, databaseLoginData);
        } catch(IOException e) {
            System.out.println(e);
        } catch(SQLException e) {
            System.out.println(e);
        }
    }
}
