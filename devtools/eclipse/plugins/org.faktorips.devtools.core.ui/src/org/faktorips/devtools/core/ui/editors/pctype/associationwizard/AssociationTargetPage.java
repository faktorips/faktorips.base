/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.pctype.AssociationDerivedUnionGroup;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;

/**
 * Page to specify the target, the association type, the derived union option, and the description.
 * 
 * @author Joerg Ortmann
 */
public class AssociationTargetPage extends WizardPage implements IBlockedValidationWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList<String> visibleProperties = new ArrayList<>(10);

    protected AssociationTargetPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association,
            UIToolkit toolkit, BindingContext bindingContext) {

        super(Messages.AssociationTargetPage_pageName, Messages.AssociationTargetPage_pageTitle, null);
        super.setDescription(Messages.AssociationTargetPage_pageDescription);
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;

        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageComposite = wizard.createPageComposite(parent);

        createTargetAndTypeControls(toolkit.createLabelEditColumnComposite(pageComposite));

        toolkit.createVerticalSpacer(pageComposite, 10);

        new AssociationDerivedUnionGroup(toolkit, bindingContext, pageComposite, association);

        createDescriptionControl(pageComposite);

        setControl(pageComposite);
    }

    private void createTargetAndTypeControls(Composite top) {
        // target
        toolkit.createFormLabel(top, Messages.AssociationTargetPage_labelTarget);
        PcTypeRefControl targetControl = toolkit.createPcTypeRefControl(association.getIpsProject(), top);
        bindingContext.bindContent(targetControl, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET);

        // type
        toolkit.createFormLabel(top, Messages.AssociationTargetPage_labelType);
        Combo typeCombo = toolkit.createCombo(top);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                IPolicyCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
        typeCombo.select(0);
        visibleProperties.add(IAssociation.PROPERTY_ASSOCIATION_TYPE);
    }

    private void createDescriptionControl(Composite pageComposite) {
        wizard.createDescriptionText(pageComposite, 2);
    }

    @Override
    public List<String> getProperties() {
        return visibleProperties;
    }

    @Override
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }
}
