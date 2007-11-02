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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;

public class AssociationTargetPage extends WizardPage implements IBlockedValidationWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;
    
    private ArrayList visibleProperties = new ArrayList(10);
    
    protected AssociationTargetPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association, UIToolkit toolkit, BindingContext bindingContext) {
        super("AssociationTargetPage", "Target", null);
        super.setDescription("Select the target and the association type");
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        setPageComplete(false);
    }

    public void createControl(Composite parent) {
        Composite main = toolkit.createLabelEditColumnComposite(parent);
        main.setLayoutData(new GridData(GridData.FILL_BOTH));

        // target
        toolkit.createFormLabel(main, "Target");
        PcTypeRefControl targetControl = toolkit.createPcTypeRefControl(association.getIpsProject(), main);
        bindingContext.bindContent(targetControl, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET);
        
        // type
        toolkit.createFormLabel(main, "Type");
        Combo typeCombo = toolkit.createCombo(main, AssociationType.getEnumType());
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE, AssociationType.getEnumType());
        typeCombo.select(0);
        visibleProperties.add(IAssociation.PROPERTY_ASSOCIATION_TYPE);
        
        // derived union checkbox
        toolkit.createFormLabel(main, "Derived union");
        final Checkbox derivedUnionCheckbox = toolkit.createCheckbox(main);        
        bindingContext.bindContent(derivedUnionCheckbox, association, IPolicyCmptTypeAssociation.PROPERTY_DERIVED_UNION);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnionCheckbox, association, IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
        
        // description
        Text text = wizard.createDescriptionText(main);
        bindingContext.bindContent(text, association, IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
        setControl(main);
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
}
