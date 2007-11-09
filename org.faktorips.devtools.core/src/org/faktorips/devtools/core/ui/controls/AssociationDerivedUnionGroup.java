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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;

/**
 * Group composite to edit the association derived union properties.
 * 
 * @author Joerg Ortmann
 */
public class AssociationDerivedUnionGroup extends Composite {

    private Checkbox containerCheckbox;
    private Checkbox subsetCheckbox;
    private Text derivedUnionText;

    private PmoAssociation pmoAssociation;

    public AssociationDerivedUnionGroup(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent, IAssociation association) {
        super(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
        
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        this.setLayoutData(gridData);
        
        Group groupDerived = uiToolkit.createGroup(this, "Derived union");
        ((GridData)groupDerived.getLayoutData()).grabExcessVerticalSpace = false;
        createDerivedUnionControls(uiToolkit, bindingContext, groupDerived, association);
    }

    private void createDerivedUnionControls(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent, IAssociation association) {

        // derived union checkbox
        containerCheckbox = uiToolkit.createCheckbox(parent, "This association is a derived union");
        
        // is subset checkbox
        subsetCheckbox = uiToolkit.createCheckbox(parent, "This association defines a subset of a derived union");
        
        // subset text field
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, "Derived union:");
        derivedUnionText = uiToolkit.createText(workArea);

        bindContent(bindingContext, association);
    }

    /**
     * Binds all controls to the given binding context.
     * 
     * @param bindingContext The binding context all controls will be cound to
     * 
     * @param association The associated which will be bound to the given context and controls
     */
    public void bindContent(BindingContext bindingContext, IAssociation association) {
        // bind controls, only if a binding context and an association is available
        if (bindingContext == null || association == null){
            return;
        }
        pmoAssociation = new PmoAssociation(association);
        
        addCompletionProcessor(association);
        
        bindingContext.bindContent(containerCheckbox, association, IAssociation.PROPERTY_DERIVED_UNION);
        
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        
        bindingContext.bindContent(derivedUnionText, association, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        
        // special binding for policy cmpt type associations only
        //   derived union is only enabled if @see IPolicyCmptTypeAssociation.isContainerRelationApplicable()
        if (association instanceof IPolicyCmptTypeAssociation){
            bindingContext.bindEnabled(containerCheckbox, association, IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
            bindingContext.bindEnabled(subsetCheckbox, association, IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
        }
    }
    
    private void addCompletionProcessor(IAssociation association){
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(derivedUnionText, CompletionUtil.createContentAssistant(completionProcessor));
    }

    public class PmoAssociation extends IpsObjectPartPmo {
        public final static String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$

        private IAssociation association;

        private boolean subset;
        
        public PmoAssociation(IAssociation association) {
            super(association);
            this.association = association;
            this.subset = association.isSubsetOfADerivedUnion();
        }
        
        public boolean isSubset() {
            return subset;
        }
        
        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion(""); //$NON-NLS-1$
            }
            notifyListeners();
        }

        /**
         * {@inheritDoc}
         */
        protected void partHasChanged() {
            // special handling of policy component type associations
            if (association instanceof IPolicyCmptTypeAssociation) {
                if (! ((IPolicyCmptTypeAssociation)association).isContainerRelationApplicable()) {
                    subset = false;
                }
            }
        }
    }
}
