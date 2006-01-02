package org.faktorips.devtools.core.model.pctype;

import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * Brauchen wir nicht. Jan
 */
public interface IMember extends IIpsObjectPart, Described {
    
    /**
     * Sets the member's name.
     */
    public void setName(String newName);
    
}
