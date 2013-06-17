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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * The <tt>AddFormulaAction</tt> defines the action adding <tt>IFormulaFunction</tt> to
 * <tt>IFormulaLibrary</tt> through <tt>FormulaLibraryPmo</tt>.
 * 
 * @see IFormulaLibrary
 * @see FormulaLibraryPmo
 * 
 * @author HBaagil
 */
public class AddFormulaAction extends IpsAction {

    private static final String IMAGE_NAME = "Add.gif"; //$NON-NLS-1$
    private FormulaLibraryPmo formulaLibraryPmo;

    /**
     * Creates a new <tt>AddFormulaAction</tt>
     * 
     * @param selectionProvider The <tt>ISelectionProvider</tt> where <tt>IFormulaFunction</tt> will
     *            be added.
     * @param formulaLibraryPmo The <tt>FormulaLibraryPmo</tt> linked to this.
     */
    public AddFormulaAction(ISelectionProvider selectionProvider, FormulaLibraryPmo formulaLibraryPmo) {
        super(selectionProvider);
        this.formulaLibraryPmo = formulaLibraryPmo;
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
    }

    @Override
    public void run(IStructuredSelection selection) {
        formulaLibraryPmo.addNewFormulaFunction();
    }

    @Override
    public void dispose() {
        super.dispose();
        formulaLibraryPmo.dispose();
    }
}
