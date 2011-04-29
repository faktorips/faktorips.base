/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.pctype.AssociationDerivedUnionGroup;

/**
 * Page to specify the product new product component type association.
 */
public class ConfProdCmptTypePropertyPage extends WizardPage implements IBlockedValidationWizardPage,
        IHiddenWizardPage, IDefaultFocusPage {

    private NewPcTypeAssociationWizard wizard;
    private IProductCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList<String> visibleProperties = new ArrayList<String>(10);

    private Text targetRoleSingularTextProdCmptType;
    private Text targetRolePluralTextProdCmptType;
    private CardinalityField cardinalityFieldMinProdCmptType;
    private CardinalityField cardinalityFieldMaxProdCmptType;
    private Text descriptionText;
    private Text targetText;
    private Combo typeCombo;
    private Text unionText;
    private Checkbox derivedUnion;
    private Checkbox subsetCheckbox;

    /**
     * Composites to dispose an recreate the page content if the inverse association wil be
     * recreated e.g. the target or the option from the previous page are changed
     */
    private Composite pageComposite;
    private Composite groupGeneral;
    private Composite dynamicComposite;

    private AssociationDerivedUnionGroup derivedUnionGroup;

    protected ConfProdCmptTypePropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit,
            BindingContext bindingContext) {

        super(Messages.ConfProdCmptTypePropertyPage_pageName, Messages.ConfProdCmptTypePropertyPage_pageTitle, null);
        super.setDescription(Messages.ConfProdCmptTypePropertyPage_pageDescription);
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;

        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        pageComposite = wizard.createPageComposite(parent);

        createSourceAndTargetControls(toolkit.createLabelEditColumnComposite(pageComposite));

        toolkit.createVerticalSpacer(pageComposite, 10);

        groupGeneral = toolkit.createGroup(pageComposite, Messages.ConfProdCmptTypePropertyPage_groupProperties);
        ((GridData)groupGeneral.getLayoutData()).grabExcessVerticalSpace = false;
        dynamicComposite = createGeneralControls(groupGeneral);

        derivedUnionGroup = new AssociationDerivedUnionGroup(toolkit, bindingContext, pageComposite, association);

        // description
        descriptionText = wizard.createDescriptionText(pageComposite, 2);
        visibleProperties.add(IIpsObjectPart.PROPERTY_DESCRIPTION);

        setControl(pageComposite);
    }

    private void createSourceAndTargetControls(Composite top) {
        // source
        toolkit.createFormLabel(top, Messages.ConfProdCmptTypePropertyPage_labelSource);
        Text sourceText = toolkit.createText(top);
        sourceText.setEnabled(false);
        IProductCmptType productCmptType = wizard.findProductCmptType();
        if (productCmptType != null) {
            sourceText.setText(productCmptType.getQualifiedName());
        }

        // target
        toolkit.createFormLabel(top, Messages.ConfProdCmptTypePropertyPage_labelTarget);
        targetText = toolkit.createText(top);
        targetText.setEnabled(false);
    }

    private Composite createGeneralControls(Composite parent) {
        Composite workArea = toolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // top extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_TOP);

        // aggregation kind
        toolkit.createFormLabel(workArea, Messages.ConfProdCmptTypePropertyPage_labelType);
        typeCombo = toolkit.createCombo(workArea, IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);

        // role singular
        toolkit.createFormLabel(workArea, Messages.ConfProdCmptTypePropertyPage_labelTargetRoleSingular);
        targetRoleSingularTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularTextProdCmptType.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });

        // role plural
        toolkit.createFormLabel(workArea, Messages.ConfProdCmptTypePropertyPage_labelTargetRolePlural);
        targetRolePluralTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralTextProdCmptType.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRolePlural();
            }
        });

        // min cardinality
        toolkit.createFormLabel(workArea, Messages.ConfProdCmptTypePropertyPage_labelMinimumCardinality);
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMinProdCmptType = new CardinalityField(minCardinalityText);
        cardinalityFieldMinProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        toolkit.createFormLabel(workArea, Messages.ConfProdCmptTypePropertyPage_labelMaximumCardinality);
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMaxProdCmptType = new CardinalityField(maxCardinalityText);
        cardinalityFieldMaxProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);

        // bottom extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_BOTTOM);

        return workArea;
    }

    /**
     * Sets or resets the product component type association.
     * 
     * @param productCmptAssociation The product component type association which will be edit in
     *            this page
     */
    public void setProductCmptTypeAssociationAndUpdatePage(IProductCmptTypeAssociation productCmptAssociation) {
        association = productCmptAssociation;

        resetControlsAndBinding();

        if (productCmptAssociation != null) {
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(groupGeneral);

            bindAllControls(productCmptAssociation);

            setDefaults();
        }

        refreshPageConrolLayouts();
    }

    private void setDefaults() {
        association.setDerivedUnion(wizard.getAssociation().isDerivedUnion());
        if (!wizard.getAssociation().isSubsetOfADerivedUnion()) {
            derivedUnionGroup.setDefaultSubset(false);
        }
    }

    private void refreshPageConrolLayouts() {
        groupGeneral.pack(true);
        pageComposite.pack(true);
        pageComposite.getParent().pack(true);
        pageComposite.getParent().layout();
    }

    private void resetControlsAndBinding() {
        bindingContext.removeBindings(targetText);
        bindingContext.removeBindings(typeCombo);
        bindingContext.removeBindings(targetRoleSingularTextProdCmptType);
        bindingContext.removeBindings(targetRolePluralTextProdCmptType);
        bindingContext.removeBindings(cardinalityFieldMinProdCmptType.getControl());
        bindingContext.removeBindings(cardinalityFieldMaxProdCmptType.getControl());
        bindingContext.removeBindings(descriptionText);

        bindingContext.removeBindings(derivedUnion);
        bindingContext.removeBindings(subsetCheckbox);
        bindingContext.removeBindings(unionText);

        wizard.getExtFactoryProductCmptTypeAssociation().removeBinding(bindingContext);

        targetRoleSingularTextProdCmptType.setText(""); //$NON-NLS-1$
        targetRolePluralTextProdCmptType.setText(""); //$NON-NLS-1$
        cardinalityFieldMinProdCmptType.setText(""); //$NON-NLS-1$
        cardinalityFieldMaxProdCmptType.setText(""); //$NON-NLS-1$
        descriptionText.setText(""); //$NON-NLS-1$
    }

    private void bindAllControls(IProductCmptTypeAssociation productCmptAssociation) {
        bindingContext.bindContent(targetText, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                AssociationType.class);
        bindingContext.bindContent(targetRoleSingularTextProdCmptType, productCmptAssociation,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        bindingContext.bindContent(targetRolePluralTextProdCmptType, productCmptAssociation,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        bindingContext.bindContent(cardinalityFieldMinProdCmptType, productCmptAssociation,
                IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(cardinalityFieldMaxProdCmptType, productCmptAssociation,
                IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(descriptionText, productCmptAssociation, IIpsObjectPart.PROPERTY_DESCRIPTION);

        derivedUnionGroup.bindContent(bindingContext, association);

        wizard.getExtFactoryProductCmptTypeAssociation().bind(bindingContext);

        bindingContext.updateUI();
    }

    private void updateDefaultTargetRolePlural() {
        if (StringUtils.isEmpty(association.getTargetRolePlural()) && association.isTargetRolePluralRequired()) {
            association.setTargetRolePlural(association.getDefaultTargetRolePlural());
        }
    }

    private void updateDefaultTargetRoleSingular() {
        if (association == null) {
            return;
        }

        if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
            association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
        }
    }

    /**
     * @return <code>true</code> if the product component type is available.
     */
    @Override
    public boolean isPageVisible() {
        return wizard.isProductCmptTypeAvailable() && wizard.isConfigureProductCmptType();
    }

    @Override
    public List<String> getProperties() {
        return visibleProperties;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            wizard.handleConfProdCmptTypeSelectionState();
        }
        super.setVisible(visible);
    }

    @Override
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }

    @Override
    public void setDefaultFocus() {
        updateDefaultTargetRoleSingular();
        targetRoleSingularTextProdCmptType.setFocus();
        targetRoleSingularTextProdCmptType.selectAll();
    }
}
