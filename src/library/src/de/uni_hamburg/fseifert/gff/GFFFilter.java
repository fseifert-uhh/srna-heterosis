package de.uni_hamburg.fseifert.gff;

public class GFFFilter {
    public GFFFilter() {}
    
    public boolean accept(GFFDataset gffDataset) {
        if(filterAttributeId || filterAttributeName || filterAttributeParent || filterAttributeBiotype) {
            boolean filterAttributeIdMatch = false;
            boolean filterAttributeNameMatch = false;
            boolean filterAttributeParentMatch = false;
            boolean filterAttributeBiotypeMatch = false;
            
            String[] attributeData = gffDataset.getAttributes().split(";");
            for(int i = 0; i < attributeData.length; i++) {
                if(filterAttributeId && attributeData[i].equals("ID=" + attributeId)) {
                    filterAttributeIdMatch = true;
                }

                if(filterAttributeName && attributeData[i].equals("Name=" + attributeName)) {
                    filterAttributeNameMatch = true;
                }

                if(filterAttributeParent && attributeData[i].equals("Parent=" + attributeParent)) {
                    filterAttributeParentMatch = true;
                }

                if(filterAttributeBiotype && attributeData[i].equals("biotype=" + attributeBiotype)) {
                    filterAttributeBiotypeMatch = true;
                }
            }
            
            if((filterAttributeId && !filterAttributeIdMatch) || (filterAttributeName && !filterAttributeNameMatch) || (filterAttributeParent && !filterAttributeParentMatch) || (filterAttributeBiotype && !filterAttributeBiotypeMatch)) {
                return false;
            }
        }

        try {
            if(filterSequenceId && !gffDataset.getSequenceId().equals(sequenceId)) {
                return false;
            }
        }
        catch(NullPointerException e) {
            return false;
        }
        
        if(filterSource && !gffDataset.getSource().equals(source)) {
            return false;
        }
        
        if(filterSequenceEndDistance) {
            long sequenceEndAnalysisWindowStart;
            long sequenceEndAnalysisWindowEnd;
            
            if(sequenceEndDistance != 0) {
                sequenceEndAnalysisWindowStart = (sequenceEndPosition + (sequenceEndDistance - 1));
                sequenceEndAnalysisWindowEnd = (sequenceEndAnalysisWindowStart + (sequenceEndWindowSize - 1));
            }
            else {
                sequenceEndAnalysisWindowStart = sequenceEndPosition;
                sequenceEndAnalysisWindowEnd = (sequenceEndPosition + (sequenceEndDistance - 1));
            }
            
            if((gffDataset.getSequenceEnd() < sequenceEndAnalysisWindowStart) || (gffDataset.getSequenceStart() > sequenceEndAnalysisWindowEnd)) {
                return false;
            }
        }

        if(filterSequenceStartDistance) {
            long sequenceStartAnalysisWindowStart;
            long sequenceStartAnalysisWindowEnd;
            
            if(sequenceStartDistance != 0) {
                sequenceStartAnalysisWindowStart = (sequenceStartPosition + (sequenceStartDistance - 1));
                sequenceStartAnalysisWindowEnd = (sequenceStartAnalysisWindowStart + (sequenceStartWindowSize - 1));
            }
            else {
                sequenceStartAnalysisWindowStart = sequenceStartPosition;
                sequenceStartAnalysisWindowEnd = (sequenceStartPosition + (sequenceStartDistance - 1));
            }
            
            if((gffDataset.getSequenceEnd() < sequenceStartAnalysisWindowStart) || (gffDataset.getSequenceStart() > sequenceStartAnalysisWindowEnd)) {
                return false;
            }
        }
        
        if(filterSequenceType && !gffDataset.getSequenceType().equals(sequenceType)) {
            return false;
        }
        
        if(filterSequenceStrand && (gffDataset.getSequenceStrand() != '.') && (gffDataset.getSequenceStrand() != sequenceStrand)) {
            return false;
        }
        
        return true;
    }
    
    public boolean contains(GFFDataset gffDataset, String query) {
        return gffDataset.getGFFString().contains(query);
    }
    
    public void setAttributeBiotypeFilter(String biotype) {
        this.filterAttributeBiotype = true;
        this.attributeBiotype = biotype;
    }    

    public void setAttributeIdFilter(String attributeId) {
        this.attributeId = attributeId;
        this.filterAttributeId = true;
    }
    
    public void setAttributeNameFilter(String attributeName) {
        this.attributeName = attributeName;
        this.filterAttributeName = true;
    }    
    
    public void setAttributeParentFilter(String attributeParent) {
        this.attributeParent = attributeParent;
        this.filterAttributeParent = true;
    }

    public void setSequenceEndFilter(long sequenceEndPosition, long sequenceEndDistance, long sequenceEndWindowSize) {
        this.filterSequenceEndDistance = true;
        this.sequenceEndDistance = sequenceEndDistance;
        this.sequenceEndPosition = sequenceEndPosition;
        this.sequenceEndWindowSize = sequenceEndWindowSize;
    }

    public void setSequenceIdFilter(String sequenceId) {
        this.filterSequenceId = true;
        this.sequenceId = sequenceId;
    }    
    
    public void setSequenceStartFilter(long sequenceStartPosition, long sequenceStartDistance, long sequenceStartWindowSize) {
        this.filterSequenceStartDistance = true;
        this.sequenceStartDistance = sequenceStartDistance;
        this.sequenceStartPosition = sequenceStartPosition;
        this.sequenceStartWindowSize = sequenceStartWindowSize;
    }
    
    public void setSequenceStrandFilter(char strand) {
        this.filterSequenceStrand = true;
        this.sequenceStrand = strand;
    }
    
    public void setSequenceTypeFilter(String sequenceType) {
        this.filterSequenceType = true;
        this.sequenceType = sequenceType;
    }
    
    public void setSourceFilter(String source) {
        this.filterSource = true;
        this.source = source; 
    }
    
    private boolean filterSequenceId = false;
    private boolean filterSource = false;
    private boolean filterSequenceType = false;
    private boolean filterSequenceStartDistance = false;
    private boolean filterSequenceEndDistance = false;
    private boolean filterSequenceStrand = false;
    private boolean filterAttributeId = false;
    private boolean filterExactAttributeId = false;
    private boolean filterAttributeName = false;
    private boolean filterExactAttributeName = false;
    private boolean filterAttributeBiotype = false;
    private boolean filterAttributeParent = false;
    private boolean filterExactAttributeParent = false;

    private char sequenceStrand;

    private long sequenceEndDistance;
    private long sequenceEndPosition;
    private long sequenceEndWindowSize;
    private long sequenceStartDistance;
    private long sequenceStartPosition;
    private long sequenceStartWindowSize;
    
    private GFFDataset gffDataset;

    private String attributeBiotype;
    private String attributeId;
    private String attributeName;
    private String attributeParent;
    private String sequenceId;
    private String sequenceType;
    private String source;
}
