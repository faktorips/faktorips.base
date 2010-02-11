/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;

/**
 * Page to specify the inverse association, either define properties for a new association, or
 * choose an existing.
 * 
 * @author Joerg Ortmann
 */
public class InverseAssociationPropertyPage extends WizardPage implements IBlockedValidationWizardPage,
        IHiddenWizardPage, IDefaultFocusPage {

    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList<String> visibleProperties = new ArrayList<String>(10);

    protected IPolicyCmptTypeAssociation association;

    private Text targetRoleSingularText;
    private Text targetRolePluralText;
    private CardinalityField cardinalityFieldMin;
    private CardinalityField cardinalityFieldMax;
    private Text targetText;
    private Text typeText;
    private Combo existingRelCombo;
    private Label existingRelLabel;

    private String prevSelExistingAssociation;
    private Text description;

    // Composites to dispose an recreate the page content if the inverse association wil be
    // recreated
    // e.g. the target or the option from the previous page are changed
    private Composite pageComposite;
    private Composite dynamicComposite;

    public InverseAssociationPropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit,
            BindingContext bindingContext) {
        super(Messages.InverseAssociationPropertyPage_pageName, Messages.InverseAssociationPropertyPage_pageTitle, null);
        setDescription(Messages.InverseAssociationPropertyPage_pageDescription);
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;

        setPageComplete(true);
    }

    public void createControl(Composite parent) {
        pageComposite = wizard.createPageComposite(parent);

        Composite labelEditComposite = toolkit.createLabelEditColumnComposite(pageComposite);
        createTargetAndTypeControls(labelEditComposite);

        createExistingAssociationComboControl(labelEditComposite);

        toolkit.createVerticalSpacer(pageComposite, 10);

        dynamicComposite = createGeneralControls(pageComposite);

        setControl(pageComposite);
    }

    private void createExistingAssociationComboControl(Composite top) {
        existingRelLabel = toolkit.createFormLabel(top,
                Messages.InverseAssociationPropertyPage_labelExistingAssociation);
        existingRelCombo = toolkit.createCombo(top);
        existingRelCombo.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event ev) {
                existingAssociationSelectionChanged();
            }
        });
    }

    private void createTargetAndTypeControls(Composite top) {
        // target
        toolkit.createFormLabel(top, Messages.InverseAssociationPropertyPage_labelTarget);
        targetText = toolkit.createText(top);
        targetText.setEnabled(false);

        // type
        toolkit.createFormLabel(top, Messages.InverseAssociationPropertyPage_labelType);
        typeText = toolkit.createText(top);
        typeText.setEnabled(false);
    }

    private Composite createGeneralControls(Composite root) {
        Composite parent = toolkit.createGridComposite(root, 1, false, false);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));

        createMainProperties(parent);

        description = wizard.createDescriptionText(parent, 2);
        visibleProperties.add(IIpsObjectPart.PROPERTY_DESCRIPTION);

        return parent;
    }

    private void createMainProperties(Composite parent) {
        Group group = toolkit.createGroup(parent, Messages.InverseAssociationPropertyPage_labelProperties);
        ((GridData)group.getLayoutData()).grabExcessVerticalSpace = false;

        Composite workArea = toolkit.createLabelEditColumnComposite(group);
        workArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // top extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_TOP);

        // role singular
        toolkit.createFormLabel(workArea, Messages.InverseAssociationPropertyPage_labelTargetRoleSingular);
        targetRoleSingularText = toolkit.createText(workArea);
        visibleProperties.add(IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });

        // role plural
        toolkit.createFormLabel(workArea, Messages.InverseAssociationPropertyPage_labelTargetRolePlural);
        targetRolePluralText = toolkit.createText(workArea);
        visibleProperties.add(IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });

        // min cardinality
        toolkit.createFormLabel(workArea, Messages.InverseAssociationPropertyPage_labelMinimumCardinality);
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMin = new CardinalityField(minCardinalityText);
        cardinalityFieldMin.setSupportsNull(false);
        visibleProperties.add(IAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        toolkit.createFormLabel(workArea, Messages.InverseAssociationPropertyPage_labelMaximumCardinality);
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMax = new CardinalityField(maxCardinalityText);
        cardinalityFieldMax.setSupportsNull(false);
        visibleProperties.add(IAssociation.PROPERTY_MAX_CARDINALITY);

        // bottom extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association,
                IExtensionPropertyDefinition.POSITION_BOTTOM);
    }

    private void updateDefaultTargetRoleSingular() {
        if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
            association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
        }
    }

    /**
     * Sets or resets the inverers association.
     * 
     * @param inverseAssociation The inverse association which will be edit in this page
     */
    public void setAssociationAndUpdatePage(IPolicyCmptTypeAssociation inverseAssociation) {
        association = inverseAssociation;

        resetControlsAndBinding(inverseAssociation);

        if (association != null) {
            // recreate the page depending on the given association
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(pageComposite);

            bindAllControls(inverseAssociation);
        }

        refreshPageConrolLayouts();
    }

    private void resetControlsAndBinding(IPolicyCmptTypeAssociation association) {
        prevSelExistingAssociation = association == null ? "" : association.getName(); //$NON-NLS-1$
        bindingContext.removeBindings(targetRoleSingularText);
        bindingContext.removeBindings(targetRolePluralText);
        bindingContext.removeBindings(cardinalityFieldMin.getControl());
        bindingContext.removeBindings(cardinalityFieldMax.getControl());
        bindingContext.removeBindings(description);
        wizard.getExtFactoryInverseAssociation().removeBinding(bindingContext);

        targetRoleSingularText.setText(""); //$NON-NLS-1$
        targetRolePluralText.setText(""); //$NON-NLS-1$
        cardinalityFieldMin.setText(""); //$NON-NLS-1$
        cardinalityFieldMax.setText(""); //$NON-NLS-1$
        targetText.setText(""); //$NON-NLS-1$
        typeText.setText(""); //$NON-NLS-1$
        description.setText(""); //$NON-NLS-1$
    }

    private void bindAllControls(IPolicyCmptTypeAssociation association) {
        bindingContext.bindContent(targetRoleSingularText, association, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        bindingContext.bindContent(targetRolePluralText, association, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        bindingContext.bindContent(cardinalityFieldMin, association, IAssociation.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(cardinalityFieldMax, association, IAssociation.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(description, association, IIpsObjectPart.PROPERTY_DESCRIPTION);

        targetText.setText(association.getTarget());
        typeText.setText(association.getAssociationType().getName());

        wizard.getExtFactoryInverseAssociation().bind(bindingContext);

        bindingContext.updateUI();
    }

    private void refreshPageConrolLayouts() {
        pageComposite.pack(true);
        pageComposite.getParent().pack(true);
        pageComposite.getParent().layout(true);
    }

    /**
     * Set <code>true</code> if the existing association control should be displayed otherwise
     * <code>false</code>.
     */
    public void setShowExistingAssociationDropDown(boolean showExisting) {
        existingRelCombo.setVisible(showExisting);
        existingRelLabel.setVisible(showExisting);
    }

    /**
     * Stores the given association names as available existing inverse associations.
     */
    public void setExistingAssociations(String[] associations) {
        existingRelCombo.setItems(associations);
        existingRelCombo.select(0);
    }

    /**
     * Refreshs the control changeable state.
     */
    public void refreshControls() {
        setControlChangeable(!wizard.isExistingInverseAssociation());
    }

    private void setControlChangeable(boolean changeable) {
        toolkit.setDataChangeable(targetRoleSingularText, changeable);
        toolkit.setDataChangeable(targetRolePluralText, changeable);
        toolkit.setDataChangeable(cardinalityFieldMin.getControl(), changeable);
        toolkit.setDataChangeable(cardinalityFieldMax.getControl(), changeable);
    }

    /**
     * Event function to indicate a change of the existing association.
     */
    private void existingAssociationSelectionChanged() {
        int selIdx = existingRelCombo.getSelectionIndex();
        if (selIdx >= 0) {
            String selExistingAssociation = existingRelCombo.getItem(selIdx);
            if (!selExistingAssociation.equals(prevSelExistingAssociation)) {
                wizard.storeExistingInverseAssociation(selExistingAssociation);
                wizard.contentsChanged(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getProperties() {
        return visibleProperties;
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>false</code> if no inverse association should be created or no existing
     *         association exists otherwise <code>true</code>.
     */
    public boolean isPageVisible() {
        if (wizard.isNoneInverseAssociation()) {
            return false;
        } else if (wizard.isExistingInverseAssociation()) {
            return wizard.areExistingAssociationsAvailable();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            wizard.handleInverseAssociationSelectionState();
        }
        super.setVisible(visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultFocus() {
        updateDefaultTargetRoleSingular();
        targetRoleSingularText.setFocus();
        targetRoleSingularText.selectAll();
    }
}
