package sequencesetfastageneration;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SequenceSetFastaGeneration {
    public SequenceSetFastaGeneration(String sequenceSetFileName, DatabaseLoginData databaseLoginData) throws IOException, SQLException {
        HashMap<Integer,Integer> sequenceLengthDistributionHashMap = new HashMap();

        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement statement = databaseConnection.getConnection().createStatement();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(sequenceSetFileName));
        String sequenceIndex = null;
        while((sequenceIndex = bufferedReader.readLine()) != null) {
            ResultSet sequenceResultSet = statement.executeQuery("SELECT * FROM srna_sequence WHERE sequence_id=" + sequenceIndex);
            if(sequenceResultSet.next()) {
                String sequence = sequenceResultSet.getString("sequence");

                System.out.println(">" + sequenceIndex);
                System.out.println(sequence);
            }
            sequenceResultSet.next();
        }
        bufferedReader.close();
        
        statement.close();
        databaseConnection.close();
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
                    
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    if(argumentTitle.equals("fileName")) {
                        sequenceSetFileName = argumentValue;
                    }
                }
            }
            
            if((databaseUser == null) || (databasePassword == null) || (sequenceSetFileName == null)) {
                System.out.println("Usage:");
                System.out.println("-fileName <filename> \tfile containing sRNA indices (one per line)");
                System.out.println("-databaseUser <username>");
                System.out.println("-databasePassword <password>");

                System.exit(1);
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            SequenceSetFastaGeneration sequenceSetFastaGeneration = new SequenceSetFastaGeneration(sequenceSetFileName, databaseLoginData);
        } catch(IOException e) {
            System.out.println(e);
        } catch(SQLException e) {
            System.out.println(e);
        }
    }
}
