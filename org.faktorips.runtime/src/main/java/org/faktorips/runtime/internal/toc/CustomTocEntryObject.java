/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    protected CustomTocEntryObject(String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
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
