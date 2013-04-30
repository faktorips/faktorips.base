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

package org.faktorips.devtools.formulalibrary.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.FlFunction;

/**
 * A formula function is used in formula libraries and is a wrapper for a method and an expression.
 * 
 * @author frank
 */
public interface IFormulaFunction extends IIpsObjectPart {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "FORMULA_FUNCTION-"; //$NON-NLS-1$

    /**
     * Returns the {@link IBaseMethod} that defines the signature of the formula.
     */
    IFormulaMethod getFormulaMethod();

    /**
     * Returns the {@link IExpression} containing the expression of the formula.
     */
    IExpression getExpression();

    /**
     * Creates an FlFunctino for this formula function that could be used by the formula compiler to
     * interpret the formula.
     * 
     */
    FlFunction getFlFunction();

}
