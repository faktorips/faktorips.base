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

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This {@link TocEntryObject} represents an entry for model types
 * 
 * @author dirmeier
 */
public abstract class ModelTypeTocEntry extends TocEntryObject {

    public ModelTypeTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, IpsStringUtils.EMPTY, implementationClassName);
    }
}
