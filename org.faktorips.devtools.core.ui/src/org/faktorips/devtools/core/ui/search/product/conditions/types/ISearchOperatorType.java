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

import org.faktorips.datatype.ValueDatatype;

/**
 * The ISearchOperatorType defines the Type of an {@link ISearchOperator}.
 * 
 * @author dicker
 */
public interface ISearchOperatorType {

    /**
     * returns a String for Labels
     */
    String getLabel();

    /**
     * returns an {@link ISearchOperator} for the given {@link IOperandProvider}
     */
    ISearchOperator createSearchOperator(IOperandProvider operandProvider,
            ValueDatatype valueDatatype,
            String argument);

    /**
     * Returns the name of the enum constant. These names must be unique to ensure that
     * search-settings can be persisted between sessions.
     * 
     * @see Enum#name()
     */
    String name();
}
