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

public class ConstrainableAssociationWizard extends Wizard {
    private ConstrainableAssociationWizardPageOne firstPage;
    private ConstrainableAssociationWizardPageTwo secondPage;

    public ConstrainableAssociationWizard() {
        super();
        this.setWindowTitle("BeziehungenAuswählen");
    }

    @Override
    public void addPages() {
        firstPage = new ConstrainableAssociationWizardPageOne("firstPage");
        addPage(firstPage);

        secondPage = new ConstrainableAssociationWizardPageTwo("secondPage");
        addPage(secondPage);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        if (currentPage == firstPage) {
            return secondPage;
        }
        return null; // TODO currentPage = secondPage }
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }
}
