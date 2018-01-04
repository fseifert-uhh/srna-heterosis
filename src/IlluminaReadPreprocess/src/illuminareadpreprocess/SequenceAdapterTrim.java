package illuminareadpreprocess;

public class SequenceAdapterTrim {
    public SequenceAdapterTrim(String sequence, String quality, String adapter5Prime, String adapter3Prime) {
        this.adapter5Prime = adapter5Prime;
        this.adapter3Prime = adapter3Prime;        
        this.quality = quality;
        this.sequence = sequence;
        
        this.trim5Prime = -1;
        this.trim3Prime = sequence.length();
        
        trimAdapter();
    }

    public String getAdapter5Prime() {
        return adapter5Prime;
    }

    public String getAdapter3Prime() {
        return adapter3Prime;
    }
    
    private void trimAdapter() {
        if(!adapter5Prime.isEmpty()) {
            LocalAlignment adapterAlignment5Prime = new LocalAlignment(sequence, adapter5Prime);
            if(adapterAlignment5Prime.alignmentStartPosition() >= 0) {
                trim5Prime = adapterAlignment5Prime.alignmentEndPosition();
            }
        }
        if(!adapter3Prime.isEmpty()) {
            LocalAlignment adapterAlignment3Prime = new LocalAlignment(sequence, adapter3Prime);
            if(adapterAlignment3Prime.alignmentStartPosition() >= 0) {
                trim3Prime = (adapterAlignment3Prime.alignmentStartPosition() + 1);
            }
        }
    }
    
    public String getAdapterTrimmedSequence() {
        if(trim5Prime < (trim3Prime - 1)) {
            return sequence.substring((trim5Prime + 1), trim3Prime);
        }

        return "";
    }
    
    public String getAdapterTrimmedQuality() {
        if(trim5Prime < (trim3Prime - 1)) {
            return quality.substring((trim5Prime + 1), trim3Prime);
        }

        return "";
    }
    
    private int trim5Prime;
    private int trim3Prime;
        
    private final String adapter5Prime;
    private final String adapter3Prime;
    private final String quality;
    private final String sequence;
    private String trimmedSequence;
}