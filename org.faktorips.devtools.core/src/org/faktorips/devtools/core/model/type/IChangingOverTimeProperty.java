/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.type;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * Represents {@link ITypePart}s that are able to change over time.
 */
public interface IChangingOverTimeProperty extends ITypePart {

    /**
     * Returns <code>true</code> if every {@link IProductCmptGeneration} may specify a different
     * value for this property, <code>false</code> if the value is the same for all generations.
     */
    public boolean isChangingOverTime();

    /**
     * Returns the {@link IProductCmptType} this {@link IChangingOverTimeProperty property} belongs
     * to. If the property belongs to an {@link IPolicyCmptType} the {@link IProductCmptType} that
     * configures the {@link IPolicyCmptType} is returned.
     */
    public IProductCmptType getProductCmptType();

}
