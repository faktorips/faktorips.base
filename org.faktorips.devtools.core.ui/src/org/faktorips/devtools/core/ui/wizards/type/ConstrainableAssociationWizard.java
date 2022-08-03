/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * Wizard to create constrained Associations. This Wizard appears when user clicks on the button
 * <em>override</em>.
 * <p>
 * The Wizard contains two pages.
 * 
 * @since 3.11
 */
public class ConstrainableAssociationWizard extends Wizard {
    private ConstrainableAssociationPmo pmo;

    public ConstrainableAssociationWizard(ConstrainableAssociationPmo pmo) {
        super();
        setWindowTitle(Messages.ConstrainableAssociationWizard_title);
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
