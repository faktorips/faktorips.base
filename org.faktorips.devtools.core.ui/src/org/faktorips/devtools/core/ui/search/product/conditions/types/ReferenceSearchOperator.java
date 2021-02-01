/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * The ReferenceSearchOperator checks, if an argument is or is not member of the operand
 * {@link List}.
 * 
 * 
 * @author dicker
 */
public class ReferenceSearchOperator extends AbstractSearchOperator<ReferenceSearchOperatorType> {

    public ReferenceSearchOperator(ValueDatatype valueDatatype, ReferenceSearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
    }

    @Override
    protected boolean check(Object operand, IProductPartsContainer productPartsContainer) {
        ArgumentCheck.notNull(operand);
        List<?> operandList = (List<?>)operand;

        return operandList.contains(getArgument());
    }

}
