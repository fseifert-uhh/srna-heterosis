package de.uni_hamburg.fseifert.sequence_analysis;

public class ReverseComplement {
    public static String getReverseComplement(String sequence) {
        String reverseComplementSequence = new StringBuilder(sequence).reverse().toString();
        
        return reverseComplementSequence.toLowerCase().replace("a", "T").replace("c", "G").replace("g", "C").replace("t", "A");
    }
}
