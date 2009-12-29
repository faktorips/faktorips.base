/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.util.memento.MementoSupport;

/**
 * A container for ips object parts.
 * 
 * @author Thorsten Guenther
 */
public interface IIpsObjectPartContainer extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport,
        MementoSupport, Described {

    /**
     * Returns the ips object this part belongs to if the container is a part, or the ips object
     * itself, if this container is the ips object.
     */
    public IIpsObject getIpsObject();

    /**
     * Returns the ips source file this container belongs to.
     */
    public IIpsSrcFile getIpsSrcFile();

    /**
     * Creates a new IIpsObjectPart of the given type. If the type is not supported, an
     * IllegalArgumentException is thrown.
     * 
     * @param partType The published interface of the ips object part that should be created.
     * @throws IllegalArgumentException if the given partType is not supported.
     */
    public IIpsObjectPart newPart(Class<?> partType);

}
