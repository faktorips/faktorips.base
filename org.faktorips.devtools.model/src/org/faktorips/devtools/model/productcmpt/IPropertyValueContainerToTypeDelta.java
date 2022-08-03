/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * A product component generation /policy component type delta describes the difference between what
 * a product component generation based on specific product component type should contain and what
 * it actually contains.
 * 
 * @author Jan Ortmann
 */
public interface IPropertyValueContainerToTypeDelta extends IFixDifferencesComposite {

    /**
     * Returns the product component generation this delta was computed for.
     */
    IPropertyValueContainer getPropertyValueContainer();

    /**
     * Returns the product component type this delta was computed for.
     */
    IProductCmptType getProductCmptType();

    /**
     * Returns the delta entries that describe the delta details. Each entry reports a difference
     * between the generation and the product component type.
     */
    IDeltaEntry[] getEntries();

    /**
     * Returns all entries for the given type.
     */
    IDeltaEntry[] getEntries(DeltaType type);

}
