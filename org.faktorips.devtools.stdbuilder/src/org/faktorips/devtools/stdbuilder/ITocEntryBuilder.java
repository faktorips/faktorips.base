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

package org.faktorips.devtools.stdbuilder;

import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.internal.toc.TocEntryObject;

/**
 * Interface for custom builders that create entries for the table of contents (TOC) file. Use the
 * extension point "org.faktorips.devtools.stdbuilder.tocEntryBuilderFactory" to register a factory
 * that creates instances of this interface.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface ITocEntryBuilder {

    /**
     * Creates a number of TOC entries for the given ipsObject.
     * 
     * @param ipsObject the {@link IIpsObject} to create TOC entries for.
     * @return a list of {@link TocEntryObject}s.
     */
    public List<TocEntryObject> createTocEntries(IIpsObject ipsObject);

    /**
     * Returns the {@link IpsObjectType} this builder can create TOC entries for.
     * 
     * @return the {@link IpsObjectType} this builder is responsible for.
     */
    public IpsObjectType getIpsObjectType();
}
