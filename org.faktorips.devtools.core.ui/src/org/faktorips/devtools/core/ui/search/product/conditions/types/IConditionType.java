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

import java.util.Collection;
import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * This interface defines the type of a condition and delivers the basic functions for the product
 * search.
 * 
 * @author dicker
 */
public interface IConditionType {

    /**
     * Returns a List of {@link IIpsElement IIpsElements} of the specified {@link IProductCmptType},
     * which can be searched with the implementation of IConditionType.
     */
    List<IIpsElement> getSearchableElements(IProductCmptType productCmptType);

    /**
     * Returns a List of {@link ISearchOperatorType ISearchOperatorTypes}, which can be used in a
     * condition for the specified {@link IIpsElement}.
     * <p>
     * This method depends on the implementation of the IConditionType and considers for example the
     * {@link ValueDatatype} of the specified IIpsElement. The result depends for example, whether
     * the ValueDatatype is a Comparable or not.
     */
    List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement);

    /**
     * Returns the {@link ValueDatatype} of the specified {@link IIpsElement}
     */
    ValueDatatype getValueDatatype(IIpsElement elementPart);

    /**
     * Returns true, if the condition can deliver a {@link IValueSet}.
     * <p>
     * Call this method before calling {@link #getValueSet(IIpsElement)}
     */
    boolean hasValueSet();

    /**
     * Returns a {@link IValueSet} according to the specified {@link IIpsElement}.
     * <p>
     * Call this method only after a call of {@link #hasValueSet()} returned true
     * 
     * @throws IllegalStateException if there is no {@link IValueSet} for this condition.
     */
    IValueSet getValueSet(IIpsElement elementPart);

    /**
     * Returns a Collection with all allowed values for the specified {@link IIpsElement}, which are
     * allowed by this condition.
     * 
     */
    Collection<?> getAllowedValues(IIpsElement elementPart);

    /**
     * Creates an {@link IOperandProvider} using the specified {@link IIpsElement}
     */
    IOperandProvider createOperandProvider(IIpsElement elementPart);

    /**
     * Returns the name of the condition
     */
    String getName();

    /**
     * Returns true, if the argument of the condition is an {@link IIpsObject}.
     */
    boolean isArgumentIpsObject();

}
