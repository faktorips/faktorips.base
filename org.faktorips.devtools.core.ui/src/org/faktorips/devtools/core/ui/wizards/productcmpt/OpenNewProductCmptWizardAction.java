/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

/**
 *
 */
public class OpenNewProductCmptWizardAction extends OpenNewWizardAction {

    private static final String NEW_TEMPLATE_ID = "org.faktorips.devtools.actions.OpenNewProductTemplateWizardAction"; //$NON-NLS-1$

    private boolean template = false;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.getId().equals(NEW_TEMPLATE_ID)) {
            template = true;
        }
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    @Override
    public INewWizard createWizard() {
        if (template) {
            return new NewProductTemplateWizard();
        } else {
            return new NewProductCmptWizard();
        }
    }

    @Override
    public void dispose() {
        // nothing to do
    }

}
