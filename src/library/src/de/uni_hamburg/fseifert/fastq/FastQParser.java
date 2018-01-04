package de.uni_hamburg.fseifert.fastq;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class FastQParser {
    public FastQParser(String fastQFileName) throws IOException {
        if(fastQFileName.endsWith("gz")) {
            fastQFileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fastQFileName))));
        }
        else {
            fastQFileReader = new BufferedReader(new FileReader(fastQFileName));
        }
    }

    public FastQDataset getFastQDataset() throws IOException {
        return next();
    }
    
    public FastQDataset next() throws IOException {
        String currentLine;
        String title = "";
        StringBuilder sequence = new StringBuilder();
        StringBuilder qualitySequence = new StringBuilder();

        boolean fastQDatasetProcess = false;
        boolean fastQDatasetSequenceProcess = false;
        boolean fastQDatasetQualitySequenceProcess = false;
        
        if(fastQFileReader == null) {
            return null;
        }
        
        while((currentLine = fastQFileReader.readLine()) != null) {
            if(fastQDatasetProcess == false) {
                if(currentLine.charAt(0) == '@') {
                    title = currentLine.substring(1);
                    fastQDatasetProcess = true;
                }
                else {
                    sequence.delete(0, sequence.length());
                    qualitySequence.delete(0, qualitySequence.length());
                }
            }
            else {
                if(fastQDatasetSequenceProcess == false) {
                    if((currentLine.charAt(0) == '+')) {
                        fastQDatasetSequenceProcess = true;
                    }
                    else {
                        sequence.append(currentLine.trim());
                    }
                }
                else if(fastQDatasetQualitySequenceProcess == false) {
                    qualitySequence.append(currentLine.trim());
                    fastQDatasetQualitySequenceProcess = true;
                    
                    if(!title.isEmpty() && (sequence.length() == qualitySequence.length())) {
                        return new FastQDataset(title, sequence.toString(), qualitySequence.toString());
                    }
                }
                else {
                    break;
                }
            }
        }

        return null;
    }

    
    public void close() throws IOException {
        if(fastQFileReader != null) {
            fastQFileReader.close();
        }
    }
    
    private BufferedReader fastQFileReader = null;
}
