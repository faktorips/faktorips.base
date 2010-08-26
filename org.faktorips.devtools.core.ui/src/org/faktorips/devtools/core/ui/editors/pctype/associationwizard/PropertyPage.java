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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.editors.pctype.AssociationQualificationGroup;

/**
 * Page to specify the properties of the association.
 * 
 * @author Joerg Ortmann
 */
public class PropertyPage extends WizardPage implements IBlockedValidationWizardPage, IDefaultFocusPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList<String> visibleProperties = new ArrayList<String>(10);

    private Text targetRoleSingularText;
    private Text targetRolePluralText;
    private CardinalityField cardinalityFieldMin;
    private CardinalityField cardinalityFieldMax;
    private AssociationQualificationGroup associationQualificationGroup;
    private Text noteAboutProductStructureConstrained;

    protected PropertyPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association,
            UIToolkit toolkit, BindingContext bindingContext) {

        super(Messages.PropertyPage_pageName, Messages.PropertyPage_pageTitle, null);
        super.setDescription(Messages.PropertyPage_pageDescription);
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;

        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageComposite = wizard.createPageComposite(parent);

        createGeneralControls(pageComposite);

        associationQualificationGroup = new AssociationQualificationGroup(toolkit, bindingContext, pageComposite,
                association);

        // bind the special note label
        associationQualificationGroup.bindLabelAboutConstrainedByProductStructure(noteAboutProductStructureConstrained,
                bindingContext);

        setControl(pageComposite);
    }

    private void createGeneralControls(Composite parent) {
        Group groupGeneral = toolkit.createGroup(parent, Messages.PropertyPage_groupProperties);
        GridData gd = (GridData)groupGeneral.getLayoutData();
        gd.grabExcessVerticalSpace = false;

        Composite workArea = toolkit.createLabelEditColumnComposite(groupGeneral);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // top extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_TOP);

        // role singular
        toolkit.createFormLabel(workArea, Messages.PropertyPage_labelTargetRoleSingular);
        targetRoleSingularText = toolkit.createText(workArea);
        visibleProperties.add(IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });

        // role plural
        toolkit.createFormLabel(workArea, Messages.PropertyPage_labelTargetRolePlural);
        targetRolePluralText = toolkit.createText(workArea);
        visibleProperties.add(IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRolePlural();
            }
        });

        // min cardinality
        toolkit.createFormLabel(workArea, Messages.PropertyPage_labelMinimumCardinality);
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMin = new CardinalityField(minCardinalityText);
        cardinalityFieldMin.setSupportsNull(false);
        visibleProperties.add(IAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        toolkit.createFormLabel(workArea, Messages.PropertyPage_labelMaximumCardinality);
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMax = new CardinalityField(maxCardinalityText);
        cardinalityFieldMax.setSupportsNull(false);
        visibleProperties.add(IAssociation.PROPERTY_MAX_CARDINALITY);

        Composite info = toolkit.createGridComposite(groupGeneral, 1, true, false);

        // create note about constrained by product structure
        noteAboutProductStructureConstrained = AssociationQualificationGroup.createConstrainedNote(toolkit, info);

        // bottom extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        wizard.getExtFactoryAssociation().bind(bindingContext);

        bindContent();
    }

    public void bindContent() {
        bindingContext.bindContent(targetRoleSingularText, association, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        bindingContext.bindContent(targetRolePluralText, association, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        bindingContext.bindContent(cardinalityFieldMin, association, IAssociation.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(cardinalityFieldMax, association, IAssociation.PROPERTY_MAX_CARDINALITY);
    }

    private void updateDefaultTargetRolePlural() {
        if (StringUtils.isEmpty(association.getTargetRolePlural()) && association.isTargetRolePluralRequired()) {
            association.setTargetRolePlural(association.getDefaultTargetRolePlural());
        }
    }

    private void updateDefaultTargetRoleSingular() {
        if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
            association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
        }
    }

    @Override
    public List<String> getProperties() {
        return visibleProperties;
    }

    @Override
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }

    @Override
    public void setDefaultFocus() {
        updateDefaultTargetRoleSingular();
        targetRoleSingularText.setFocus();
        targetRoleSingularText.selectAll();
    }
}
