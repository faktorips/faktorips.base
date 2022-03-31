/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;

public class NewPcTypeWizard extends NewIpsObjectWizard {

    private NewPcTypePage pctypePage;

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        pctypePage = new NewPcTypePage(selection);
        return pctypePage;
    }

    @Override
    protected IWizardPage[] createAdditionalPages(IStructuredSelection selection) {
        NewProductCmptTypePage page = new NewProductCmptTypePage(selection, pctypePage);
        pctypePage.setPageOfAssociatedType(page);
        return new IWizardPage[] { page };
    }

}
