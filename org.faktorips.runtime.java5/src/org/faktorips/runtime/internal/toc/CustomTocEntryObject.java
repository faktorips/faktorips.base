/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.toc;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * A TOC entry for a custom type of runtime object. The runtime object corresponds to a
 * IpsObjectType at design time.
 * 
 * @author schwering
 */
public abstract class CustomTocEntryObject<T> extends TocEntryObject {

    protected CustomTocEntryObject(String ipsObjectQualifiedName, String xmlResourceName, String implementationClassName) {
        super("", ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    /**
     * Returns the object identified by this TOC entry.
     * 
     * @param repository the repository used to find the object
     * @return the object identified by this TOC entry
     */
    public abstract T createRuntimeObject(IRuntimeRepository repository);

    /**
     * The class of the objects referenced by this TOC entry.
     * 
     * @return the class of the objects referenced by this TOC entry
     */
    public abstract Class<T> getRuntimeObjectClass();

    /**
     * Returns the Id of the IpsObjectType for which this TOC entry was created.
     * 
     * @return the Id of the IpsObjectType for which this TOC entry was created
     */
    public abstract String getIpsObjectTypeId();

}
