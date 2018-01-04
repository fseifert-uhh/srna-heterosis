package de.uni_hamburg.fseifert.gff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GFFParser {
    public GFFParser(String fileName) throws IOException {
        gffFile = new File(fileName);
        if(gffFile.exists()) {
            bufferedGFFFileReader = new BufferedReader(new FileReader(fileName));
        }
        else {
            throw new IOException("File \"" + fileName + "\" was not found!");
        }
    }
    
    public void close() throws IOException {
        bufferedGFFFileReader.close();
    }

    public boolean hasNext() throws IOException {
        if(nextGFFDataset != null) {
            return true;
        }
        
        if((nextGFFDataset = next()) != null) {
            return true;
        }
        
        return false;
    }
    
    public boolean hasNext(GFFFilter gffFilter) throws IOException {
        while(hasNext()) {
            GFFDataset currentGFFDataset = next();
            
            if(gffFilter.accept(currentGFFDataset)) {
                nextFilteredGFFDataset = currentGFFDataset;
                
                return true;
            }
        }
        
        return false;
    }
    
    public GFFDataset next() throws IOException {
        if(nextGFFDataset != null) {
            GFFDataset returnGFFDataset = nextGFFDataset;
            nextGFFDataset = null;
            
            return returnGFFDataset;
        }
        else {
            String gffLine;

            if(bufferedGFFFileReader == null) {
                return null;
            }

            do {
                if((gffLine = bufferedGFFFileReader.readLine()) == null) {
                    return null;
                }

            } while(gffLine.isEmpty() || (gffLine.charAt(0) == '#'));

            return (new GFFDataset(gffLine));
        }
    }
    
    public GFFDataset next(GFFFilter gffFilter) throws IOException {
        if(nextFilteredGFFDataset != null) {
            GFFDataset returnGFFDataset = nextFilteredGFFDataset;
            nextFilteredGFFDataset = null;
            
            return returnGFFDataset;
        }
        else {
            while(hasNext(gffFilter)) {
                GFFDataset returnGFFDataset = nextFilteredGFFDataset;
                nextFilteredGFFDataset = null;

                return returnGFFDataset;
            }
        }
        
        return null;
    }
    
    private File gffFile;
    private BufferedReader bufferedGFFFileReader;
    private GFFDataset nextGFFDataset = null;
    private GFFDataset nextFilteredGFFDataset = null;
}
