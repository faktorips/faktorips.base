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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;

/**
 * Interface, that provides an operand for a condition of the product search
 * 
 * @author dicker
 */
public interface IOperandProvider {

    /**
     * Returns the operand of an given (=searched) IProductCmptGeneration e.g. an attribute value or
     * a used table.
     */
    public Object getSearchOperand(IProductPartsContainer productComponentGeneration);
}
