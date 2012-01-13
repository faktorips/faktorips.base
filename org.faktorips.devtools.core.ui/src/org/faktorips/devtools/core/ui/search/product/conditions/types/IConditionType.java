/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.Collection;
import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;

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
    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType);

    /**
     * Returns a List of {@link ISearchOperatorType ISearchOperatorTypes}, which can be used in a
     * condition for the specified {@link IIpsElement}.
     * <p>
     * This method depends on the implementation of the IConditionType and considers for example the
     * {@link ValueDatatype} of the specified IIpsElement. The result depends for example, whether
     * the ValueDatatype is a Comparable or not.
     */
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement);

    /**
     * Returns the {@link ValueDatatype} of the specified {@link IIpsElement}
     */
    public ValueDatatype getValueDatatype(IIpsElement elementPart);

    /**
     * Returns true, if the condition can deliver a {@link IValueSet}.
     * <p>
     * Call this method before calling {@link #getValueSet(IIpsElement)}
     */
    public boolean hasValueSet();

    /**
     * Returns a {@link IValueSet} according to the specified {@link IIpsElement}.
     * <p>
     * Call this method only after a call of {@link #hasValueSet()} returned true
     * 
     * @throws IllegalStateException if there is no {@link IValueSet} for this condition.
     */
    public IValueSet getValueSet(IIpsElement elementPart);

    /**
     * Returns a Collection with all allowed values for the specified {@link IIpsElement}, which are
     * allowed by this condition.
     * 
     */
    public Collection<?> getAllowedValues(IIpsElement elementPart);

    /**
     * Creates an {@link IOperandProvider} using the specified {@link IIpsElement}
     */
    public IOperandProvider createOperandProvider(IIpsElement elementPart);

    /**
     * Returns the name of the condition
     */
    public String getName();

    /**
     * Returns true, if the argument of the condition is an {@link IIpsObject}.
     */
    public boolean isArgumentIpsObject();

}