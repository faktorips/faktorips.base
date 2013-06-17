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

import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunction;
import org.faktorips.fl.FlFunction;

/**
 * A formula library is a collection of formulas functions that can be used in other formulas.
 * 
 * @see IFormulaFunction
 * 
 * @author frank
 */
public interface IFormulaLibrary extends IIpsObject {

    public static final String PLUGIN_ID = "org.faktorips.devtools.formulalibrary"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "FORMULA_LIBRARY-"; //$NON-NLS-1$

    public static final String MSGCODE_DUPLICATE_LIBRARY = MSGCODE_PREFIX + "duplicateLibrary"; //$NON-NLS-1$

    /**
     * Create and return an new {@link IFormulaFunction} part
     */
    IFormulaFunction newFormulaFunction();

    /**
     * Returns all {@link IFormulaFunction} parts
     */
    List<IFormulaFunction> getFormulaFunctions();

    /**
     * Remove the formulaFunction from the {@link IFormulaLibrary}
     */
    boolean removeFormulaFunction(IFormulaFunction formulaFunction);

    /**
     * Returns all the {@link FormulaFunction} as FlFunctions
     */
    List<FlFunction> getFlFunctions();

}