/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * Represents {@link ITypePart ITypeParts} that are able to change over time.
 */
public interface IChangingOverTimeProperty extends ITypePart {

    /**
     * Returns <code>true</code> if every {@link IProductCmptGeneration} may specify a different
     * value for this property, <code>false</code> if the value is the same for all generations.
     */
    boolean isChangingOverTime();

    /**
     * Returns the {@link IProductCmptType} this {@link IChangingOverTimeProperty property} belongs
     * to or null if the referenced {@link IProductCmptType} could not be found.
     * 
     * @throws IpsException if an error occurs during the search
     */
    IProductCmptType findProductCmptType(IIpsProject ipsProject) throws IpsException;

}
