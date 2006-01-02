package org.faktorips.devtools.core.model;

/**
 * A memento for a source file that stores the contents and the dirty state.
 * <p>
 * This is an application of the memento pattern.
 */
public class IIpsSrcFileMemento {
    
    private IIpsSrcFile file;
    private String contents;
    private boolean dirty;
    
    public IIpsSrcFileMemento(IIpsSrcFile file, String contents, boolean dirty) {
        this.file = file;
        this.contents = contents;
        this.dirty = dirty;
    }
    
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

    public String getContents() {
        return contents;
    }
    
    public boolean isDirty() {
        return dirty;
    }
}
