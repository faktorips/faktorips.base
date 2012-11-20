/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
