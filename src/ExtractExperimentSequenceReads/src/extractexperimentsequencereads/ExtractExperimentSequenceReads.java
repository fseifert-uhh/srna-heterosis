package extractexperimentsequencereads;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExtractExperimentSequenceReads {
    public static void main(String[] args) {
        String databaseUser = null;
        String databasePassword = null;
        String outputFileName = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-dbUser <user>\tmysql user name");
            System.out.println("-dbPassword <password>\tmysql user password");
            System.out.println("-outputFileName <file name>\tthe name of the file to be generated");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("dbUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("dbPassword")) {
                        databasePassword = argumentValue;
                    }
                    else if(argumentTitle.equals("outputFileName")) {
                        outputFileName = argumentValue;
                    }
                }
            }
        }
        
        if((outputFileName == null) || (databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage:");
            System.out.println("-dbUser <user>\tmysql user name");
            System.out.println("-dbPassword <password>\tmysql user password");
            System.out.println("-outputFileName <file name>\tthe name of the file to be generated");
            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
            Statement statement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(Integer.MIN_VALUE);
            
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));
            
            ResultSet sequenceDataResultSet = statement.executeQuery("SELECT * FROM srna_sequence ORDER BY sequence_id");
            while(sequenceDataResultSet.next()) {
                bufferedWriter.append(">" + sequenceDataResultSet.getString("sequence_id"));
                bufferedWriter.newLine();
                bufferedWriter.append(sequenceDataResultSet.getString("sequence"));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            sequenceDataResultSet.close();

            bufferedWriter.close();
            
            statement.close();
            databaseConnection.close();
        } catch(SQLException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        }
        
    }
}
