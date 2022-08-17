/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.List;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
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
    List<TocEntryObject> createTocEntries(IIpsObject ipsObject);

    /**
     * Returns the {@link IpsObjectType} this builder can create TOC entries for.
     * 
     * @return the {@link IpsObjectType} this builder is responsible for.
     */
    IpsObjectType getIpsObjectType();
}
