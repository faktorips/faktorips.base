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

/**
 * Delta entry for a product definition property.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntryForProperty extends IDeltaEntry {

    /**
     * Returns the type of the property this entry refers.
     */
    PropertyValueType getPropertyType();

    /**
     * Returns the name of the product definition property this entry relates.
     */
    String getPropertyName();

}
