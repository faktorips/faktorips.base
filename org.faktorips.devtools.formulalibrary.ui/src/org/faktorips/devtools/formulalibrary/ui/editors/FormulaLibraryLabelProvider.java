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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;

/**
 * The <tt>FormulaLibraryLabelProvider</tt> determine what text to show in
 * <tt>FormulaFunctionListSection</tt>.
 * 
 * @see FormulaFunctionListSection
 * 
 * @author HBaagil
 */
public class FormulaLibraryLabelProvider extends ColumnLabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof IFormulaFunction) {
            IFormulaFunction function = (IFormulaFunction)element;
            return function.getFormulaMethod().getFormulaName();
        }
        return null;
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptableElement = (IAdaptable)element;
            return IpsUIPlugin.getImageHandling().getImage(adaptableElement);
        }
        return super.getImage(element);
    }
}
