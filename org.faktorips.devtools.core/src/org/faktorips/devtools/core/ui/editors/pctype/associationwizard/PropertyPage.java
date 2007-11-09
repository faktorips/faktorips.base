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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.AssociationQualificationGroup;

/**
 * Page to specify the properties of the association.
 * 
 * @author Joerg Ortmann
 */
public class PropertyPage extends WizardPage implements IBlockedValidationWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList visibleProperties = new ArrayList(10);
    
    private Text targetRoleSingularText;
    private AssociationQualificationGroup associationQualificationGroup;
    private Label noteAboutProductStructureConstrained;
    
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
        Composite pageComposite = wizard.createPageComposite(parent);
        
        createGeneralControls(pageComposite);
        
        associationQualificationGroup = new AssociationQualificationGroup(toolkit, bindingContext, pageComposite, association);

        // bind the special note label
        associationQualificationGroup.bindLabelAboutConstrainedByProductStructure(noteAboutProductStructureConstrained, bindingContext);
        
        setControl(pageComposite);
    }
    
    private void createGeneralControls(Composite parent) {
        Group groupGeneral = toolkit.createGroup(parent, "Properties");
        GridData gd = (GridData)groupGeneral.getLayoutData();
        gd.grabExcessVerticalSpace = false;
        
        Composite workArea = toolkit.createLabelEditColumnComposite(groupGeneral);
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
        
        Composite info = toolkit.createGridComposite(groupGeneral, 1, true, false);
        
        // create note about constrained by product structure
        noteAboutProductStructureConstrained = toolkit.createLabel(info, "");

        // bottom extensions
        wizard.getExtFactoryAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        wizard.getExtFactoryAssociation().bind(bindingContext);
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
    public List getProperties() {
        return visibleProperties;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }
}
