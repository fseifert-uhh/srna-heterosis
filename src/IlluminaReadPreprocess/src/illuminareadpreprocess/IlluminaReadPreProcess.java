package illuminareadpreprocess;

import de.uni_hamburg.fseifert.fastq.FastQDataset;
import de.uni_hamburg.fseifert.fastq.FastQParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IlluminaReadPreProcess {
    public static void main(String[] args) {
        boolean fileEndingFlag = false;
        boolean fileOptionFlag = false;
        
        double qualityThreshold = 99.9;

        int minSequenceLength = 15;
        int maxSequenceLength = 40;
        int phredOffset = 33; // 33/64 for Illumina, 0 for fastQ file assembled from fna/qual-files
        
        String libraryBarcode = "";
        String adapter5Prime = "";
        String adapter3Prime = "";
        String fastqFileDirectory = "";
        String fileEnding = "fastq";
        
        if(args.length == 0) {
            System.out.println("IlluminaReadPreprocess");
            System.out.println("Adapter/Quality trimming of one fastq-file or batch of fastq-files in one directory.");
            System.out.println("Processed files are saved in the directory the fastq-file(s) reside in with suffix '_reads.txt'");
            System.out.println("");
            System.out.println("Usage (input of strings without quotation marks (\"):");
            System.out.println("-adapter3Prime\tthe sequence of the 3-prime adapter (optional for trimming)");
            System.out.println("-adapter5Prime\tthe sequence of the 5-prime adapter (optional for trimming)");
            System.out.println("-directory\tthe directory the files to be processed reside in");
            System.out.println("-fileEnding\tthe file-ending of the fastq files to be processed (default: fastq)");
            System.out.println("-fileName\tthe name of the file to be processed");
            System.out.println("-libraryBarcode\tthe library barcode sequence");
            System.out.println("-maxLength\tthe maximum sequence length (default: 40)");
            System.out.println("-minLength\tthe maximum sequence length (default: 15)");
            System.out.println("-phredOffset\tphred offset for processing (33 or 64 for Illumina, default: 33)");
            System.out.println("-minQuality\tthe minimum quality of the sequencing (in percent, default: 99.9)");
            System.exit(1);
        }
        else {
            for(int argumentIndex = 0; argumentIndex < args.length; argumentIndex++) {
                if(args[argumentIndex].startsWith("-")) {
                    String argumentTitle = args[argumentIndex].substring(1);
                    argumentIndex++;
                    String argumentValue = args[argumentIndex];
                    
                    if(argumentTitle.equals("adapter3Prime")) {
                        adapter3Prime = argumentValue;
                    }
                    else if(argumentTitle.equals("adapter5Prime")) {
                        adapter5Prime = argumentValue;                    
                    }    
                    else if(argumentTitle.equals("directory")) {
                        fastqFileDirectory = argumentValue;
                    }
                    else if(argumentTitle.equals("fileName")) {
                        fileEnding = argumentValue;
                        fileOptionFlag = true;
                    }
                    else if(argumentTitle.equals("fileEnding") && !fileOptionFlag) {
                        fileEnding = argumentValue;
                        fileEndingFlag = true;
                    }
                    else if(argumentTitle.equals("libraryBarcode")) {
                        libraryBarcode = argumentValue;
                    }
                    else if(argumentTitle.equals("maxLength")) {
                        maxSequenceLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("minLength")) {
                        minSequenceLength = Integer.valueOf(argumentValue);
                    }
                    else if(argumentTitle.equals("phredOffset")) {
                        phredOffset = Integer.valueOf(argumentValue);
                    } 
                    else if(argumentTitle.equals("quality")) {
                        qualityThreshold = Double.valueOf(argumentValue);
                    }
                }
            }
        }

        IlluminaReadPreProcess illuminaAnalysis = new IlluminaReadPreProcess(fastqFileDirectory, fileEnding, fileEndingFlag, qualityThreshold, phredOffset, libraryBarcode, adapter5Prime, adapter3Prime, minSequenceLength, maxSequenceLength);
    }
    
    public IlluminaReadPreProcess(String sequenceFilePath, final String fileName, boolean fileEndingFlag, double qualityThreshold, int phredOffset, String libraryBarcodeSequence, String adapter5PrimeSequence, String adapter3PrimeSequence, int minLength, int maxLength) {
        this.qualityThreshold = qualityThreshold;
        this.phredOffset = phredOffset;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.adapter5PrimeSequence = adapter5PrimeSequence;
        this.adapter3PrimeSequence = adapter3PrimeSequence;
        this.libraryBarcodeSequence = libraryBarcodeSequence;

        if(fileEndingFlag) {
            File fastQFilePath = new File(sequenceFilePath);
            File[] fastQFiles = fastQFilePath.listFiles();
            for(int i = 0; i < fastQFiles.length; i++) {
                if(fastQFiles[i].getName().toLowerCase().endsWith(fileName) && !fastQFiles[i].isDirectory()) {
                    this.processIlluminaFastQFile(fastQFiles[i].getAbsolutePath(), fileName);
                }
            }
        }
        else {
            if(fileName.contains(".")) {
                this.processIlluminaFastQFile(sequenceFilePath + fileName, fileName.substring(0, fileName.lastIndexOf(".")));
            }
            else {
                this.processIlluminaFastQFile(sequenceFilePath + fileName, "");
            }
        }
    }
    
    private void processIlluminaFastQFile(String fastQFilename, String fileEnding) {
        try {
            File fastQFile = new File(fastQFilename);

            System.out.print(fastQFilename + " ...");

            String extractedReadsFilename = null;
            if(fastQFilename.contains(".")) {
                extractedReadsFilename = fastQFilename.substring(0 , fastQFilename.lastIndexOf(".")) + "_reads.txt";
            }
            else {
                extractedReadsFilename = fastQFilename + "_reads.txt";
            }            
            
            File extractedReadsOutputFile = new File(extractedReadsFilename);
            FileWriter extractedReadsOutputFileWriter = new FileWriter(extractedReadsOutputFile);
            BufferedWriter bufferedExtractedReadsOutputWriter = new BufferedWriter(extractedReadsOutputFileWriter);

            if(fastQFile.exists()) {
                ArrayList<String> sequenceReadList = new ArrayList<String>();
                
                FastQParser fastQParser = new FastQParser(fastQFilename);
                FastQDataset currentFastQDataset;
                while((currentFastQDataset = fastQParser.getFastQDataset()) != null) {
                    String currentSequence = currentFastQDataset.getSequence();
                    String currentQualitySequence = currentFastQDataset.getQualitySequence();
                    if(!libraryBarcodeSequence.isEmpty()) {
                        SequenceAdapterTrim libraryBarcodeSelectionTrim = new SequenceAdapterTrim(currentSequence, currentQualitySequence, libraryBarcodeSequence, "");                    
                        currentSequence = libraryBarcodeSelectionTrim.getAdapterTrimmedSequence();
                        currentQualitySequence = libraryBarcodeSelectionTrim.getAdapterTrimmedQuality();
                    }
                    
                    SequenceAdapterTrim sequenceAdapterTrim = new SequenceAdapterTrim(currentSequence, currentQualitySequence, adapter5PrimeSequence, adapter3PrimeSequence);
                    SequenceQualityTrim sequenceQualityTrim = new SequenceQualityTrim(sequenceAdapterTrim.getAdapterTrimmedSequence(), sequenceAdapterTrim.getAdapterTrimmedQuality(), qualityThreshold, phredOffset);
                    String trimmedSequence = sequenceQualityTrim.getQualityTrimmedSequence();
                    if((trimmedSequence.length() >= minLength) && (trimmedSequence.length() <= maxLength)) {
                        bufferedExtractedReadsOutputWriter.write(trimmedSequence);
                        bufferedExtractedReadsOutputWriter.newLine();
                        bufferedExtractedReadsOutputWriter.flush();
                    }    
                }
                fastQParser.close();

                bufferedExtractedReadsOutputWriter.close();
                extractedReadsOutputFileWriter.close();
            }
            else {
                System.out.println("file does not exist!");
            }
            
            System.out.println(" processed");
        } catch(IOException e) {
            System.out.println("I/O-error: " + e);
        }
    }
    
    private final double qualityThreshold;
        
    private final int minLength;
    private final int maxLength;
        
    private final int phredOffset;

    private final String adapter5PrimeSequence;
    private final String adapter3PrimeSequence;
    private final String libraryBarcodeSequence;
}