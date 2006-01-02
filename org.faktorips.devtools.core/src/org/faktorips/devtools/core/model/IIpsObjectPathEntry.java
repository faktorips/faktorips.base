package org.faktorips.devtools.core.model;

/**
 * An entry in an IPS object path.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathEntry {
    
    /**
     * Type constant indicating a source folder entry.
     */
    public final static String TYPE_SRC_FOLDER = "src"; 
    
    /**
     * Type constant indicating a project reference entry.
     */
    public final static String TYPE_PROJECT_REFERENCE = "project";
    
    /**
     * Returns the object path this is an entry of.
     */
    public IIpsObjectPath getIpsObjectPath();
    
    /**
     * Returns the type of this entry as one of the type constant defined in this interface.
     */
    public String getType();

}
