/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

/**
 * A delta entry describes a single difference between a product component generation and the type
 * it is based on. For example the type might contain a new attribute but the product component has
 * not matching attribute value.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntry {

    /**
     * Returns the entry's type.
     */
    public DeltaType getDeltaType();

    /**
     * Returns a detailed description, especially for mismatches.
     */
    public String getDescription();

    /**
     * Fixes the difference between the type and the product component.
     * <p>
     * For example if the type contains a new attribute but the product component generation. has
     * not matching attribute value, this method creates the attribute value.
     */
    public void fix();

}
