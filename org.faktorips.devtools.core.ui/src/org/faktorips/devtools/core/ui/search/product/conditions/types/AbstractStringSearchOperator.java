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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;

/**
 * An implementation of {@link ISearchOperator} for String operands with a defined
 * {@link ValueDatatype}
 * 
 * @author dicker
 */
public abstract class AbstractStringSearchOperator<S extends ISearchOperatorType> extends AbstractSearchOperator<S> {

    protected AbstractStringSearchOperator(ValueDatatype valueDatatype, S searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    protected final boolean check(Object operand, IProductPartsContainer productPartsContainer) {
        if (operand instanceof String && getValueDatatype().isParsable((String)operand)) {
            return checkInternal((String)operand);
        }
        return false;
    }

    abstract boolean checkInternal(String operand);
}
