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

import java.beans.PropertyChangeEvent;

import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * 
 * presentation model object for ConstrainableAssociationWizard to spot the selections made in the
 * first and second Page
 * 
 * @since 3.11
 */
public class ConstrainableAssociationPmo extends PresentationModelObject {

    public static final String PROPERTY_SELECTED_ASSOCIATION = "selectedAssociation"; //$NON-NLS-1$
    public static final String PROPERTY_SELECTED_TARGET = "selectedTarget"; //$NON-NLS-1$

    private IAssociation selectedAssociation;
    private IType selectedTarget;
    private IType type;

    public ConstrainableAssociationPmo(IType type) {
        super();
        this.type = type;
    }

    public IType getSelectedTarget() {
        return selectedTarget;
    }

    public void setSelectedTarget(IType selectedTarget) {
        IType oldValue = this.selectedTarget;
        this.selectedTarget = selectedTarget;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_TARGET, oldValue, this.selectedTarget));
    }

    public IAssociation getSelectedAssociation() {
        return selectedAssociation;
    }

    public void setSelectedAssociation(IAssociation selectedAssociation) {
        IAssociation oldValue = this.selectedAssociation;
        this.selectedAssociation = selectedAssociation;
        notifyListeners(
                new PropertyChangeEvent(this, PROPERTY_SELECTED_ASSOCIATION, oldValue, this.selectedAssociation));
    }

    public IType getType() {
        return type;
    }
}
