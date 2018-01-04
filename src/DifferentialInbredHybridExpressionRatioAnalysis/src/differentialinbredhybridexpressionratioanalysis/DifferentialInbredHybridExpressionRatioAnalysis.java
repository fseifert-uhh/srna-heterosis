package differentialinbredhybridexpressionratioanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;


public class DifferentialInbredHybridExpressionRatioAnalysis {
    public DifferentialInbredHybridExpressionRatioAnalysis(DatabaseLoginData databaseLoginData) throws SQLException {
        this.databaseLoginData = databaseLoginData;
    }
    
    public void calculateExpressionRatioDistribution(String associatedSequencesFileName, double minExpressionThreshold, double minFoldChangeThreshold, boolean differentialExpressionFlag) throws FileNotFoundException, IOException, SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection(databaseLoginData);
        
        // collect associated sRNA sequence indices
        HashMap<String,Boolean> associatedSequenceIndexHashMap = new HashMap();

        BufferedReader associatedSequencesBufferedReader = new BufferedReader(new FileReader(associatedSequencesFileName));
        String associatedSequencesDataLine;
        while((associatedSequencesDataLine = associatedSequencesBufferedReader.readLine()) != null) {
            String[] associatedSequencesDataLineParts = associatedSequencesDataLine.split("\t");
            
            if(associatedSequencesDataLineParts.length >= 1) {
                associatedSequenceIndexHashMap.put(associatedSequencesDataLineParts[0], true);
            }
        }
        associatedSequencesBufferedReader.close();        
        
        // collect hybrid/inbred data
        ArrayList<Integer[]> hybridInbredIdsArrayList = new ArrayList();
        
        Statement hybridDataStatement = databaseConnection.getConnection().createStatement();
        ResultSet hybridDataResultSet = hybridDataStatement.executeQuery("SELECT * FROM hybrid_library_assignment");
        while(hybridDataResultSet.next()) {
            int hybridId = hybridDataResultSet.getInt("hybrid_id");
            int hybridLibraryId = hybridDataResultSet.getInt("library_id");
            
            Statement hybridInbredDataStatement = databaseConnection.getConnection().createStatement();
            ResultSet hybridInbredDataResultSet = hybridInbredDataStatement.executeQuery("SELECT * FROM hybrid_inbred_pairs WHERE hybrid_id=" + hybridId);
            if(hybridInbredDataResultSet.next()) {
                int parent1LibraryId = hybridInbredDataResultSet.getByte("parent1_id");
                int parent2LibraryId = hybridInbredDataResultSet.getByte("parent2_id");
                
                Integer[] hybridInbredLibraryId = new Integer[3];
                hybridInbredLibraryId[0] = hybridLibraryId;
                hybridInbredLibraryId[1] = parent1LibraryId;
                hybridInbredLibraryId[2] = parent2LibraryId;
                hybridInbredIdsArrayList.add(hybridInbredLibraryId);
            }
            hybridInbredDataResultSet.close();
            hybridInbredDataStatement.close();
        }
        hybridDataResultSet.close();
        hybridDataStatement.close();
        
        // calculate expression ratios
        ArrayList<String[]> hybridInbredTitlesArrayList = new ArrayList();
        for(Integer[] hybridInbredLibraryIds : hybridInbredIdsArrayList) {
            hybridInbredTitlesArrayList.add(new String[3]);
            Statement hybridInbredTitleStatement = databaseConnection.getConnection().createStatement();
            for(int hybridInbredIndex = 0; hybridInbredIndex <= 2; hybridInbredIndex++) {
                ResultSet hybridInbredTitleResultSet = hybridInbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE library_id=" + hybridInbredLibraryIds[hybridInbredIndex]);
                if(hybridInbredTitleResultSet.next()) {
                    hybridInbredTitlesArrayList.get(hybridInbredTitlesArrayList.size() - 1)[hybridInbredIndex] = hybridInbredTitleResultSet.getString("library_title");
                }
                hybridInbredTitleResultSet.close();
            }
            hybridInbredTitleStatement.close();
        }
        
        DatabaseConnection hybridInbredExpressionDatabaseConnection = new DatabaseConnection(databaseLoginData);
        Statement hybridInbredExpressionDataStatement = hybridInbredExpressionDatabaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        hybridInbredExpressionDataStatement.setFetchSize(Integer.MIN_VALUE);
        
        for(String[] hybridInbredLibraryTitles : hybridInbredTitlesArrayList) {
            System.out.print("\t" + hybridInbredLibraryTitles[0]);
        }
        System.out.println();
        
        ResultSet hybridInbredExpressionDataResultSet = hybridInbredExpressionDataStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(hybridInbredExpressionDataResultSet.next()) {
            String sequenceIndex = hybridInbredExpressionDataResultSet.getString("sequence_id");

            if(associatedSequenceIndexHashMap.get(sequenceIndex) != null) {
                System.out.print(sequenceIndex);
                
                for(int hybridInbredIndex = 0; hybridInbredIndex < hybridInbredTitlesArrayList.size(); hybridInbredIndex++) {
                    String[] hybridInbredTitles = hybridInbredTitlesArrayList.get(hybridInbredIndex);
                    
                    double lowParentExpression = Math.min(hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[1]), hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[2]));
                    double highParentExpression = Math.max(hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[1]), hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[2]));

                    boolean differentialExpression = (differentialExpressionFlag ? false : true);
                    if(minExpressionThreshold > 0) {
                        if(lowParentExpression < minExpressionThreshold) {
                            if(highParentExpression >= (minExpressionThreshold * minFoldChangeThreshold)) {
                                differentialExpression = true;
                            }
                        }
                        else if((highParentExpression / lowParentExpression) >= minFoldChangeThreshold) {
                            differentialExpression = true;
                        }
                    }
                    else {
                        if(lowParentExpression == 0) {
                            if(highParentExpression >= minFoldChangeThreshold) {
                                differentialExpression = true;
                            }
                        }
                        else if((highParentExpression / lowParentExpression) >= minFoldChangeThreshold) {
                            differentialExpression = true;
                        }
                    }
                    
                    if(differentialExpression) {
                        double midParentExpression = ((hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[1]) + hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[2])) / 2.0);
                    
                        double hybridExpression = hybridInbredExpressionDataResultSet.getDouble(hybridInbredTitles[0]);

                        if(hybridExpression == 0) {
                            System.out.print("\tabsent");
                        }
                        else {
                            double dominantToAdditiveRatio = ((hybridExpression - midParentExpression) / (midParentExpression - lowParentExpression));

                            if(midParentExpression == 0) {
                                System.out.print("\tnovel");
                            }
                            else if(midParentExpression == lowParentExpression) {
                                System.out.print("\tnon-additive");
                            }
                            else {
                                System.out.print("\t" +  dominantToAdditiveRatio);
                            }
                        }
                    }
                    else {
                        System.out.print("\t");
                    }
                }

                System.out.println();
            }
        }
        hybridInbredExpressionDataResultSet.close();
        hybridInbredExpressionDataStatement.close();
        hybridInbredExpressionDatabaseConnection.close();

        databaseConnection.close();
    }

    public static void main(String[] args) {
        boolean differentialExpressionFlag = true;
        
        String associatedSequencesFileName = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("-associatedSequencesFilename <filename> \tfile containing associated sequence ids in first column");
            System.out.println("-differentialExpressionTest {yes|no}");
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
                    
                    if(argumentTitle.equals("associatedSequencesFilename")) {
                        associatedSequencesFileName = argumentValue;
                    }
                    if(argumentTitle.equals("differentialExpressionTest")) {
                        differentialExpressionFlag = (argumentValue.equals("no") ? false : true);
                    }
                    if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    if(argumentTitle.equals("associatedSequencesFilename")) {
                        databasePassword = databasePassword;
                    }
                }
            }
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);

        double minExpressionThreshold = 0.5;
        double minFoldChangeThreshold = 2.0;
        
        try {
            DifferentialInbredHybridExpressionRatioAnalysis inbredHybridExpressionRatioAnalysis = new DifferentialInbredHybridExpressionRatioAnalysis(databaseLoginData);
            inbredHybridExpressionRatioAnalysis.calculateExpressionRatioDistribution(associatedSequencesFileName, minExpressionThreshold, minFoldChangeThreshold, differentialExpressionFlag);
        } catch(SQLException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        }
    }
    
    private final DatabaseLoginData databaseLoginData;
}
