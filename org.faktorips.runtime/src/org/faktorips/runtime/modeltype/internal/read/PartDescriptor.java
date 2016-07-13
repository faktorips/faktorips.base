/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal.read;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.ModelType;

/**
 * A descriptor for a part which type is specified by the generic type T.
 * <p>
 * To be totally type safe we would need to specify the generic subtype of {@link ModelType}. But
 * this leads to much more generic overhead for little use.
 * 
 */
public abstract class PartDescriptor<T extends IModelElement> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Is called by the collector to create a part of type T using the previously collected
     * information.
     * 
     * @param parentElement The parent of the part that should be created
     * @return The newly created part
     */
    public abstract T create(IModelElement parentElement);

}
