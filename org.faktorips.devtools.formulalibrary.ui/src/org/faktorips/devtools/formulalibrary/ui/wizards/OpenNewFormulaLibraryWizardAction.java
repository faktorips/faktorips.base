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

package org.faktorips.devtools.formulalibrary.ui.wizards;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * Open the Wizard to create new {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class OpenNewFormulaLibraryWizardAction extends OpenNewWizardAction {

    public OpenNewFormulaLibraryWizardAction() {
        // default for reflection
    }

    public OpenNewFormulaLibraryWizardAction(IWorkbenchWindow window) {
        super.init(window);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public INewWizard createWizard() {
        return new NewFormulaLibraryWizard();
    }

}
