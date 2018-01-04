package generatedistancematrixrelavitetolibrary;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GenerateDistanceMatrixRelativeToLibrary {
    public GenerateDistanceMatrixRelativeToLibrary(DatabaseLoginData databaseLoginData) throws SQLException {
        databaseConnection = new DatabaseConnection(databaseLoginData);
        if(databaseConnection.isActive()) {
            inbredTitleList = getInbredTitleList();
        }
    }
    
    public void close() throws SQLException {
        databaseConnection.close();
    }
    
    public void exportDistanceMatrix(double[][] distanceMatrix) {
        boolean firstTitleFlag = true;
        
        for(String inbredTitle : inbredTitleList) {
            if(!firstTitleFlag) {
                System.out.print("\t");
            }
            
            System.out.print(inbredTitle);
            
            firstTitleFlag = false;
        }
        System.out.println();
        
        for(int inbred1Index = 0; inbred1Index < inbredTitleList.size(); inbred1Index++) {
            firstTitleFlag = true;
            
            for(int inbred2Index = 0; inbred2Index < inbredTitleList.size(); inbred2Index++) {
                if(!firstTitleFlag) {
                    System.out.print("\t");
                }
                
                System.out.print(new DecimalFormat("0.000000").format(distanceMatrix[inbred1Index][inbred2Index]).replace(",", "."));
                
                firstTitleFlag = false;
            }
            
            System.out.println();
        }
    }
    
    public double[][] calculateDistanceMatrix(DistanceType distanceType, double minExpressionThreshold, double minFoldChangeThreshold) throws SQLException {
        double[][] distanceMatrix = new double[inbredTitleList.size()][inbredTitleList.size()];
        double[] differentialSequenceCount = new double[inbredTitleList.size()];
        int[] sequenceCount = new int[inbredTitleList.size()];
            
        Statement inbredElementExpressionStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        inbredElementExpressionStatement.setFetchSize(Integer.MIN_VALUE);

        ResultSet inbredElementExpressionResultSet = inbredElementExpressionStatement.executeQuery("SELECT * FROM srna_library_expression");
        while(inbredElementExpressionResultSet.next()) {
            boolean[] differentiallyExpressed = new boolean[inbredTitleList.size()];
            double[] inbredExpression = new double[inbredTitleList.size()];
            
            for(int inbredIndex = 0; inbredIndex < inbredTitleList.size(); inbredIndex++) {
                inbredExpression[inbredIndex] = inbredElementExpressionResultSet.getDouble(inbredTitleList.get(inbredIndex));
            }

            for(int inbred1Index = 0; inbred1Index < inbredTitleList.size(); inbred1Index++) {
                for(int inbred2Index = 0; inbred2Index < inbred1Index; inbred2Index++) {
                    boolean differentiallyExpressedPair = false;
                    double lowParentExpression = Math.min(inbredExpression[inbred1Index], inbredExpression[inbred2Index]);
                    double highParentExpression = Math.max(inbredExpression[inbred1Index], inbredExpression[inbred2Index]);
                    
                    if(lowParentExpression < minExpressionThreshold) {
                        if(highParentExpression >= (minExpressionThreshold * minFoldChangeThreshold)) {
                            differentiallyExpressedPair =  true;
                        }
                    }
                    else if((highParentExpression / lowParentExpression) >= minFoldChangeThreshold) {
                        differentiallyExpressedPair =  true;
                    }
                    
                    if(differentiallyExpressedPair) {
                        differentiallyExpressed[inbred1Index] = true;
                        differentiallyExpressed[inbred2Index] = true;
                    }
                    
                    if(differentiallyExpressedPair) {
                        if(distanceType == DistanceType.BINARY_DISTANCE) {
                            distanceMatrix[inbred1Index][inbred2Index]++;
                            distanceMatrix[inbred2Index][inbred1Index]++;
                        }
                        else if(distanceType == DistanceType.EUCLIDEAN_DISTANCE) {
                            distanceMatrix[inbred1Index][inbred2Index] += Math.pow((lowParentExpression - highParentExpression), 2);
                            distanceMatrix[inbred2Index][inbred1Index] += Math.pow((lowParentExpression - highParentExpression), 2);
                        }
                    }
                }
            }
            
            for(int libraryIndex = 0; libraryIndex < inbredTitleList.size(); libraryIndex++) {
                if(differentiallyExpressed[libraryIndex]) {
                    sequenceCount[libraryIndex]++;
                }
            }
        }
        
        for(int inbred1Index = 0; inbred1Index < inbredTitleList.size(); inbred1Index++) {
            for(int inbred2Index = 0; inbred2Index < inbredTitleList.size(); inbred2Index++) {
                if(distanceType == DistanceType.BINARY_DISTANCE) {
                    distanceMatrix[inbred1Index][inbred2Index] = Math.sqrt(distanceMatrix[inbred1Index][inbred2Index] / (double) sequenceCount[inbred1Index]);
                }
                else if(distanceType == DistanceType.EUCLIDEAN_DISTANCE) {
                    distanceMatrix[inbred1Index][inbred2Index] = Math.sqrt(distanceMatrix[inbred1Index][inbred2Index]);
                }
            }
        }
        
        return distanceMatrix;
    }
        
    private ArrayList<String> getInbredTitleList() throws SQLException {
        ArrayList<String> inbredTitleList = new ArrayList();
        
        Statement inbredTitleStatement = databaseConnection.getConnection().createStatement();
        ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE germplasm=0 OR germplasm=1 ORDER BY library_id"); // incl. B73 germplasm<2
        while(inbredTitleResultSet.next()) {
            int inbredId = inbredTitleResultSet.getInt("library_id");
            String inbredTitle = inbredTitleResultSet.getString("library_title");
            
            inbredTitleList.add(inbredId, inbredTitle);
        }
        inbredTitleResultSet.close();
        inbredTitleStatement.close();
        
        return inbredTitleList;
    }
    
    public static void main(String[] args) {
        double minimumExpression = 0;
        double minimumFoldChange = 0;
        
        DistanceType distanceType = DistanceType.BINARY_DISTANCE;
        
        String databaseUser = null;
        String databasePassword = null;
        
        if(args.length == 0) {
            System.out.println("Usage: ");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-distance <distance_type>\tbinary (default), euclidean");
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
                    
                    if(argumentTitle.equals("distance")) {
                        if(argumentValue.equals("binary")) {
                            distanceType = DistanceType.BINARY_DISTANCE;
                        }
                        else if(argumentValue.equals("euclidean")) {
                            distanceType = DistanceType.EUCLIDEAN_DISTANCE;
                        }
                        else {
                            System.out.println("Error: distance type not known");
                            System.exit(1);
                        }
                    }
                    else if(argumentTitle.equals("minimumExpression")) {
                        minimumExpression = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("minimumFoldChange")) {
                        minimumFoldChange = Double.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("databaseUser")) {
                        databaseUser = argumentValue;
                    }
                    else if(argumentTitle.equals("minimumFoldChange")) {
                        databasePassword = argumentValue;
                    }
                }
            }
        }
        
        if((databaseUser == null) || (databasePassword == null)) {
            System.out.println("Usage: ");
            System.out.println("-minimumExpression <expression> \tminimum expression value");
            System.out.println("-minimumFoldChange <fold change>\tminimum fold change");
            System.out.println("-distance <distance_type>\tbinary (default), euclidean");
            System.out.println("-databaseUser <username>");
            System.out.println("-databasePassword <password>");
            System.exit(1);
        }        
        
        try {
            DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
            GenerateDistanceMatrixRelativeToLibrary generateDistanceMatrix = new GenerateDistanceMatrixRelativeToLibrary(databaseLoginData);
            
            double[][] distanceMatrix = null;
            if(distanceType == DistanceType.BINARY_DISTANCE) {
                distanceMatrix = generateDistanceMatrix.calculateDistanceMatrix(DistanceType.BINARY_DISTANCE, minimumExpression, minimumFoldChange);
            }
            else if(distanceType == DistanceType.EUCLIDEAN_DISTANCE) {
                distanceMatrix = generateDistanceMatrix.calculateDistanceMatrix(DistanceType.EUCLIDEAN_DISTANCE, minimumExpression, minimumFoldChange);
            }            
            
            generateDistanceMatrix.exportDistanceMatrix(distanceMatrix);
            generateDistanceMatrix.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private int hybridCount;
    private int[][] hybridInbredParentIdentifiers;
    
    private ArrayList<String> inbredTitleList;
    
    private DatabaseConnection databaseConnection;
}