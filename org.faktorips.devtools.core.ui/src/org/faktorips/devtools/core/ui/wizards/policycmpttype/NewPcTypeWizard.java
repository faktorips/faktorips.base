/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;
import org.faktorips.devtools.core.ui.wizards.productcmpttype.ProductCmptTypePage;

public class NewPcTypeWizard extends NewIpsObjectWizard {

    private PcTypePage pctypePage;

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        pctypePage = new PcTypePage(selection);
        return pctypePage;
    }

    @Override
    protected IWizardPage[] createAdditionalPages(IStructuredSelection selection) throws CoreException {
        ProductCmptTypePage page = new ProductCmptTypePage(selection, pctypePage);
        pctypePage.setProductCmptTypePage(page);
        return new IWizardPage[] { page };
    }
}
