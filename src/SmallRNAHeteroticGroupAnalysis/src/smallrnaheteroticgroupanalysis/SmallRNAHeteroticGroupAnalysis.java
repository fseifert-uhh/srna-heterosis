package smallrnaheteroticgroupanalysis;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SmallRNAHeteroticGroupAnalysis {
    public SmallRNAHeteroticGroupAnalysis(DatabaseLoginData databaseLoginData, boolean populationFlag) throws SQLException {
        databaseConnection = new DatabaseConnection(databaseLoginData);
        if(databaseConnection.isActive()) {
            this.loadInbredTitles(populationFlag);
        }
    }
    
    public void close() throws SQLException {
        databaseConnection.close();
    }

    private void loadInbredTitles(boolean populationFlag) throws SQLException {
        lineTitleHashMap = new HashMap[3];
        lineTitleHashMap[0] = new HashMap();
        lineTitleHashMap[1] = new HashMap();
        lineTitleHashMap[2] = new HashMap();
        
        if(populationFlag) {
            Statement inbredTitleStatement = databaseConnection.getConnection().createStatement();
            ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE germplasm>=0 ORDER BY library_id");
            while(inbredTitleResultSet.next()) {
                int inbredId = inbredTitleResultSet.getInt("library_id");
                String inbredTitle = inbredTitleResultSet.getString("library_title");
                int heteroticGroup = inbredTitleResultSet.getInt("germplasm");

                lineTitleHashMap[heteroticGroup].put(inbredId, inbredTitle);
            }
            inbredTitleResultSet.close();
            inbredTitleStatement.close();
        }
        else {
            /* specific hybrid */
            lineTitleHashMap[0].put(0, "f039");
            lineTitleHashMap[0].put(1, "f047");
            lineTitleHashMap[0].put(2, "l024");
            lineTitleHashMap[1].put(0, "p033");
            lineTitleHashMap[1].put(1, "s028");
            lineTitleHashMap[2].put(0, "p033xf047");
            lineTitleHashMap[2].put(1, "s028xf039");
            lineTitleHashMap[2].put(2, "s028xl024");
        }
    }
    
    public void performAnalysis(double minimalExpression) throws SQLException {
        int[] smallRNAHeteroticGroupSets = new int[8];
        
        Statement smallRNAExpressionStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        smallRNAExpressionStatement.setFetchSize(Integer.MIN_VALUE);
        ResultSet smallRNAExpressionResultSet = smallRNAExpressionStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(smallRNAExpressionResultSet.next()) {
            int sequenceIndex = smallRNAExpressionResultSet.getInt("sequence_id");
            
            int[] heteroticGroupExpressionCount = new int[3];
            
            for(int heteroticGroupIndex = 0; heteroticGroupIndex <= 2; heteroticGroupIndex++) {
                for(int libraryIndex : lineTitleHashMap[heteroticGroupIndex].keySet()) {
                    if(smallRNAExpressionResultSet.getDouble(lineTitleHashMap[heteroticGroupIndex].get(libraryIndex)) >= minimalExpression) {
                        heteroticGroupExpressionCount[heteroticGroupIndex]++;
                    }
                }
            }
            
            int expressionIntersectionGroup = 0;
            if(heteroticGroupExpressionCount[0] > 0) {
                expressionIntersectionGroup += 1;
            }
            if(heteroticGroupExpressionCount[1] > 0) {
                expressionIntersectionGroup += 2;
            }
            if(heteroticGroupExpressionCount[2] > 0) {
                expressionIntersectionGroup += 4;
            }
            
            smallRNAHeteroticGroupSets[expressionIntersectionGroup]++;
        }
        
        smallRNAExpressionResultSet.close();
        smallRNAExpressionStatement.close();
        
        System.out.println("not present: " + smallRNAHeteroticGroupSets[0]);
        System.out.println("flint: " + smallRNAHeteroticGroupSets[1]);
        System.out.println("dent: " + smallRNAHeteroticGroupSets[2]);
        System.out.println("flint/dent: " + smallRNAHeteroticGroupSets[3]);
        System.out.println("hybrid: " + smallRNAHeteroticGroupSets[4]);
        System.out.println("flint/hybrid: " + smallRNAHeteroticGroupSets[5]);
        System.out.println("dent/hybrid: " + smallRNAHeteroticGroupSets[6]);
        System.out.println("flint/dent/hybrid: " + smallRNAHeteroticGroupSets[7]);
    }
            
    public static void main(String[] args) {
        double minimalExpression = 0;

        String analysis = null;
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-analysis {flintdent/inbredhybrid}");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
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
                    
                    if(argumentTitle.equals("analysis")) {
                        analysis = argumentValue;
                    }
                    else if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("databasePassword")) {
                        databasePassword = argumentValue;
                    }
                    else if(argumentTitle.equals("minimumExpression")) {
                        minimalExpression = Double.valueOf(argumentValue);
                    }
                }
            }
        }
        
        boolean populationFlag = true;
        
        if(analysis.equals("flintdent")) {
            populationFlag = true;
        }
        else if(analysis.equals("inbredhybrid")) {
            populationFlag = false;
        }
        else {
            analysis = null;
        }
        
        if((analysis == null) || (databaseUser == null) || (databasePassword == null) || (minimalExpression == 0)) {
            System.out.println("Usage:");
            System.out.println("-analysis {flintdent/inbredhybrid}");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-databaseUser <value>");
            System.out.println("-databasePassword <value>");
            System.exit(1);
        }
        
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            SmallRNAHeteroticGroupAnalysis smallRNAHeteroticGroupAnalysis = new SmallRNAHeteroticGroupAnalysis(databaseLoginData, populationFlag);

            smallRNAHeteroticGroupAnalysis.performAnalysis(minimalExpression);
            
            smallRNAHeteroticGroupAnalysis.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private HashMap<Integer,String>[] lineTitleHashMap;
    
    private final DatabaseConnection databaseConnection;
}