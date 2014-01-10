/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * Delta entry for a product definition property.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntryForProperty extends IDeltaEntry {

    /**
     * Returns the type of the property this entry refers.
     */
    public ProductCmptPropertyType getPropertyType();

    /**
     * Returns the name of the product definition property this entry relates.
     */
    public String getPropertyName();

}
