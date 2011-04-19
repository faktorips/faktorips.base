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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;

/**
 * Group composite to edit the association derived union properties.
 * 
 * @author Joerg Ortmann
 */
public class AssociationDerivedUnionGroup extends Composite {

    private Checkbox containerCheckbox;
    private Checkbox subsetCheckbox;

    private PmoAssociation pmoAssociation;
    private Combo derivedUnionCombo;

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

        // derived union checkbox
        containerCheckbox = uiToolkit.createCheckbox(parent, Messages.AssociationDerivedUnionGroup_labelIsDerivedUnion);

        // is subset checkbox
        subsetCheckbox = uiToolkit.createCheckbox(parent,
                Messages.AssociationDerivedUnionGroup_labelDefinesSubsetOfDerivedUnion);

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

        addCompletionProcessor(association);

        bindingContext.bindContent(containerCheckbox, association, IAssociation.PROPERTY_DERIVED_UNION);

        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        bindingContext.bindContent(new StringValueComboField(derivedUnionCombo), association,
                IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnionCombo, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        // special binding for policy cmpt type associations only
        // derived union is only enabled if @see
        // IPolicyCmptTypeAssociation.isContainerRelationApplicable()
        if (association instanceof IPolicyCmptTypeAssociation) {
            bindingContext.bindEnabled(containerCheckbox, association,
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
        }
    }

    /**
     * Sets the defaults. If subset is <code>true</code> then the subset checkbox will be checked
     * otherwise it will be unchecked.
     */
    public void setDefaultSubset(boolean subset) {
        pmoAssociation.setSubset(subset);
        pmoAssociation.ignoreAutomaticSubsetEnabling();
    }

    private void addCompletionProcessor(IAssociation association) {
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForCombo(derivedUnionCombo, completionProcessor);
    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$

        public String previousTarget = ""; //$NON-NLS-1$

        private IAssociation association;

        private boolean subset;
        private boolean ignoreAutomaticSubsetEnabling = false;

        public PmoAssociation(IAssociation association) {
            super(association);
            this.association = association;
            subset = association.isSubsetOfADerivedUnion();
        }

        /**
         * Ignores the automatically enabling of the subset checkbox.
         */
        public void ignoreAutomaticSubsetEnabling() {
            ignoreAutomaticSubsetEnabling = true;
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

        @Override
        protected void partHasChanged() {
            initDerivedUnionCandidates(association);

            // special handling for policy component type associations
            if (association instanceof IPolicyCmptTypeAssociation) {
                partHasChangedFor((IPolicyCmptTypeAssociation)association);
            }
        }

        private void initDerivedUnionCandidates(IAssociation policyCmptTypeAssociation) {
            // set derived union candidates
            String currentTarget = policyCmptTypeAssociation.getTarget();
            if (StringUtils.isEmpty(currentTarget)) {
                setDerivedUnions(new String[0]);
                return;
            }

            if (!currentTarget.equals(previousTarget)) {
                previousTarget = currentTarget;
                try {
                    // init drop down with available candidates
                    IAssociation[] associations = policyCmptTypeAssociation.findDerivedUnionCandidates(association
                            .getIpsProject());
                    String[] derivedUnionCandidates = new String[associations.length];
                    for (int i = 0; i < associations.length; i++) {
                        derivedUnionCandidates[i] = associations[i].getName();
                    }
                    setDerivedUnions(derivedUnionCandidates);

                    if (ignoreAutomaticSubsetEnabling) {
                        return;
                    }

                    // if at least one derived union candidate is available
                    // then set the first derived union as default
                    if (associations.length > 0) {
                        subset = true;
                        association.setSubsettedDerivedUnion(associations[0].getName());
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }

        private void partHasChangedFor(IPolicyCmptTypeAssociation association) {
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = association;
            // enable subset
            if (!policyCmptTypeAssociation.isContainerRelationApplicable()) {
                subset = false;
            }
        }
    }
}
