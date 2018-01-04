package de.uni_hamburg.fseifert.fasta;

import java.io.*;

public class FastaParser {
    public FastaParser(String fastaFileName) throws IOException {
        this(fastaFileName, 4096);
    }
    
    public FastaParser(String fastaFileName, int sequenceFragmentLength) throws IOException {
        this.fastaFileName = fastaFileName;
        this.sequenceFragmentLength = sequenceFragmentLength;
        this.sequenceFragmentPosition = 0;
        
        File fastaFile = new File(fastaFileName);

        residualSequence = new StringBuilder();

        if(fastaFile.exists()) {
            fastaFileInputStream = new FileInputStream(fastaFileName);
            fastaLineBuffer = new BufferedReader(new InputStreamReader(fastaFileInputStream));
        }
        else {
            System.out.println("Error: file " + fastaFileName + " does not exist");
        }
    }
    
    public void close() throws IOException {
        fastaLineBuffer.close();
        fastaFileInputStream.close();
    }
    
    public String nextSequenceFragment() throws IOException {
        return nextSequenceFragment(sequenceFragmentLength);
    }

    public String nextSequenceFragment(int sequenceFragmentLength) throws IOException {
        String sequenceFragment;
        
        String fastaLine;
        
        while((residualSequence.length() < sequenceFragmentLength) && !sequenceEndReached) {
            if(((fastaLine = fastaLineBuffer.readLine()) != null)) {
                if((fastaLine.trim().length() > 0) && (fastaLine.charAt(0) != ';')) {
                    if(fastaLine.charAt(0) == '>') {
                        sequenceEndReached = true;
                        nextSequenceReached = true;
                        nextSequenceTitle = fastaLine.trim().substring(1);
                    }
                    else {
                        residualSequence.append(fastaLine.trim().replace(" ", ""));
                    }
                }
            }
            else {
                sequenceEndReached = true;
            }
        }
        
        if(residualSequence.length() > sequenceFragmentLength) {
            sequenceFragment = residualSequence.substring(0, sequenceFragmentLength);
            residualSequence.delete(0, sequenceFragmentLength);
        }
        else {
            sequenceFragment = residualSequence.toString();
            residualSequence.delete(0, residualSequence.length());
        }
        
        sequenceFragmentPosition += sequenceFragment.length();
        
        return sequenceFragment;
    }
    
    public String getSequenceTitle() {
        return sequenceTitle;
    }
    
    public boolean nextSequence() throws IOException {
        String fastaLine;
        
        sequenceFragmentPosition = 0;

        if(nextSequenceReached) {
            nextSequenceReached = false;
            sequenceEndReached = false;

            sequenceTitle = nextSequenceTitle.replace(">", "");
            nextSequenceTitle = "";
            
            return true;
        }
        else {
            residualSequence.delete(0, residualSequence.length());

            if(fastaLineBuffer != null) {
                while((fastaLine = fastaLineBuffer.readLine()) != null) {
                    if((fastaLine.trim().length() > 0) && (fastaLine.charAt(0) == '>')) {
                        sequenceTitle = fastaLine.trim().replace(">", "");

                        sequenceEndReached = false;

                        return true;
                    }
                }


                if(fastaLine == null) {
                    sequenceEndReached = true;
                }
            }
        }
        
        return false;
    }
    
    public String reverseComplement(String sequence) {
        if(sequence == null) {
            return null;
        }
        
        StringBuilder sequenceBuffer = new StringBuilder(sequence);
        String reverseComplement = sequenceBuffer.reverse().toString().replace('A', 'T').replace('C', 'G').replace('G', 'C').replace('T', 'A').replace('a', 't').replace('c', 'g').replace('g', 'c').replace('t', 'a');
        
        return reverseComplement;
    }
    
    public void setSequenceFragmentLenght(int fragmentLength) {
        sequenceFragmentLength = fragmentLength;
    }
    
    public boolean setSequenceFragmentPosition(long position) throws IOException {
        position--;
        
        if(sequenceFragmentPosition > position) {
            File fastaFile = new File(fastaFileName);

            residualSequence = new StringBuilder();

            if((fastaFile != null) && fastaFile.exists()) {
                fastaLineBuffer.close();
                fastaFileInputStream.close();
                
                fastaFileInputStream = new FileInputStream(fastaFileName);
                fastaLineBuffer = new BufferedReader(new InputStreamReader(fastaFileInputStream));
            }
        }
        
        int tempSequenceFragmentLength = sequenceFragmentLength;
        sequenceFragmentLength = 100000;
        while(sequenceFragmentPosition < position) {
            if(sequenceFragmentLength > (position - sequenceFragmentPosition)) {
                sequenceFragmentLength = (int) (position - sequenceFragmentPosition);
            }
            
            String sequence = this.nextSequenceFragment(sequenceFragmentLength);
            if(sequence == null) {
                return false;
            }
        }
        sequenceFragmentLength = tempSequenceFragmentLength;

        return true;
    }
    
    private boolean nextSequenceReached = false;
    private boolean sequenceEndReached = false;

    private int sequenceFragmentLength = 0;
    
    private long sequenceFragmentPosition = 0;
    
    private BufferedReader fastaLineBuffer;
    private FileInputStream fastaFileInputStream;
    
    private String fastaFileName;
    private String nextSequenceTitle;
    private String sequenceTitle;

    private StringBuilder residualSequence;
}