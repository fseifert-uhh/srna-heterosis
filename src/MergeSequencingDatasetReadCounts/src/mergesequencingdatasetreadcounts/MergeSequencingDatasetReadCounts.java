package mergesequencingdatasetreadcounts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

public class MergeSequencingDatasetReadCounts {
    public static void main(String[] args) {
        String fileEnding = "txt";
        String fileNamePrefix = "";
        String fileNameSuffix = "";
        String outputFileName = "merged_sequence_count_data.csv";
        String path = "";
        
        if(args.length == 0) {
            System.out.println("Usage:");
            System.out.println("-directory <path>\tthe path the files to be processed reside in");
            System.out.println("-fileEnding <ending>\tthe file-ending (csv, txt ...) of the files to be processed (default: txt)");
            System.out.println("-output <filename>\tname of the output file (default: merged_sequence_count_data.csv");
            System.out.println("-prefix <path>\tbeginning of the filename to be trimmed");
            System.out.println("-suffix <path>\tending of the filename to be trimmed");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("directory")) {
                        path = argumentValue;
                    }
                    if(argumentTitle.equals("fileEnding")) {
                        fileEnding = argumentValue;
                    }
                    else if(argumentTitle.equals("output")) {
                        outputFileName = argumentValue;                    
                    }    
                    else if(argumentTitle.equals("prefix")) {
                        fileNamePrefix = argumentValue;
                    }
                    else if(argumentTitle.equals("suffix")) {
                        fileNameSuffix = argumentValue;
                    }
                }
            }
        }
        
        MergeSequencingDatasetReadCounts mergeSequencingDatasetReadCounts = new MergeSequencingDatasetReadCounts(path, fileEnding, fileNamePrefix, fileNameSuffix, outputFileName);
    }

    public MergeSequencingDatasetReadCounts(String filePath, String fileEnding, String fileNamePrefix, String fileNameSuffix, String outputFileName) {
        String separator = "\t";
        
        String newDatasetSpacer = "";
        String header = "sequence";
        
        try {
            boolean initializeFlag = false;
            
            File readCountFilePath = new File(filePath);
            File[] readCountFiles = readCountFilePath.listFiles();
            ArrayList<String> fileNamesArrayList = new ArrayList();
            
            if(readCountFiles != null) {
                for(File readCountFile : readCountFiles) {
                    if(readCountFile.getName().toLowerCase().endsWith(fileEnding) && !readCountFile.isDirectory() && !readCountFile.equals(outputFileName)) {
                        if((fileNamePrefix != null) && !readCountFile.getName().toLowerCase().startsWith(fileNamePrefix.toLowerCase())) {
                            continue;
                        }
                        if((fileNameSuffix != null) && !readCountFile.getName().toLowerCase().replace("." + fileEnding, "").endsWith(fileNameSuffix.toLowerCase())) {
                            continue;
                        }
                        
                        fileNamesArrayList.add(readCountFile.getName());
                    }
                }
            }
            
            String[] fileNames = new String[fileNamesArrayList.size()];
            for(int arrayIndex = 0; arrayIndex < fileNamesArrayList.size(); arrayIndex++) {
                fileNames[arrayIndex] = fileNamesArrayList.get(arrayIndex);
            }
            
            Arrays.sort(fileNames);
            
            for(String fileName : fileNames) {
                String datasetTitle = fileName.replace(fileNamePrefix, "").replace("." + fileEnding, "").replace(fileNameSuffix, "");
                header += separator + datasetTitle;

                if(!initializeFlag) {
                    fileCopy(filePath + fileName, filePath + outputFileName);
                    initializeFlag = true;
                    
                    continue;
                }

                mergedDatasetFileReader = new FileReader(filePath + outputFileName);
                mergedDatasetBufferedReader = new BufferedReader(mergedDatasetFileReader);
                datasetFileReader = new FileReader(filePath + fileName);
                datasetBufferedReader = new BufferedReader(datasetFileReader);

                String datasetBuffer = datasetBufferedReader.readLine();
                String mergedDatasetBuffer = mergedDatasetBufferedReader.readLine();
                
                newDatasetSpacer += separator + "0";

                fileWriter = new FileWriter(filePath + "temp");
                bufferedWriter = new BufferedWriter(fileWriter);

                if(fileName.equals(fileNames[fileNames.length - 1])) {
                    bufferedWriter.write(header);
                    bufferedWriter.newLine();
                }

                do {
                    if(mergedDatasetBuffer == null) {
                        String[] datasetParts = datasetBuffer.split(separator);
                        bufferedWriter.write(datasetParts[0] + newDatasetSpacer + separator + datasetParts[1]);
                        datasetBuffer = datasetBufferedReader.readLine();
                    }
                    else if(datasetBuffer == null) {
                        bufferedWriter.write(mergedDatasetBuffer + separator + "0");
                        mergedDatasetBuffer = mergedDatasetBufferedReader.readLine();
                    }
                    else {
                        if(datasetBuffer.split(separator).length > 2) {
                            continue;
                        }
                
                        String[] datasetParts = datasetBuffer.split(separator);
                        String[] mergedDatasetParts = {mergedDatasetBuffer.split(separator)[0], mergedDatasetBuffer.substring(mergedDatasetBuffer.indexOf(separator) + 1)};

                        int datasetCompare = datasetParts[0].compareTo(mergedDatasetParts[0]);
                        if(datasetCompare == 0) {
                            bufferedWriter.write(mergedDatasetBuffer + separator + datasetParts[1]);
                            datasetBuffer = datasetBufferedReader.readLine();
                            mergedDatasetBuffer = mergedDatasetBufferedReader.readLine();                        
                        }
                        else if(datasetCompare < 0) {
                            if(datasetParts.length < 2) {
                                System.out.println(datasetBuffer);
                            }
                            bufferedWriter.write(datasetParts[0] + newDatasetSpacer + separator + datasetParts[1]);
                            datasetBuffer = datasetBufferedReader.readLine();
                        }
                        else if(datasetCompare > 0) {
                            bufferedWriter.write(mergedDatasetBuffer + separator + "0");
                            mergedDatasetBuffer = mergedDatasetBufferedReader.readLine();                        
                        }
                    }
                    bufferedWriter.newLine();
                } while((mergedDatasetBuffer != null) || (datasetBuffer != null));

                bufferedWriter.close();
                fileWriter.close();
                datasetBufferedReader.close();
                datasetFileReader.close();
                mergedDatasetBufferedReader.close();
                mergedDatasetFileReader.close();

                fileCopy(filePath + "temp", filePath + outputFileName);
            }
            
            File deleteTempFile = new File(filePath + "temp");
            deleteTempFile.delete();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    private void fileCopy(String inputName, String copyName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(inputName);
            FileOutputStream fileOutputStream = new FileOutputStream(copyName);
            
            byte[] buffer = new byte[0xFFFF];
            int bufferLength;
            while((bufferLength = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            
            fileInputStream.close();
            fileOutputStream.close();
        }
        catch (IOException e ) {
            System.out.println(e);
        }
    }
    
    private BufferedReader mergedDatasetBufferedReader;
    private BufferedReader datasetBufferedReader;
    private BufferedWriter bufferedWriter;
    private FileReader mergedDatasetFileReader;
    private FileReader datasetFileReader;
    private FileWriter fileWriter;
}
