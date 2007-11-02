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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.pctype.associationwizard.NewPcTypeAssociationWizard.PmoAssociation;

public class PropertyPage extends WizardPage implements IBlockedValidationWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList visibleProperties = new ArrayList(10);
    
    private Text targetRoleSingularText;
    private boolean visibleBefore;
    
    protected PropertyPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association, UIToolkit toolkit, BindingContext bindingContext) {
        super("PropertyPage", "Association properties", null);
        super.setDescription("Define association properties");
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        
        setPageComplete(false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite workArea = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(1, false);
        workArea.setLayout(layout);
        
        Group groupGeneral = toolkit.createGroup(workArea, "Properties");
        GridData gd = (GridData)groupGeneral.getLayoutData();
        gd.grabExcessVerticalSpace = false;
        
        createGeneralControls(groupGeneral);
        
        Group groupQualification = toolkit.createGroup(workArea, "Qualification");
        createQualificationGroup(groupQualification);
        gd = (GridData)groupQualification.getLayoutData();
        gd.grabExcessVerticalSpace = false;
        
        targetRoleSingularText.setFocus();
        
        setControl(workArea);
    }
    
    private void createQualificationGroup(Composite c) {
        Composite workArea = toolkit.createGridComposite(c, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Checkbox qualifiedCheckbox = toolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, wizard.getPmoAssociation(), PmoAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = toolkit.createFormLabel(workArea, StringUtils.rightPad("", 120));
        bindingContext.bindContent(note, wizard.getPmoAssociation(), PmoAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, wizard.getPmoAssociation(), PmoAssociation.PROPERTY_QUALIFICATION_LABEL));
    }
    
    private void createGeneralControls(Composite c) {
        Composite workArea = toolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularText = toolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });
        
        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        final Text targetRolePluralText = toolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRolePlural();
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        wizard.getExtFactoryAssociation().bind(bindingContext);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        setErrorMessage(null);
        boolean valid = wizard.isValidPage(this, false);
        
        if (getNextPage() == null){
            return false;
        }
        
        return valid;
    }

    public List getProperties() {
        return visibleProperties;
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
    
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible && !visibleBefore){
            visibleBefore = true;
            updateDefaultTargetRoleSingular();
            updateDefaultTargetRolePlural();
        }
        super.setVisible(visible);
    }
}
