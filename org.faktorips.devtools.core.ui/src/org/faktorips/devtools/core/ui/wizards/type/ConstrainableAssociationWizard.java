/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.binding.BindingContext;

public class ConstrainableAssociationWizard extends Wizard {
    private ConstrainableAssociationSelectionPage firstPage;
    private ConstrainableAssociationTargetPage secondPage;
    private ConstrainableAssociationPmo pmo;
    private BindingContext bindingContext;
    private IType cmptType;

    public ConstrainableAssociationWizard(IType cmptType) {
        super();
        this.setWindowTitle(Messages.ConstrainableAssociationWizard_title);
        pmo = new ConstrainableAssociationPmo();
        bindingContext = new BindingContext();
        this.cmptType = cmptType;
    }

    @Override
    public void addPages() {
        firstPage = new ConstrainableAssociationSelectionPage(pmo, cmptType);
        addPage(firstPage);

        secondPage = new ConstrainableAssociationTargetPage(pmo, cmptType);
        addPage(secondPage);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        if (currentPage == firstPage) {
            return secondPage;
        }
        return null;
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

    public ISelection getAssociationSelection() {
        ISelection selection = firstPage.getTreeViewerSelection();
        return selection;
    }

    public ISelection getTargetSelection() {
        return secondPage.getTreeViewerSelection();
    }

}
