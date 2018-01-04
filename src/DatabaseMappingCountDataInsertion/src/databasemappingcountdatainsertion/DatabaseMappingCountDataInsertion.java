package databasemappingcountdatainsertion;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;

public class DatabaseMappingCountDataInsertion {
    public static void main(String[] args) {
        boolean skipSequenceInsertion = false;
        
        String databaseUser = null;
        String databasePassword = null;
        String bamMappingFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-bamMappingFile <file name>\tthe name of the BAM mapping file");
            System.out.println("-dbUser <user>\tmysql user name");
            System.out.println("-dbPassword <password>\tmysql user password");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("bamMappingFile")) {
                        bamMappingFileName = argumentValue;
                    }
                    else if(argumentTitle.equals("dbUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("dbPassword")) {
                        databasePassword = argumentValue;
                    }
                }
            }
        }
        
        if((bamMappingFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-bamMappingFile <file name>\tthe name of the BAM mapping file");
            System.out.println("-dbUser <user>\tmysql user name");
            System.out.println("-dbPassword <password>\tmysql user password");
            System.exit(1);
        }
        
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
            Statement statement = databaseConnection.getConnection().createStatement();
            databaseConnection.executeQuery("TRUNCATE srna_heterosis.srna_mapping_status", statement);
            
            SAMFileReader bamFileReader = new SAMFileReader(new File(bamMappingFileName));
        
            SAMFileHeader bamFileHeader = bamFileReader.getFileHeader();
            SAMSequenceDictionary bamSequenceDictionary = bamFileHeader.getSequenceDictionary();
        
            int mappingCount = 0;
            int mappingInsertCount = 0;
            String readTitle = null;
            String previousReadTitle = null;
            
            StringBuilder mappingCountDatabaseInsertStringBuilder = new StringBuilder("INSERT INTO srna_heterosis.srna_mapping_status (sequence_id, mapping_count) VALUES ");
            
            for(SAMRecord bamRecord : bamFileReader) {
                readTitle = bamRecord.getReadName();

                if(previousReadTitle != null) {
                    if(!readTitle.equals(previousReadTitle)) {
                        if(mappingInsertCount > 0) {
                            mappingCountDatabaseInsertStringBuilder.append(", ");
                        }
                    
                        mappingCountDatabaseInsertStringBuilder.append("(" + previousReadTitle + ", " + mappingCount + ")");
                        mappingInsertCount++;
                    
                        mappingCount = 0;
                        previousReadTitle = readTitle;
                    }
                }
                
                previousReadTitle = readTitle;
                
                if(!bamRecord.getReadUnmappedFlag()) {
                    mappingCount++;
                }
                
                if(mappingInsertCount >= 10000) {
                    databaseConnection.executeQuery(mappingCountDatabaseInsertStringBuilder.toString(), statement);
                    
                    mappingCountDatabaseInsertStringBuilder = new StringBuilder("INSERT INTO srna_heterosis.srna_mapping_status (sequence_id, mapping_count) VALUES ");
                    mappingInsertCount = 0;
                }
            }
            
            if(mappingInsertCount > 0) {
                mappingCountDatabaseInsertStringBuilder.append(", ");
            }
            mappingCountDatabaseInsertStringBuilder.append("(" + previousReadTitle + ", " + mappingCount + ")");
            databaseConnection.executeQuery(mappingCountDatabaseInsertStringBuilder.toString(), statement);
            
            databaseConnection.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
