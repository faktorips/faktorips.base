package org.faktorips.devtools.core.model;

import org.faktorips.util.memento.MementoSupport;


/**
 * A container for ips object parts.
 * 
 * @author Thorsten Guenther
 */
public interface IIpsObjectPartContainer extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport, MementoSupport, Described {

    /**
     * Creates a new IIpsObjectPart of the given type. If the type is not supported, an 
     * IllegalArgumentException is thrown.
     * 
     * @param partType The published interface of the ips object part that should be created.
     * @throws IllegalArgumentException if the given partType is not supported.
     */
    public IIpsObjectPart newPart(Class partType);

}
