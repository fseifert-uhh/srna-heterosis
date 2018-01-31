package hybridcharacteristiccorrelationdatabase;

import de.uni_hamburg.fseifert.mysql.DatabaseConnection;
import de.uni_hamburg.fseifert.mysql.DatabaseLoginData;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrection;
import de.uni_hamburg.fseifert.statistics.MultipleTestingCorrectionMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class HybridCharacteristicSequenceCorrelationDatabase {
    public HybridCharacteristicSequenceCorrelationDatabase(DatabaseLoginData databaseLoginData, BreedingFactorial breedingFactorial) throws SQLException {
        this.databaseLoginData = databaseLoginData;

        this.breedingFactorial = breedingFactorial;
        hybridInbredParentIdentifiers = getHybridInbredParentIdentifiers();
        hybridCount = hybridInbredParentIdentifiers.length;
            
        inbredTitleHashMap = getInbredTitleList();
    }
    
    /**
     * generates an object of HybridExpressionElementData covering inbred line expression data and differential expression information
     * 
     * @param expressionThreshold expression threshold that needs to be reached in higher expressed parent of one inbred line combination
     * @param expressionFoldChangeThreshold fold change that needs to be reached in one inbred line combination
     * @param differentialExpressionPValue p-value threshold for differential expression
     * @return DifferentialExpressedElementData object containing expression data and information about differential expression between inbred lines
     * @throws SQLException 
     */
    public HybridExpressionElementData getDifferentialExpressedSequences(double expressionThreshold, double expressionFoldChangeThreshold, double differentialExpressionPValue) throws SQLException {
        HashMap<String,HashMap<Integer,Boolean>> differentialExpressedTranscriptFlagHashMap = new HashMap();
        HashMap<String,HashMap<Integer,Double>> transcriptExpressionHashMap = new HashMap();

        DatabaseConnection databaseConnection = null;
        Statement inbredLineStatement = null;
        Statement sequenceDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
        
            if(databaseConnection.isActive()) {
                ArrayList<Double> arrayIndexPValueArrayList = new ArrayList();
                
                databaseConnection = new DatabaseConnection(databaseLoginData);
                Statement expressionDataStatement = databaseConnection.getConnection().createStatement();
                ResultSet expressionDataResultSet = expressionDataStatement.executeQuery("SELECT * FROM microarray_differential_expression_transcripts");
                while(expressionDataResultSet.next()) {
                    double transcriptPvalue = expressionDataResultSet.getDouble("pvalue");
                    arrayIndexPValueArrayList.add(transcriptPvalue);
                }
                expressionDataResultSet.close();
                
                double[] transcriptPValues = new double[arrayIndexPValueArrayList.size()];
                int index = 0;
                for(double pValue : arrayIndexPValueArrayList) {
                    transcriptPValues[index] = pValue;
                    index++;
                }
                
                MultipleTestingCorrection multipleTestingCorrection = new MultipleTestingCorrection(transcriptPValues);
                double transcriptPValueFDR = multipleTestingCorrection.getCorrectedAlphaErrorThreshold(MultipleTestingCorrectionMethod.FDR_BENJAMINI_HOCHBERG, differentialExpressionPValue);
                
                expressionDataResultSet = expressionDataStatement.executeQuery("SELECT * FROM microarray_library_expression,microarray_differential_expression_transcripts,microarray_hybrid_parents_differential_expression_pvalues WHERE microarray_differential_expression_transcripts.pvalue<=" + transcriptPValueFDR + " AND microarray_library_expression.array_id=microarray_hybrid_parents_differential_expression_pvalues.array_id AND microarray_library_expression.array_id=microarray_differential_expression_transcripts.array_id");
                while(expressionDataResultSet.next()) {
                    String arrayIndex = expressionDataResultSet.getString("array_id");
                    
                    HashMap<Integer,Double> inbredTranscriptExpressionHashMap = new HashMap();

                    double[][] germplasmExpressionExtremes = {{-1.0, -1.0}, {-1.0, -1.0}};
                    
                    for(int inbredIndex : breedingFactorial.getInbredIndices()) {
                        double transcriptExpression = expressionDataResultSet.getDouble(breedingFactorial.getInbredTitle(inbredIndex));
                        inbredTranscriptExpressionHashMap.put(inbredIndex, transcriptExpression);
                        
                        int germplasmIndex = breedingFactorial.getInbredGermplasm(breedingFactorial.getInbredTitle(inbredIndex));
                        if(germplasmExpressionExtremes[germplasmIndex][0] == -1.0) {
                            germplasmExpressionExtremes[germplasmIndex][0] = transcriptExpression;
                            germplasmExpressionExtremes[germplasmIndex][1] = transcriptExpression;
                        }
                        
                        if((transcriptExpression >= expressionThreshold) && (transcriptExpression < germplasmExpressionExtremes[germplasmIndex][0])) {
                            germplasmExpressionExtremes[germplasmIndex][0] = transcriptExpression;
                        }
                        else if(transcriptExpression > germplasmExpressionExtremes[germplasmIndex][1]) {
                            germplasmExpressionExtremes[germplasmIndex][1] = transcriptExpression;
                        }
                    }
                    
                    transcriptExpressionHashMap.put(arrayIndex, inbredTranscriptExpressionHashMap);
                    
                    if(Math.max(germplasmExpressionExtremes[0][1], germplasmExpressionExtremes[1][1]) <= expressionThreshold) {
                        continue;
                    }
                    if(Math.max((Math.pow(2.0, germplasmExpressionExtremes[0][1]) / Math.pow(2.0, germplasmExpressionExtremes[1][0])), (Math.pow(2.0, germplasmExpressionExtremes[1][1]) / Math.pow(2.0, germplasmExpressionExtremes[0][0]))) <= expressionFoldChangeThreshold) {
                        continue;
                    }
                    
                    HashMap<Integer,Boolean> hybridDifferentialExpressionFlagHashMap = new HashMap();

                    for(int hybridIndex : breedingFactorial.getHybridIndices()) {
                        boolean hybridDifferentialFlag = false;

                        double hybridDifferentialParentsPvalue = expressionDataResultSet.getDouble("hybrid_" + hybridIndex);
                        if(hybridDifferentialParentsPvalue <= differentialExpressionPValue) {
                            hybridDifferentialFlag = true;
                        }
                    
                        hybridDifferentialExpressionFlagHashMap.put(hybridIndex, hybridDifferentialFlag);
                    }

                    differentialExpressedTranscriptFlagHashMap.put(arrayIndex, hybridDifferentialExpressionFlagHashMap);
                }
                
                expressionDataResultSet.close();
                expressionDataStatement.close();
            }
        }
        finally {
            if(databaseConnection != null) {
                if(inbredLineStatement != null) {
                    inbredLineStatement.close();
                }
                if(sequenceDataStatement != null) {
                    sequenceDataStatement.close();
                }
            
                databaseConnection.close();
            }
        }
        
        HybridExpressionElementData differentialExpressedElementData = new HybridExpressionElementData(differentialExpressedTranscriptFlagHashMap, transcriptExpressionHashMap);
        
        return differentialExpressedElementData;
    }
        
    public HashMap<String,HashMap<Integer,Double>> getSequenceExpressionData() throws SQLException {
        HashMap<String,HashMap<Integer,Double>> arraySequenceExpressionHashMap = new HashMap();

        Statement inbredLineStatement = null;
        Statement sequenceDataStatement = null;
        
        try {
            databaseConnection = new DatabaseConnection(databaseLoginData);
            
            if(databaseConnection.isActive()) {
                sequenceDataStatement = databaseConnection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                sequenceDataStatement.setFetchSize(Integer.MIN_VALUE);

                ResultSet sequenceExpressionResultSet = sequenceDataStatement.executeQuery("SELECT * FROM microarray_library_expression");
                while(sequenceExpressionResultSet.next()) {
                    String arrayIndex = sequenceExpressionResultSet.getString("array_id");

                    HashMap<Integer,Double> sequenceExpressionHashMap = new HashMap();
                    for(int inbredIndex : inbredTitleHashMap.keySet()) {
                        double inbredExpression = sequenceExpressionResultSet.getDouble(inbredTitleHashMap.get(inbredIndex));
                        
                        sequenceExpressionHashMap.put(inbredIndex, inbredExpression);
                    }
                    
                    arraySequenceExpressionHashMap.put(arrayIndex, sequenceExpressionHashMap);
                }
            }
            
            databaseConnection.close();
        }
        finally {
            if(databaseConnection != null) {
                if(inbredLineStatement != null) {
                    inbredLineStatement.close();
                }
                if(sequenceDataStatement != null) {
                    sequenceDataStatement.close();
                }
            
                databaseConnection.close();
            }
        }
        
        return arraySequenceExpressionHashMap;
    }
    
    private HashMap<Integer,String> getInbredTitleList() throws SQLException {
        HashMap<Integer,String> inbredTitleHashMap = new HashMap();
        
        databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement inbredTitleStatement = databaseConnection.getConnection().createStatement();
        ResultSet inbredTitleResultSet = inbredTitleStatement.executeQuery("SELECT * FROM srna_libraries WHERE germplasm=0 OR germplasm=1");
        while(inbredTitleResultSet.next()) {
            int inbredId = inbredTitleResultSet.getInt("library_id");
            String inbredTitle = inbredTitleResultSet.getString("library_title");
            
            inbredTitleHashMap.put(inbredId, inbredTitle);
        }
        inbredTitleResultSet.close();
        inbredTitleStatement.close();
        databaseConnection.close();
        
        return inbredTitleHashMap;
    }
    
    private double[] getHybridCharacteristicValues(HybridCharacteristic hybridCharacteristic) throws SQLException {
        String hybridCharacteristicDatabaseColumn;
        switch(hybridCharacteristic) {
            case BEST_PARENT_HETEROSIS:
                hybridCharacteristicDatabaseColumn = "best_parent_heterosis_r";
                break;
            case HYBRID_PERFORMANCE:
                hybridCharacteristicDatabaseColumn = "hybrid_performance";
                break;
            case MID_PARENT_HETEROSIS:
            default:
                hybridCharacteristicDatabaseColumn = "mid_parent_heterosis_r";
        }
        
        double[] hybridCharacteristicValues = null;
        
        databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement hybridCharacteristicStatement = databaseConnection.getConnection().createStatement();
        
        ResultSet hybridCharacteristicResultSet = hybridCharacteristicStatement.executeQuery("SELECT count(hybrid_id) FROM hybrid_field_data");
        hybridCharacteristicResultSet.next();
        int hybridCount = hybridCharacteristicResultSet.getInt("count(hybrid_id)");
        hybridCharacteristicValues = new double[hybridCount];

        hybridCharacteristicResultSet = hybridCharacteristicStatement.executeQuery("SELECT * FROM hybrid_field_data");
        while(hybridCharacteristicResultSet.next()) {
            int hybridId = hybridCharacteristicResultSet.getInt("hybrid_id");
            hybridCharacteristicValues[hybridId] = hybridCharacteristicResultSet.getDouble(hybridCharacteristicDatabaseColumn);
        }

        hybridCharacteristicResultSet.close();
        hybridCharacteristicStatement.close();
        databaseConnection.close();
        
        return hybridCharacteristicValues;
    }
    
    private int[][] getHybridInbredParentIdentifiers() throws SQLException {
        int[][] hybridInbredParentIdentifiers = null;
        
        databaseConnection = new DatabaseConnection(databaseLoginData);
        Statement hybridParentsStatement = databaseConnection.getConnection().createStatement();
        
        ResultSet hybridParentsResultSet = hybridParentsStatement.executeQuery("SELECT *,count(hybrid_id) FROM hybrid_inbred_pairs ORDER BY hybrid_id");
        hybridParentsResultSet.next();
        int hybridCount = hybridParentsResultSet.getInt("count(hybrid_id)");
        hybridInbredParentIdentifiers = new int[hybridCount][2];
        
        hybridParentsResultSet = hybridParentsStatement.executeQuery("SELECT * FROM hybrid_inbred_pairs ORDER BY hybrid_id");
        while(hybridParentsResultSet.next()) {
            int hybridId = hybridParentsResultSet.getInt("hybrid_id");
            hybridInbredParentIdentifiers[hybridId][0] = hybridParentsResultSet.getInt("parent1_id");
            hybridInbredParentIdentifiers[hybridId][1] = hybridParentsResultSet.getInt("parent2_id");
        }
        
        hybridParentsResultSet.close();
        hybridParentsStatement.close();
        databaseConnection.close();
        
        return hybridInbredParentIdentifiers;
    }
    
    public void performCorrelation(HybridTraitCharacteristic hybridTraitCharacteristic, HybridExpressionElementData hybridExpressionElementData) throws SQLException {
        if(hybridExpressionElementData.getDifferentialFlagData().isEmpty()) {
            return;
        }
        
        double[] hybridTraitCharacteristicValues = new double[hybridCount];
        double[] hybridBinaryDistance = new double[hybridCount];
        double[] hybridEuclideanDistance = new double[hybridCount];

        double[] binaryDistance = new double[hybridCount];
        double[] euclideanDistance = new double[hybridCount];

        for(String arrayIndex : hybridExpressionElementData.getDifferentialFlagData().keySet()) {
            for(int hybridIndex : breedingFactorial.getHybridIndices()) {
                if(!hybridExpressionElementData.getDifferentialFlagData().get(arrayIndex).get(hybridIndex)) {
                    continue;
                }
                    
                double lowParentExpression = Math.min(hybridExpressionElementData.getExpressionData().get(arrayIndex).get(hybridInbredParentIdentifiers[hybridIndex][0]), hybridExpressionElementData.getExpressionData().get(arrayIndex).get(hybridInbredParentIdentifiers[hybridIndex][1]));
                double highParentExpression = Math.max(hybridExpressionElementData.getExpressionData().get(arrayIndex).get(hybridInbredParentIdentifiers[hybridIndex][0]), hybridExpressionElementData.getExpressionData().get(arrayIndex).get(hybridInbredParentIdentifiers[hybridIndex][1]));

                if((hybridExpressionElementData.getDifferentialFlagData().get(arrayIndex) != null) && (hybridExpressionElementData.getDifferentialFlagData().get(arrayIndex).get(hybridIndex) == true)) {
                    binaryDistance[hybridIndex]++;
                }

                euclideanDistance[hybridIndex] += Math.pow((highParentExpression - lowParentExpression), 2);
            }
        }

        System.out.println();
        System.out.println("correlation: ");
        
        for(int hybridIndex = 0; hybridIndex < hybridCount; hybridIndex++) {
            hybridBinaryDistance[hybridIndex] = (Math.sqrt(binaryDistance[hybridIndex] / (double) hybridExpressionElementData.getDifferentialFlagData().size()));
            hybridEuclideanDistance[hybridIndex] = Math.sqrt(euclideanDistance[hybridIndex]);
            hybridTraitCharacteristicValues[hybridIndex] = breedingFactorial.getHybridTraitCharacteristicValue(hybridIndex);
            
            System.out.println(hybridIndex + ";"  + hybridBinaryDistance[hybridIndex] + ";" + hybridEuclideanDistance[hybridIndex] + ";" + breedingFactorial.getHybridTraitCharacteristicValue(hybridIndex));
        }
        
        PearsonsCorrelation binaryDistanceHybridCharacteristicCorrelation = new PearsonsCorrelation();
        double binaryDistanceCorrelationCoefficient = binaryDistanceHybridCharacteristicCorrelation.correlation(hybridBinaryDistance, hybridTraitCharacteristicValues);
        PearsonsCorrelation euclideanDistanceHybridCharacteristicCorrelation = new PearsonsCorrelation();
        double euclideanDistanceCorrelationCoefficient = binaryDistanceHybridCharacteristicCorrelation.correlation(hybridEuclideanDistance, hybridTraitCharacteristicValues);

        System.out.print(hybridExpressionElementData.getDifferentialFlagData().size() + ";" + binaryDistanceCorrelationCoefficient + ";" + euclideanDistanceCorrelationCoefficient);
    }
    
    public void testSequences(DatabaseLoginData databaseLoginData, HybridTraitCharacteristic hybridTraitCharacteristic, double expressionThreshold, double expressionFoldChangeThreshold, double differentialExpressionPvalueThreshold) throws SQLException {
        HashMap<String,HashMap<Integer,Double>> sequenceExpressionHashMap = getSequenceExpressionData();
        HybridExpressionElementData hybridExpressionElementData = getDifferentialExpressedSequences(expressionThreshold, expressionFoldChangeThreshold, differentialExpressionPvalueThreshold);
        
        this.performCorrelation(hybridTraitCharacteristic, hybridExpressionElementData);
    }
            
    public static void main(String[] args) {
        double expressionThreshold = 8.0;
        double expressionFoldChangeThreshold = 1.3;
        double differentialExpressionPvalueThreshold = 0.05;
        double falseDiscoveryRate = 0.05;
        
        HybridTraitCharacteristic hybridTraitCharacteristic = HybridTraitCharacteristic.HYBRID_PERFORMANCE;
        
        String databaseUser = null;
        String databasePassword = null;
        String[][] germplasmInbredLines = {{"f037", "f039", "f043", "f047", "l024", "l035", "l043"}, {"p033", "p040", "p046", "p048", "p063", "p066", "s028", "s036", "s044", "s046", "s049", "s050", "s058", "s067"}};
        String traitCharacteristic = "MPH";
        
        /* define command line options */
        Option traitCharacteristicOption = new Option("c", "traitCharacteristic", true, "trait characteristic: MPH, GY or HP (default: MPH)");
        Option expressionOption = new Option("e", "minExpression", true, "expression threshold (default: 8.0)");
        Option foldChangeOption = new Option("f", "foldChange", true, "expression fold change threshold (default: 1.3)");
        Option differentialExpressionOption = new Option("d", "differentialExpressionPvalueThreshold", true, "differential expression p-value threshold (default: 0.01)");
        Option fdrOption = new Option("F", "fdr", true, "% FDR for Benjamini-Hochberg multiple testing correction (default: 5)");
        Option databaseUserOption = new Option("u", "databaseUser", true, "MySQL database user");
        Option databasePasswordOption = new Option("p", "databasePassword", true, "password for MySQL database user");
        
        Options commandLineOptions = new Options();
        commandLineOptions.addOption(traitCharacteristicOption);
        commandLineOptions.addOption(differentialExpressionOption);
        commandLineOptions.addOption(expressionOption);
        commandLineOptions.addOption(foldChangeOption);
        commandLineOptions.addOption(fdrOption);
        commandLineOptions.addOption(databaseUserOption);
        commandLineOptions.addOption(databasePasswordOption);
        
        /* parse command line options */
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine;
        
        try {
            commandLine = commandLineParser.parse(commandLineOptions, args);
        
            /* retrieve values and perform plausability check */
            if(commandLine.hasOption("c")) {
                traitCharacteristic = commandLine.getOptionValue("c");
                
                if(!traitCharacteristic.equals("MPH") && !traitCharacteristic.equals("BPH") && !traitCharacteristic.equals("HP")) {
                    throw new ParseException("Trait characteristic needs to bei either MPH, BPH or HP");
                }
                else {
                    switch(traitCharacteristic) {
                        case "BPH":
                            hybridTraitCharacteristic = HybridTraitCharacteristic.BEST_PARENT_HETEROSIS;
                            break;
                        case "HP":
                            hybridTraitCharacteristic = HybridTraitCharacteristic.HYBRID_PERFORMANCE;
                            break;
                        case "MPH":
                            hybridTraitCharacteristic = HybridTraitCharacteristic.MID_PARENT_HETEROSIS;
                            break;                            
                    }
                }
            }
            
            if(commandLine.hasOption("d")) {
                try {
                    differentialExpressionPvalueThreshold = Double.valueOf(commandLine.getOptionValue("d"));
                }
                /* throw exception value is not numeric */
                catch(NumberFormatException e) {
                    throw new ParseException("Differential expression p-value threshold (d) input not numeric");
                }
                
                if((differentialExpressionPvalueThreshold <=0) || (differentialExpressionPvalueThreshold > 1)) {
                    throw new ParseException("Differential expression p-value threshold (d)");
                }
            }
            
            if(commandLine.hasOption("e")) {
                try {
                    expressionThreshold = Double.valueOf(commandLine.getOptionValue("e"));
                }
                /* throw exception value is not numeric */
                catch(NumberFormatException e) {
                    throw new ParseException("Expression threshold (e) input not numeric");
                }
                
                if(expressionThreshold < 0) {
                    throw new ParseException("Expression threshold (e) needs to be >= 0");
                }
            }

            if(commandLine.hasOption("f")) {
                try {
                    expressionFoldChangeThreshold = Double.valueOf(commandLine.getOptionValue("f"));
                }
                /* throw exception value is not numeric */
                catch(NumberFormatException e) {
                    throw new ParseException("Expression fold change threshold (f) input not numeric");
                }
            
                if(expressionFoldChangeThreshold <= 1) {
                    throw new ParseException("Expression fold change threshold (f) needs to be > 1");
                }
            }

            if(commandLine.hasOption("F")) {
                try {
                    falseDiscoveryRate = (Double.valueOf(commandLine.getOptionValue("F")) / 100.0);
                }
                /* throw exception value is not numeric */
                catch(NumberFormatException e) {
                    throw new ParseException("False discovery rate (-F) input not numeric");
                }
                
                if((falseDiscoveryRate <= 0) || (falseDiscoveryRate > 1)) {
                    throw new ParseException("False discovery rate (F) needs to be within limits 0 < F <= 100");
                }
            }
            
            if(commandLine.hasOption("u")) {
                databaseUser = commandLine.getOptionValue("u");
            }
            
            if(commandLine.hasOption("p")) {
                databasePassword = commandLine.getOptionValue("p");
            }
        }
        catch(ParseException e) {
            /* handle command line parser/plausability exceptions */
            System.out.println("Error - " + e.getMessage() + "\n");
            
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("HybridCharacteristicMicroarraySequenceCorrelationDatabase", commandLineOptions);
            
            System.exit(1);
        }
        
        if((databaseUser == null) || (databasePassword == null)) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("HybridCharacteristicMicroarraySequenceCorrelationDatabase", commandLineOptions);

            System.exit(1);
        }
        
        DatabaseLoginData databaseLoginData = new DatabaseLoginData("localhost", "3306", "srna_heterosis", databaseUser, databasePassword);
        
        try {
            BreedingFactorial breedingFactorial = new BreedingFactorial(databaseLoginData, germplasmInbredLines, hybridTraitCharacteristic);
            HybridCharacteristicSequenceCorrelationDatabase hybridTraitCharacteristicSequenceCorrelationDatabase = new HybridCharacteristicSequenceCorrelationDatabase(databaseLoginData, breedingFactorial);
            
            hybridTraitCharacteristicSequenceCorrelationDatabase.testSequences(databaseLoginData, hybridTraitCharacteristic, expressionThreshold, expressionFoldChangeThreshold, differentialExpressionPvalueThreshold);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private final int hybridCount;
    private final int[][] hybridInbredParentIdentifiers;
    
    private final HashMap<Integer,String> inbredTitleHashMap;
 
    private final BreedingFactorial breedingFactorial;
    
    private DatabaseConnection databaseConnection;
    private final DatabaseLoginData databaseLoginData;
}