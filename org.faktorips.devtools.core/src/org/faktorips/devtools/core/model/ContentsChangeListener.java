package org.faktorips.devtools.core.model;

/**
 * A listener that listens to changes in source files. 
 * 
 * @author Jan Ortmann
 */
public interface ContentsChangeListener {

    /**
     * Notifies the listener that an object has changed.
     */
    public void contentsChanged(ContentChangeEvent event);
    
}
