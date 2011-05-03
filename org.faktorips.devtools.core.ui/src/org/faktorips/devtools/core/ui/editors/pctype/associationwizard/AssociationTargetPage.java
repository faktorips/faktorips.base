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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.pctype.AssociationDerivedUnionGroup;

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

    private ArrayList<String> visibleProperties = new ArrayList<String>(10);

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
        Text text = wizard.createDescriptionText(pageComposite, 2);
        bindingContext.bindContent(text, association, IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
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
