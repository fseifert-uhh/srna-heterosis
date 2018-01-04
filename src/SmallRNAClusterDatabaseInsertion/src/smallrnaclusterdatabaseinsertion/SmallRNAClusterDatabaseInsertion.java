package smallrnaclusterdatabaseinsertion;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class SmallRNAClusterDatabaseInsertion {
    public static void main(String[] args) {
        String clusterDataFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-clusterDataFile <file name>");
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
                    
                    if(argumentTitle.equals("clusterDataFile")) {
                        clusterDataFileName = argumentValue;
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
        
        if((clusterDataFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-clusterDataFile <file name>");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");            
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
            Statement statement = databaseConnection.getConnection().createStatement();
            databaseConnection.executeQuery("TRUNCATE cluster_data", statement);
            
            int insertCount = 0;
            StringBuilder clusterDatabaseInsertStringBuilder = new StringBuilder("INSERT INTO cluster_data (cluster_id, chromosome, strand, start_position, end_position, sequence_id_text) VALUES ");
            
            BufferedReader clusterDataBufferedReader = new BufferedReader(new FileReader(clusterDataFileName));
            String fileLine = null;
            while((fileLine = clusterDataBufferedReader.readLine()) != null) {
                String[] clusterData = fileLine.split("\t");
                
                if(insertCount > 0) {
                    clusterDatabaseInsertStringBuilder.append(",");
                }                
                clusterDatabaseInsertStringBuilder.append("(" + clusterData[0] + "," + clusterData[1].replace("chr", "") + ",\"" + (clusterData[2].equals("plus") ? "+" : "-") + "\"," + clusterData[3] + "," + clusterData[4] + ",\"" + clusterData[5] + "\")");
                insertCount++;
                
                if(insertCount > 10) {
                    databaseConnection.executeQuery(clusterDatabaseInsertStringBuilder.toString(), statement);
                    
                    insertCount = 0;
                    clusterDatabaseInsertStringBuilder = new StringBuilder("INSERT INTO cluster_data (cluster_id, chromosome, strand, start_position, end_position, sequence_id_text) VALUES ");
                }
            }
            
            if(insertCount > 0) {
                databaseConnection.executeQuery(clusterDatabaseInsertStringBuilder.toString(), statement);
            }
            
            databaseConnection.close();
        } catch(IOException e) { 
            System.out.println(e);
        } catch(SQLException e) {
            System.out.println(e);
        }
    }
}
