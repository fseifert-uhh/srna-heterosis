package quantilenormalization;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public class QuantileNormalization {
    public QuantileNormalization(String rawDataFileName, String outputPath) throws IOException {
        this.outputPath = outputPath;
        this.rawDataFileName = rawDataFileName;
        
        splitReadCountTable();
        sortLibraryReadExpression();
        normalizeSortedLibraries();
        exportNormalizedLibraries();
    }

    private void exportNormalizedLibraries() throws IOException {
        System.out.println("reorder and export data");

        for(String libraryTitle : libraryTitles) {
            double[][] normalizedExpressionData = new double[libraryDistinctReadCount][2];

            BufferedReader normalizedDataBufferedReader = new BufferedReader(new FileReader(outputPath + libraryTitle + "_data_expression_sorted_qnorm.tmp"));

            int currentReadPosition = 0;
            String dataLine;
            while((dataLine = normalizedDataBufferedReader.readLine()) != null) {
                String[] data = dataLine.split(separator);
                if(data.length > 1) {
                    normalizedExpressionData[currentReadPosition][0] = Double.valueOf(data[0]);
                    normalizedExpressionData[currentReadPosition][1] = Double.valueOf(data[1]);

                    currentReadPosition++;
                }
            }

            normalizedDataBufferedReader.close();

            Arrays.sort(normalizedExpressionData, new Comparator<double[]>() {
                @Override
                public int compare(double[] a, double[] b) {
                    return (int) (a[0] - b[0]);
                }
            });

            BufferedReader sequenceIdTranslationBufferedReader = new BufferedReader(new FileReader(outputPath + "sequence_id_translation.txt"));

            BufferedWriter normalizedLibraryBufferedWriter = new BufferedWriter(new FileWriter(outputPath + libraryTitle + "_qnorm.csv"));
            
            for(int readIndex = 0; readIndex < normalizedExpressionData.length; readIndex++) {
                String sequenceTitle = sequenceIdTranslationBufferedReader.readLine().split(separator)[1];

                normalizedLibraryBufferedWriter.append(sequenceTitle + separator + normalizedExpressionData[readIndex][1]);
                normalizedLibraryBufferedWriter.newLine();
                normalizedLibraryBufferedWriter.flush();
            }
            
            normalizedLibraryBufferedWriter.close();

            sequenceIdTranslationBufferedReader.close();
        }        
        
        File temporaryFileOutputPath;
        if(outputPath.length() > 0) {
            temporaryFileOutputPath = new File((outputPath.charAt(outputPath.length() - 1) == '/') ? outputPath.substring(0, outputPath.length() - 1) : outputPath);
        }
        else {
            temporaryFileOutputPath = new File("");
        }
         
        File[] temporaryFiles = temporaryFileOutputPath.listFiles();
        if((temporaryFiles != null) && (temporaryFiles.length > 0)) {
            for(File temporaryFile : temporaryFiles) {
                if(temporaryFile.getName().toLowerCase().endsWith("tmp") && !temporaryFile.isDirectory()) {
                    temporaryFile.delete();
                }
            }
        }
    }
    
    private void normalizeSortedLibraries() throws IOException {
        System.out.println("quantile normalization");
        
        BufferedReader[] sortedRawCountBufferedReader = new BufferedReader[libraryTitles.length];
        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            sortedRawCountBufferedReader[libraryIndex] = new BufferedReader(new FileReader(outputPath + libraryTitles[libraryIndex] + "_data_expression_sorted.tmp"));
        }

        BufferedWriter[] sortedNormalizedCountBufferedWriter = new BufferedWriter[libraryTitles.length];
        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            sortedNormalizedCountBufferedWriter[libraryIndex] = new BufferedWriter(new FileWriter(outputPath + libraryTitles[libraryIndex] + "_data_expression_sorted_qnorm.tmp"));
        }

        int[] datasetReadId = new int[libraryTitles.length];
        double[] datasetRawReadCount = new double[libraryTitles.length];

        String dataLine;
        while((dataLine = sortedRawCountBufferedReader[0].readLine()) != null) {
            double meanRawReadCount = 0;

            datasetReadId[0] = Integer.valueOf(dataLine.split(separator)[0]);
            datasetRawReadCount[0] = Double.valueOf(dataLine.split(separator)[1]);
            meanRawReadCount += Double.valueOf(dataLine.split(separator)[1]);

            for(int libraryIndex = 1; libraryIndex < libraryTitles.length; libraryIndex++) {
                if((dataLine = sortedRawCountBufferedReader[libraryIndex].readLine()) != null) {
                    datasetReadId[libraryIndex] = Integer.valueOf(dataLine.split(separator)[0]);
                    datasetRawReadCount[libraryIndex] = Double.valueOf(dataLine.split(separator)[1]);
                    meanRawReadCount += Double.valueOf(dataLine.split(separator)[1]);
                }
            }

            meanRawReadCount /= (double) libraryTitles.length;

            for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
                if(datasetRawReadCount[libraryIndex] > 0) {
                    sortedNormalizedCountBufferedWriter[libraryIndex].append(datasetReadId[libraryIndex] + separator + meanRawReadCount);
                    sortedNormalizedCountBufferedWriter[libraryIndex].newLine();
                    sortedNormalizedCountBufferedWriter[libraryIndex].flush();
                }
                else {
                    sortedNormalizedCountBufferedWriter[libraryIndex].append(datasetReadId[libraryIndex] + separator + "0.0");
                    sortedNormalizedCountBufferedWriter[libraryIndex].newLine();
                    sortedNormalizedCountBufferedWriter[libraryIndex].flush();
                }
            }
        }

        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            sortedNormalizedCountBufferedWriter[libraryIndex].close();
        }

        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            sortedRawCountBufferedReader[libraryIndex].close();
        }
    }
    
    private void sortLibraryReadExpression() throws IOException {
        System.out.println("sort single experiment data");
        
        BufferedReader splitDataBufferedReader;
        
        for(String libraryTitle : libraryTitles) {
            double[][] expressionData = new double[libraryDistinctReadCount][2];
            splitDataBufferedReader = new BufferedReader(new FileReader(outputPath + libraryTitle + "_data.tmp"));
            int currentReadPosition = 0;
            String dataLine;
            while((dataLine = splitDataBufferedReader.readLine()) != null) {
                String[] data = dataLine.split(separator);
                
                expressionData[currentReadPosition][0] = Double.valueOf(data[0]);
                expressionData[currentReadPosition][1] = Double.valueOf(data[1]);
                
                currentReadPosition++;
            }
            splitDataBufferedReader.close();
            Arrays.sort(expressionData, new Comparator<double[]>() {
                @Override
                public int compare(double[] value1, double[] value2) {
                    return (int) (value1[1] - value2[1]);
                }
            });
            BufferedWriter sortedDataBufferedWriter = new BufferedWriter(new FileWriter(outputPath + libraryTitle + "_data_expression_sorted.tmp"));
            for(int readIndex = 0; readIndex < libraryDistinctReadCount; readIndex++) {
                sortedDataBufferedWriter.append((int) expressionData[readIndex][0] + separator + expressionData[readIndex][1]);
                sortedDataBufferedWriter.newLine();
                sortedDataBufferedWriter.flush();
            }
            sortedDataBufferedWriter.close();
        }
    }
    
    private void splitReadCountTable() throws IOException {
        System.out.println("split read count table into single libraries");
        
        BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(rawDataFileName));
            
        String datasetTitleLine = rawDataBufferedReader.readLine();
        libraryTitles = datasetTitleLine.substring(datasetTitleLine.indexOf(separator) + 1).split(separator);

        BufferedWriter sequenceIdTranslationBufferedWriter = new BufferedWriter(new FileWriter(outputPath + "sequence_id_translation.txt"));
        BufferedWriter[] bufferedWriter = new BufferedWriter[libraryTitles.length];
        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            bufferedWriter[libraryIndex] = new BufferedWriter(new FileWriter(outputPath + libraryTitles[libraryIndex] + "_data.tmp"));
        }

        libraryDistinctReadCount = 0;
        String dataLine;
        while((dataLine = rawDataBufferedReader.readLine()) != null) {
            String[] data = dataLine.split(separator);

            sequenceIdTranslationBufferedWriter.append(libraryDistinctReadCount + separator + data[0]);
            sequenceIdTranslationBufferedWriter.newLine();
            sequenceIdTranslationBufferedWriter.flush();

            for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
                bufferedWriter[libraryIndex].append(libraryDistinctReadCount + separator + data[libraryIndex + 1]);
                bufferedWriter[libraryIndex].newLine();
                bufferedWriter[libraryIndex].flush();
            }
            
            libraryDistinctReadCount++;
        }

        for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
            bufferedWriter[libraryIndex].close();
        }

        sequenceIdTranslationBufferedWriter.close();

        rawDataBufferedReader.close();
    }
    
    public static void main(String[] args) {
        String fileName = "";
        String outputPath = "";
        
        if(args.length == 0) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-outputDirectory directory\tthe name of the directory the results should be saved in");
            System.out.println("-fileName\tthe name of the file to be processed");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("outputDirectory")) {
                        outputPath = argumentValue;
                    }
                    else if(argumentTitle.equals("fileName")) {
                        fileName = argumentValue;
                    }
                }
            }
        }

        try {
            QuantileNormalization sequenceLibraryQuantileNormalization = new QuantileNormalization(fileName, outputPath);
        } catch (IOException e) {
            System.out.println("I/O-exception: " + e);
        }
    }
    
    private int libraryDistinctReadCount;
    
    private final String outputPath;
    private final String rawDataFileName;
    private final String separator = "\t";
    private String[] libraryTitles;
}