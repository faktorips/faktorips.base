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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;

/**
 * A dialog to edit an association.
 */
public class AssociationEditDialog extends IpsPartEditDialog2 {

    /**
     * Keep track of the content of the name fields to be able to determine whether they have
     * changed.
     */
    private final String initialName;
    private final String initialPluralName;

    private IProductCmptTypeAssociation association;
    private PmoAssociation pmoAssociation;
    private ExtensionPropertyControlFactory extFactory;

    public AssociationEditDialog(IProductCmptTypeAssociation association, Shell parentShell) {
        super(association, parentShell, Messages.AssociationEditDialog_title, true);
        this.association = association;
        initialName = association.getName();
        initialPluralName = association.getTargetRolePlural();
        this.pmoAssociation = new PmoAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.AssociationEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite panel = createTabItemComposite(folder, 1, false);

        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_TOP);
        createGenerellGroup(uiToolkit.createGroup(panel, Messages.AssociationEditDialog_generalGroup));
        createDerivedUnionGroup(uiToolkit.createGroup(panel, Messages.AssociationEditDialog_derivedUnionGroup));
        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_BOTTOM);

        extFactory.bind(bindingContext);

        return panel;
    }

    private void createExtensionArea(Composite parent, String position) {
        if (!extFactory.needsToCreateControlsFor(association, position)) {
            return;
        }
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        extFactory.createControls(workArea, uiToolkit, association, position);
    }

    private void createGenerellGroup(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);

        // target
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_targetLabel);
        ProductCmptType2RefControl targetControl = new ProductCmptType2RefControl(association.getIpsProject(),
                workArea, uiToolkit, false);
        bindingContext.bindContent(targetControl, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        targetControl.setFocus();

        // aggregation kind
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_typeLabel);
        Combo typeCombo = uiToolkit.createCombo(workArea);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE,
                IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);

        // role singular
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_roleSingularLabel);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });

        // role plural
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_rolePluralLabel);
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });

        // min cardinality
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_minCardLabel);
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);

        // max cardinality
        uiToolkit.createFormLabel(workArea, Messages.AssociationEditDialog_maxCardLabel);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
    }

    private void createDerivedUnionGroup(Composite workArea) {
        Checkbox derivedUnion = uiToolkit.createCheckbox(workArea, Messages.AssociationEditDialog_derivedUnionCheckbox);
        bindingContext.bindContent(derivedUnion, association, IProductCmptTypeAssociation.PROPERTY_DERIVED_UNION);

        Checkbox subsetCheckbox = uiToolkit.createCheckbox(workArea, Messages.AssociationEditDialog_subsetCheckbox);
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        Composite temp = uiToolkit.createLabelEditColumnComposite(workArea);
        temp.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(temp, Messages.AssociationEditDialog_derivedUnionLabel);
        Text unionText = uiToolkit.createText(temp);
        bindingContext
                .bindContent(unionText, association, IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(unionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler
                .createHandlerForText(unionText, CompletionUtil.createContentAssistant(completionProcessor));
    }

    @Override
    protected void okPressed() {
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            String newName = association.getName();
            String newPluralName = association.getTargetRolePlural();
            if (!(newName.equals(initialName) && newPluralName.equals(initialPluralName))) {
                applyRenameRefactoring(newName, newPluralName);
            }
        }
        super.okPressed();
    }

    private void applyRenameRefactoring(String newName, String newPluralName) {
        // First, reset the initial names as otherwise errors 'names must not equal' will occur
        association.setTargetRoleSingular(initialName);
        association.setTargetRolePlural(initialPluralName);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                association, newName, newPluralName, false);
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(ipsRenameRefactoring, getShell());
        refactoringOperation.runDirectExecution();
    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset"; //$NON-NLS-1$

        private IProductCmptTypeAssociation association;
        private boolean subset;

        public PmoAssociation(IProductCmptTypeAssociation association) {
            super(association);
            this.association = association;
            subset = association.isSubsetOfADerivedUnion();
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
    }
}
