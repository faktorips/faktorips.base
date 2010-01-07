/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewProductComponentAction extends Action {

    private IWorkbenchWindow window;

    public NewProductComponentAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewProductComponentAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductCmptWizard.gif")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        OpenNewProductCmptWizardAction o = new OpenNewProductCmptWizardAction();
        o.init(window);
        o.run(this);
    }
}
