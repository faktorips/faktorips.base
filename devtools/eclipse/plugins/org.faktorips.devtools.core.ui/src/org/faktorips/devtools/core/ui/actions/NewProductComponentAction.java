/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewProductComponentAction extends Action {

    private final IWorkbenchWindow window;
    private final boolean template;

    public NewProductComponentAction(IWorkbenchWindow window, boolean template) {
        super();
        this.window = window;
        this.template = template;
        if (template) {
            setText(IpsObjectType.PRODUCT_TEMPLATE.getDisplayName());
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductTemplateWizard.gif")); //$NON-NLS-1$
        } else {
            setText(IpsObjectType.PRODUCT_CMPT.getDisplayName());
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductCmptWizard.gif")); //$NON-NLS-1$
        }
    }

    @Override
    public void run() {
        OpenNewProductCmptWizardAction o = new OpenNewProductCmptWizardAction();
        o.setTemplate(template);
        o.init(window);
        o.run(this);
    }

}
