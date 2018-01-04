package permillionnormalization;

import java.io.*;

public class PerMillionNormalization {
    public PerMillionNormalization(String rawDataFileName, String outputPath, String separator) throws IOException {
        this.outputPath = outputPath;
        this.rawDataFileName = rawDataFileName;
        this.separator = separator;
        
        determineLibraryReadCount();
        exportNormalizedLibraries();
    }

    private void exportNormalizedLibraries() throws IOException {
        System.out.println("export data ...");

        BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(rawDataFileName));

        String outputFileName = rawDataFileName.replace(".", "_per_million.");
        outputFileName = outputFileName.substring(outputFileName.lastIndexOf("/"));
        BufferedWriter normalizedPerMillionLibraryBufferedWriter = new BufferedWriter(new FileWriter(outputPath + outputFileName));
            
        normalizedPerMillionLibraryBufferedWriter.append(rawDataBufferedReader.readLine());
        normalizedPerMillionLibraryBufferedWriter.newLine();
        normalizedPerMillionLibraryBufferedWriter.flush();
        
        String dataLine;
        while((dataLine = rawDataBufferedReader.readLine()) != null) {
            String[] data = dataLine.split(separator);

            for(int dataIndex = 0; dataIndex < data.length; dataIndex++) {
                if(dataIndex == 0) {
                    normalizedPerMillionLibraryBufferedWriter.append(data[0]);
                }
                else {
                    normalizedPerMillionLibraryBufferedWriter.append(separator + String.valueOf(Double.valueOf(data[dataIndex]) / (libraryTotalReadCount[dataIndex - 1] / 1000000)));
                }
            }
            
            normalizedPerMillionLibraryBufferedWriter.newLine();
            normalizedPerMillionLibraryBufferedWriter.flush();
        }        

        normalizedPerMillionLibraryBufferedWriter.close();
    }
    
    private void determineLibraryReadCount() throws IOException {
        System.out.println("determining library total read counts ...");
        
        BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(rawDataFileName));
            
        String datasetTitleLine = rawDataBufferedReader.readLine();
        libraryTitles = datasetTitleLine.substring(datasetTitleLine.indexOf(separator) + 1).split(separator);
        libraryTotalReadCount = new double[libraryTitles.length];
        
        String dataLine;
        while((dataLine = rawDataBufferedReader.readLine()) != null) {
            String[] data = dataLine.split(separator);
            
            for(int libraryIndex = 0; libraryIndex < libraryTitles.length; libraryIndex++) {
                libraryTotalReadCount[libraryIndex] += Double.valueOf(data[libraryIndex + 1]);
            }
        }
    }
    
    public static void main(String[] args) {
        String fileName = "";
        String outputPath = "";
        String separator = " ";
        
        if(args.length == 0) {
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-outputDirectory directory\tthe name of the directory the results should be saved in");
            System.out.println("-fileName\tthe name of the file to be processed");
            System.out.println("-separator {space|tab|semicolon|comma} (optional, default: space)");
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
                    else if(argumentTitle.equals("separator")) {
                        if(argumentValue.equals("comma")) {
                            separator = ",";
                        }
                        else if(argumentValue.equals("semicolon")) {
                            separator = ";";
                        }
                        else if(argumentValue.equals("space")) {
                            separator = " ";
                        }
                        else if(argumentValue.equals("tab")) {
                            separator = "\t";
                        }                        
                    }                    
                }
            }
        }

        try {
            PerMillionNormalization sequenceLibraryQuantileNormalization = new PerMillionNormalization(fileName, outputPath, separator);
        } catch (IOException e) {
            System.out.println("I/O-exception: " + e);
        }
    }
    
    private double[] libraryTotalReadCount;

    private final String outputPath;
    private final String rawDataFileName;
    private final String separator;
    private String[] libraryTitles;
}