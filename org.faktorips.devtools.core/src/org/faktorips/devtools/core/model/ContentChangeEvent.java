package org.faktorips.devtools.core.model;

/**
 * An event for content changes. 
 */
public class ContentChangeEvent {

    private IIpsSrcFile pdSrcFile;
    
    public ContentChangeEvent(IIpsSrcFile pdSrcFile) {
        this.pdSrcFile = pdSrcFile;
    }

    /**
     * Returns the source file which contents has changed.
     */
    public IIpsSrcFile getPdSrcFile() {
        return pdSrcFile;
    }
    
}
