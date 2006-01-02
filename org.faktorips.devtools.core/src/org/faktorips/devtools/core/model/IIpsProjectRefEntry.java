package org.faktorips.devtools.core.model;

/**
 * An object path entry that defines a reference to another ips project.
 *  
 * @author Jan Ortmann
 */
public interface IIpsProjectRefEntry extends IIpsObjectPathEntry {
    
    /**
     * Returns the ips project being referenced by this entry.
     */
    public IIpsProject getReferencedIpsProject();

}
