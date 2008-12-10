/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;
import org.faktorips.devtools.core.ui.wizards.productcmpttype.ProductCmptTypePage;


/**
 *
 */
public class NewPcTypeWizard extends NewIpsObjectWizard implements IPageChangedListener{
    
    private PcTypePage pctypePage;
    
    /** 
     * {@inheritDoc}
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        pctypePage = new PcTypePage(selection);
        return pctypePage;
    }

    /** 
     * {@inheritDoc}
     * @throws CoreException 
     */
    protected IWizardPage[] createAdditionalPages(IStructuredSelection selection) throws CoreException {
        ProductCmptTypePage page = new ProductCmptTypePage(selection, pctypePage);
        pctypePage.setProductCmptTypePage(page);
        return new IWizardPage[]{page};
    }
}
