/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;

/**
 * Page to specify the inverse association, either define properties for a new association, or choose
 * an existing.
 * 
 * @author Joerg Ortmann
 */
public class InverseRelationPropertyPage extends WizardPage implements IBlockedValidationWizardPage, IHiddenWizardPage {
    
    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;
    private BindingContext bindingContext;
    
    private ArrayList visibleProperties = new ArrayList(10);
    
    protected IPolicyCmptTypeAssociation association;
    
    private Text targetRoleSingularText;
    private Text targetRolePluralText;
    private CardinalityField cardinalityFieldMin;
    private CardinalityField cardinalityFieldMax;
    private Text targetText;
    private Text typeText;
    private Combo existingRelCombo;
    private Label existingRelLabel;

    private String prevSelExistingRelation;
    private Text description;
    
    // Composites to dispose an recreate the page content if the inverse association wil be recreated
    // e.g. the target or the option from the previous page are changed
    private Composite pageComposite;
    private Composite dynamicComposite;
    
    public InverseRelationPropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit, BindingContext bindingContext) {
        super("InverseRelationPropertyPage", "Inverse relation properties", null);
        setDescription("Define new inverse relation");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        setPageComplete(true);
    }

    public void createControl(Composite parent) {
        pageComposite = wizard.createPageComposite(parent);
        
        createTargetAndTypeControls(toolkit.createLabelEditColumnComposite(pageComposite));

        createExistingAssociationComboControl(pageComposite);
        
        toolkit.createVerticalSpacer(pageComposite, 10);
        
        dynamicComposite = createGeneralControls(pageComposite);
        
        setControl(pageComposite);
    }

    private void createExistingAssociationComboControl(Composite top) {
        existingRelLabel = toolkit.createFormLabel(top, "Existing association");
        existingRelCombo = toolkit.createCombo(top);
        existingRelCombo.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event ev) {
                existingRelationSelectionChanged();
            }
        });
    }

    private void createTargetAndTypeControls(Composite top) {
        // target
        toolkit.createFormLabel(top, "Target");
        targetText = toolkit.createText(top);
        targetText.setEnabled(false);

        // type
        toolkit.createFormLabel(top, "Type");
        typeText = toolkit.createText(top);
        typeText.setEnabled(false);
    }

    private Composite createGeneralControls(Composite root) {
        Composite parent = toolkit.createGridComposite(root, 1, false, false);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        createMainProperties(parent);
        
        description = wizard.createDescriptionText(parent, 2);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
        return parent;
    }

    private void createMainProperties(Composite parent) {
        Group group = toolkit.createGroup(parent, "Properties");
        ((GridData)group.getLayoutData()).grabExcessVerticalSpace = false;
        
        Composite workArea = toolkit.createLabelEditColumnComposite(group);
        workArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false ));

        // top extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularText = toolkit.createText(workArea);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        targetRolePluralText = toolkit.createText(workArea);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMin = new CardinalityField(minCardinalityText);
        cardinalityFieldMin.setSupportsNull(false);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMax = new CardinalityField(maxCardinalityText);
        cardinalityFieldMax.setSupportsNull(false);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
    }

    /**
     * Sets or resets the inverers association. 
     * 
     * @param inverseAssociation The inverse association which will be edit in this page
     */
    public void setAssociationAndUpdatePage(IPolicyCmptTypeAssociation inverseAssociation) {
        this.association = inverseAssociation;
        
        resetControlsAndBinding(inverseAssociation);
        
        if (association != null){
            // recreate the page depending on the given association
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(pageComposite);
            
            bindAllControls(inverseAssociation);
        }
        
        refreshPageConrolLayouts();
    }

    private void resetControlsAndBinding(IPolicyCmptTypeAssociation association) {
        prevSelExistingRelation = association==null?"":association.getName();
        bindingContext.removeBindings(targetRoleSingularText);
        bindingContext.removeBindings(targetRolePluralText);
        bindingContext.removeBindings(cardinalityFieldMin.getControl());
        bindingContext.removeBindings(cardinalityFieldMax.getControl());
        bindingContext.removeBindings(description);
        wizard.getExtFactoryAssociation().removeBinding(bindingContext);

        targetRoleSingularText.setText("");
        targetRolePluralText.setText("");
        cardinalityFieldMin.setText("");
        cardinalityFieldMax.setText("");
        targetText.setText("");
        typeText.setText("");
        description.setText("");
    }

    private void bindAllControls(IPolicyCmptTypeAssociation association) {
        bindingContext.bindContent(targetRoleSingularText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        bindingContext.bindContent(targetRolePluralText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        bindingContext.bindContent((Text)cardinalityFieldMin.getControl(), association, IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent((Text)cardinalityFieldMax.getControl(), association, IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(description, association, IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
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
    public void setShowExistingRelationDropDown(boolean showExisting) {
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
        setControlChangeable(!wizard.isExistingReverseRelation());
    }
    
    private void setControlChangeable(boolean changeable){
        toolkit.setDataChangeable(targetRoleSingularText, changeable);
        toolkit.setDataChangeable(targetRolePluralText, changeable);
        toolkit.setDataChangeable(cardinalityFieldMin.getControl(), changeable);
        toolkit.setDataChangeable(cardinalityFieldMax.getControl(), changeable);
    }
    
    /**
     * Event function to indicate a change of the existing relation.
     */
    private void existingRelationSelectionChanged() {
        int selIdx = existingRelCombo.getSelectionIndex();
        if (selIdx>=0){
            String selExistingRelation = existingRelCombo.getItem(selIdx);
            if (!selExistingRelation.equals(prevSelExistingRelation)){
                wizard.storeExistingInverseRelation(selExistingRelation);
                wizard.contentsChanged(this);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List getProperties() {
        return visibleProperties;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return <code>false</code> if no inverse association should be created or no existing
     *         relation exists otherwise <code>true</code>.
     */
    public boolean isPageVisible() {
        if (wizard.isNoneReverseRelation()) {
            return false;
        } else if (wizard.isExistingReverseRelation()) {
            List correspondingAssociations = wizard.getExistingInverseAssociationCandidates();
            if (correspondingAssociations.size() == 0) {
                return false;
            }
            if (correspondingAssociations.size() == 1 && correspondingAssociations.get(0).equals(association)) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible){
            wizard.handleInverseAssociationSelectionState();
            setPageComplete(canFlipToNextPage());
        }
        super.setVisible(visible);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }    
}
