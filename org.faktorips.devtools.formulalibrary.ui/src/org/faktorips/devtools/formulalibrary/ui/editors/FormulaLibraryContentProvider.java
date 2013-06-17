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

package org.faktorips.devtools.formulalibrary.ui.editors;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;

/**
 * The <tt>FormulaLibraryContentProvider</tt> determines the content of
 * <tt>FormulaFunctionListSection</tt>.
 * 
 * @see FormulaFunctionListSection
 * 
 * @author HBaagil
 */
public class FormulaLibraryContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof FormulaLibraryPmo) {
            FormulaLibraryPmo formulaLibraryPmo = (FormulaLibraryPmo)inputElement;
            List<IFormulaFunction> formulaFunctions = formulaLibraryPmo.getFormulaFunctions();
            return formulaFunctions.toArray();
        }
        return new Object[] {};
    }
}
