/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.devtools.core.util.QNameUtil;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog2 {
    
    private IIpsProject ipsProject;
    private IPolicyCmptTypeAssociation association;
    private PmoAssociation pmoAssociation;
    
    private ExtensionPropertyControlFactory extFactory;
    
    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IPolicyCmptTypeAssociation relation2, Shell parentShell) {
        super(relation2, parentShell, Messages.RelationEditDialog_title, true );
        this.association = relation2;
        this.ipsProject = association.getIpsProject();
        pmoAssociation = new PmoAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.RelationEditDialog_propertiesTitle);
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    /**
     * Creates the first tab page. With the following goups: general, policy side, and product side.
     */
    private Control createFirstPage(TabFolder folder) {
        
    	Composite c = createTabItemComposite(folder, 1, false);
        
        Group groupGeneral = uiToolkit.createGroup(c, Messages.RelationEditDialog_GroupLabel_General);
    	createGeneralControls(groupGeneral);
        
        uiToolkit.createVerticalSpacer(c, 12);
        
        // Group groupPolicySide = uiToolkit.createGroup(c, Messages.RelationEditDialog_GroupLabel_PolicySide);
        createDerivedUnionGroup(uiToolkit.createGroup(c, "Derived union"));
        
        uiToolkit.createVerticalSpacer(c, 12);
        
        createQualificationGroup(uiToolkit.createGroup(c, "Qualification"));
        
        return c;
    }

    private void createGeneralControls(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        extFactory.createControls(workArea, uiToolkit, association, IExtensionPropertyDefinition.POSITION_TOP); //$NON-NLS-1$
                
        // target
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTarget);
        PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(association.getIpsProject(), workArea);
        bindingContext.bindContent(targetControl, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET);
        
        // type
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelType);
        final Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
        bindingContext.bindContent(typeCombo, association, IPolicyCmptTypeAssociation.PROPERTY_RELATIONTYPE, RelationType.getEnumType());
        typeCombo.setFocus();
        
        // role singular
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRoleSingular);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRolePlural);
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);

        // inverse relation
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReverseRel);
        Text reverseRelationText = uiToolkit.createText(workArea);
        bindingContext.bindContent(reverseRelationText, association, IPolicyCmptTypeAssociation.PROPERTY_INVERSE_RELATION);
        bindingContext.bindEnabled(reverseRelationText, association, IPolicyCmptTypeAssociation.PROPERTY_INVERSE_RELATION_APPLICABLE);
        ReverseRelationCompletionProcessor reverseRelationCompletionProcessor = new ReverseRelationCompletionProcessor(association);
        reverseRelationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(reverseRelationText, CompletionUtil.createContentAssistant(reverseRelationCompletionProcessor));
        
        Composite info = uiToolkit.createGridComposite(c, 1, true, false);
        Label note = uiToolkit.createLabel(info, pmoAssociation.getConstrainedNote());
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAINED_NOTE);
        
        // bottom extensions
        extFactory.createControls(workArea, uiToolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM); //$NON-NLS-1$
        extFactory.bind(bindingContext);
    }

    private void createDerivedUnionGroup(Composite c) {

        // derived union checkbox
        Checkbox containerCheckbox = uiToolkit.createCheckbox(c, "This association is a derived union");
        // uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReadOnlyContainer);
        bindingContext.bindContent(containerCheckbox, association, IAssociation.PROPERTY_DERIVED_UNION);
        bindingContext.bindEnabled(containerCheckbox, association, IPolicyCmptTypeAssociation.PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE);
        
        // is subset checkbox
        // uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelContainerRel);
        Checkbox subsetCheckbox = uiToolkit.createCheckbox(c, "This association defines a subset of a derived union");
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, "Derived union:");
        Text derivedUnion = uiToolkit.createText(workArea);
        bindingContext.bindContent(derivedUnion, association, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(derivedUnion, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(derivedUnion, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    private void createQualificationGroup(Composite c) {
        Composite workArea = uiToolkit.createGridComposite(c, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Checkbox qualifiedCheckbox = uiToolkit.createCheckbox(workArea);
        uiToolkit.createFormLabel(workArea, "Note: For qualified associations multiplicty is defined per qualified instance.");
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_LABEL));
    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset";
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel";
        public final static String PROPERTY_CONSTRAINED_NOTE = "constrainedNote";

        private boolean subset;
        
        public PmoAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
            subset = association.isSubsetOfADerivedUnion();
        }
        
        public boolean isSubset() {
            return subset;
        }
        
        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion("");
            }
            notifyListeners();
        }
        
        public String getQualificationLabel() {
            String label = "This association is qualified";
            try {
                IPolicyCmptType type = association.findTargetPolicyCmptType(ipsProject);
                if (type!=null) {
                    String productCmptType = QNameUtil.getUnqualifiedName(type.getProductCmptType());
                    return label + " by type '" + productCmptType + "'";
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return label;            
        }

        public String getConstrainedNote() {
            try {
                IProductCmptTypeAssociation matchingAss = association.findMatchingProductCmptTypeAssociation(ipsProject);
                if (matchingAss!=null) {
                    String type = matchingAss.getProductCmptType().getName();
                    return "Note: This association is constrained by product structure. " 
                    +" The matching \nassociation in type '" + type + "' is '" + matchingAss.getTargetRoleSingular() + "' (rolename)."
                    + StringUtils.rightPad("\n", 120); 
                } else {
                    String note = "Note: This association is not constrained by product structure."; 
                    IProductCmptType sourceProductType = association.getPolicyCmptType().findProductCmptType(ipsProject);
                    IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                    if (sourceProductType!=null && targetType!=null) {
                        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                        if (targetProductType!=null) {
                            return note + "\nTo constrain the association by product structure create an association between the "
                                + "\nproduct component types '" + sourceProductType.getName() + "' and '" + targetProductType.getName() + "'.";
                        }
                    }
                    return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120) ;
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return "";
            }
            
        }
        
    }
}
