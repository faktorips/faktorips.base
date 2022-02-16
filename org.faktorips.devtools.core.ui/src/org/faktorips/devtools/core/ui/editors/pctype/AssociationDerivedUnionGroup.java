/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

/**
 * Group composite to edit the association derived union properties.
 * 
 * @author Joerg Ortmann
 */
public class AssociationDerivedUnionGroup extends Composite {

    private Checkbox derivedUnionCheckbox;

    private Checkbox subsetCheckbox;

    private Combo derivedUnionCombo;

    private PmoAssociation pmoAssociation;

    private Checkbox constrainCheckBox;

    public AssociationDerivedUnionGroup(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent,
            IAssociation association) {

        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        setLayoutData(gridData);

        Group groupDerived = uiToolkit.createGroup(this, Messages.AssociationDerivedUnionGroup_groupDerivedUnion);
        ((GridData)groupDerived.getLayoutData()).grabExcessVerticalSpace = false;
        createDerivedUnionControls(uiToolkit, bindingContext, groupDerived, association);
    }

    private void createDerivedUnionControls(UIToolkit uiToolkit,
            BindingContext bindingContext,
            Composite parent,
            IAssociation association) {

        // constrain checkbox
        constrainCheckBox = uiToolkit.createCheckbox(parent, Messages.AssociationEditDialog_constrain);
        constrainCheckBox.setToolTipText(Messages.AssociationDerivedUnionGroup_toolTipOverride);

        // derived union checkbox
        derivedUnionCheckbox = uiToolkit.createCheckbox(parent,
                Messages.AssociationDerivedUnionGroup_labelIsDerivedUnion);
        derivedUnionCheckbox.setToolTipText(Messages.AssociationDerivedUnionGroup_toolTipDerivedUnion);

        // is subset checkbox
        subsetCheckbox = uiToolkit.createCheckbox(parent,
                Messages.AssociationDerivedUnionGroup_labelDefinesSubsetOfDerivedUnion);
        subsetCheckbox.setToolTipText(Messages.AssociationDerivedUnionGroup_toolTipDerivedUnion);

        // subset combo field
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, Messages.AssociationDerivedUnionGroup_labelSubsetTextField);
        derivedUnionCombo = new Combo(workArea, SWT.DROP_DOWN);
        derivedUnionCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

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
        if (bindingContext == null || association == null) {
            return;
        }
        pmoAssociation = new PmoAssociation(association);

        bindingContext.bindContent(constrainCheckBox, association, IAssociation.PROPERTY_CONSTRAIN);
        bindingContext.bindEnabled(constrainCheckBox, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAIN_ENABLED);

        addCompletionProcessor(association);

        bindingContext.bindContent(derivedUnionCheckbox, association, IAssociation.PROPERTY_DERIVED_UNION);

        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        bindingContext.bindContent(new StringValueComboField(derivedUnionCombo), association,
                IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnionCombo, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        // special binding for policy cmpt type associations only
        // derived union is only enabled if @see
        // IPolicyCmptTypeAssociation.isContainerRelationApplicable()
        if (association instanceof IPolicyCmptTypeAssociation) {
            bindingContext.bindEnabled(derivedUnionCheckbox, association,
                    IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
            bindingContext.bindEnabled(subsetCheckbox, association,
                    IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
        }
    }

    /**
     * Sets the available derived union association in the drop down.
     */
    public void setDerivedUnions(String[] derivedUnions) {
        if (!derivedUnionCombo.isDisposed()) {
            derivedUnionCombo.setItems(derivedUnions);
            if (derivedUnions.length > 0) {
                derivedUnionCombo.select(0);
            }
        }
    }

    /**
     * Sets the defaults. If subset is <code>true</code> then the subset checkbox will be checked
     * otherwise it will be unchecked.
     */
    public void setDefaultSubset(boolean subset) {
        pmoAssociation.setSubset(subset);
    }

    private void addCompletionProcessor(IAssociation association) {
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForCombo(derivedUnionCombo, completionProcessor);
    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$
        public static final String PROPERTY_CONSTRAIN_ENABLED = "constrainEnabled"; //$NON-NLS-1$

        private String previousTarget;

        private IAssociation association;

        private boolean subset;

        private boolean derivedUnionsInitialized;

        public PmoAssociation(IAssociation association) {
            super(association);
            this.association = association;
            subset = association.isSubsetOfADerivedUnion();
            AssociationDerivedUnionGroup.this.addDisposeListener($ -> dispose());
            initDerivedUnionCandidates();
        }

        public boolean isSubset() {
            return subset;
        }

        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset || derivedUnionCombo.getItemCount() == 0) {
                association.setSubsettedDerivedUnion(StringUtils.EMPTY);
            } else {
                association.setSubsettedDerivedUnion(derivedUnionCombo.getItem(0));
                if (association instanceof IProductCmptTypeAssociation) {
                    IProductCmptTypeAssociation subsettedDerivedUnion = (IProductCmptTypeAssociation)association
                            .findSubsettedDerivedUnion(association.getIpsProject());
                    ((IProductCmptTypeAssociation)association).setChangingOverTime(subsettedDerivedUnion
                            .isChangingOverTime());
                }
            }
            notifyListeners();
        }

        @Override
        protected void partHasChanged() {
            initDerivedUnionCandidates();

            // special handling for policy component type associations
            if (association instanceof IPolicyCmptTypeAssociation) {
                partHasChangedFor((IPolicyCmptTypeAssociation)association);
            }
        }

        private void initDerivedUnionCandidates() {
            Set<String> derivedUnions = new LinkedHashSet<>(1);
            if (association.isSubsetOfADerivedUnion()) {
                derivedUnions.add(association.getSubsettedDerivedUnion());
            }

            // set derived union candidates
            String currentTarget = association.getTarget();
            if (StringUtils.isEmpty(currentTarget)) {
                setDerivedUnions(derivedUnions.toArray(new String[derivedUnions.size()]));
                derivedUnionsInitialized = true;
                return;
            }

            if (!currentTarget.equals(previousTarget)) {
                previousTarget = currentTarget;

                try {
                    // init drop down with available candidates
                    IAssociation[] associations = association.findDerivedUnionCandidates(association.getIpsProject());
                    for (IAssociation association2 : associations) {
                        derivedUnions.add(association2.getName());
                    }
                    setDerivedUnions(derivedUnions.toArray(new String[derivedUnions.size()]));

                    // if at least one derived union candidate is available
                    // then set the first derived union as default
                    if (associations.length > 0 && !association.isSubsetOfADerivedUnion() && derivedUnionsInitialized) {
                        setSubset(true);
                    }
                    derivedUnionsInitialized = true;
                } catch (IpsException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }

        private void partHasChangedFor(IPolicyCmptTypeAssociation association) {
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = association;
            // enable subset
            if (!policyCmptTypeAssociation.isDerivedUnionApplicable()) {
                subset = false;
            }
        }

        public boolean isConstrainEnabled() {
            if (StringUtils.isEmpty(association.getType().getSupertype())) {
                // only disable the checkbox when it's not checked
                // because there is no way to remove the mark.
                if (!association.isConstrain()) {
                    return false;
                }
            }
            if (association.isDerived() || isSubset()) {
                return false;
            }
            if (AssociationType.COMPOSITION_DETAIL_TO_MASTER.equals(association.getAssociationType())) {
                return false;
            }
            return true;
        }
    }

}
