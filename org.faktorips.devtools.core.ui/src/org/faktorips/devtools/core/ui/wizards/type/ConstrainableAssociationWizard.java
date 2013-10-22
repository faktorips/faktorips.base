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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public class ConstrainableAssociationWizard extends Wizard {
    private ConstrainableAssociationSelectionPage firstPage;
    private ConstrainableAssociationTargetPage secondPage;
    private ConstrainableAssociationPmo pmo;

    public ConstrainableAssociationWizard(ConstrainableAssociationPmo pmo) {
        super();
        this.setWindowTitle(Messages.ConstrainableAssociationWizard_title);
        this.pmo = pmo;
    }

    @Override
    public void addPages() {
        firstPage = new ConstrainableAssociationSelectionPage(pmo);
        addPage(firstPage);

        secondPage = new ConstrainableAssociationTargetPage(pmo);
        addPage(secondPage);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        if (currentPage == firstPage) {
            secondPage.setLabel();
            secondPage.initContentLabelProvider();
            return secondPage;
        }
        return null;
    }

    @Override
    public boolean performFinish() {
        IAssociation selectedAssociation = pmo.getSelectedAssociation();
        IType selectedTarget = pmo.getSelectedTarget();
        CreateConstrainingAssociationOperation createConstrainingAssociation = new CreateConstrainingAssociationOperation(
                pmo.getType(), selectedAssociation, selectedTarget);
        createConstrainingAssociation.execute();
        return true;
    }
}
