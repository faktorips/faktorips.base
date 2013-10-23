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

import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

/**
 * Wizard to create constrained Associations. This Wizard appears when user clicks on the button
 * <quote>override</quote> The Wizard contains two pages.
 * 
 * @since 3.11
 */
public class ConstrainableAssociationWizard extends Wizard {
    private ConstrainableAssociationPmo pmo;

    public ConstrainableAssociationWizard(ConstrainableAssociationPmo pmo) {
        super();
        this.setWindowTitle(Messages.ConstrainableAssociationWizard_title);
        this.pmo = pmo;
    }

    @Override
    public void addPages() {
        addPage(new ConstrainableAssociationSelectionPage(pmo));
        addPage(new ConstrainableAssociationTargetPage(pmo));
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
