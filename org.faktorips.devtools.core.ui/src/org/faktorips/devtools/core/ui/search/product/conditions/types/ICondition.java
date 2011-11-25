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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;

public interface ICondition {

    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType);

    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement elementPart);

    public ValueDatatype getValueDatatype(IIpsElement elementPart);

    public IValueSet getValueSet(IIpsElement elementPart);

    public Collection<?> getAllowedValues(IIpsElement elementPart);

    public boolean hasValueSet();

    public IOperandProvider createOperandProvider(IIpsElement elementPart);

    public String getName();

    public String getNoSearchableElementsMessage(IProductCmptType productCmptType);

}