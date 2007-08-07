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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog2 extends IpsPartEditDialog2 {
    
    private IRelation relation;
    private ExtensionPropertyControlFactory extFactory;
    
    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog2(IRelation relation, Shell parentShell) {
        super(relation, parentShell, Messages.RelationEditDialog_title, true );
        this.relation = relation;
        extFactory = new ExtensionPropertyControlFactory(relation.getClass());
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
        
        Group groupPolicySide = uiToolkit.createGroup(c, Messages.RelationEditDialog_GroupLabel_PolicySide);
        createPolicySide(groupPolicySide);
        
        uiToolkit.createVerticalSpacer(c, 12);
        
        Group groupProductSide = uiToolkit.createGroup(c, Messages.RelationEditDialog_GroupLabel_ProductSide);
        createProductSide(groupProductSide);
        
        return c;
    }

    /*
     * Creates the general contols:<ul>
     * <li>target
     * <li>type
     * <li>read only container
     * <li>container relation
     * <li>reverse relation
     * </ul>
     */
    private void createGeneralControls(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // target
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTarget);
        PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(relation.getIpsProject(), workArea);
        bindingContext.bindContent(targetControl, relation, IRelation.PROPERTY_TARGET);
        
        // type
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelType);
        final Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
        bindingContext.bindContent(typeCombo, relation, IRelation.PROPERTY_RELATIONTYPE, RelationType.getEnumType());
        typeCombo.setFocus();
        
        // read only container
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReadOnlyContainer);
        Checkbox containerCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(containerCheckbox, relation, IRelation.PROPERTY_READONLY_CONTAINER);
        bindingContext.bindEnabled(containerCheckbox, relation, IRelation.PROPERTY_CONTAINER_RELATION_APPLICABLE);
        
        // container relation
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelContainerRel);
        Text containerRelationText = uiToolkit.createText(workArea);
        bindingContext.bindContent(containerRelationText, relation, IRelation.PROPERTY_CONTAINER_RELATION);
        bindingContext.bindEnabled(containerRelationText, relation, IRelation.PROPERTY_CONTAINER_RELATION_APPLICABLE);
        ContainerRelationCompletionProcessor completionProcessor = new ContainerRelationCompletionProcessor(relation);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(containerRelationText, CompletionUtil.createContentAssistant(completionProcessor));
        
        // inverse relation
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReverseRel);
        Text reverseRelationText = uiToolkit.createText(workArea);
        bindingContext.bindContent(reverseRelationText, relation, IRelation.PROPERTY_INVERSE_RELATION);
        bindingContext.bindEnabled(reverseRelationText, relation, IRelation.PROPERTY_INVERSE_RELATION_APPLICABLE);
        ReverseRelationCompletionProcessor reverseRelationCompletionProcessor = new ReverseRelationCompletionProcessor(relation);
        reverseRelationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(reverseRelationText, CompletionUtil.createContentAssistant(reverseRelationCompletionProcessor));
    }

    /*
     * Creates the policy side controls.:
     * <ul>
     * <li>top extension controls
     * <li>role singular
     * <li>role plural
     * <li>min cardinality
     * <li>max cardinality
     * <li>bottom extensions
     * </ul>
     */
    private void createPolicySide(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        extFactory.createControls(workArea, uiToolkit, (IIpsObjectPartContainer)relation, "top"); //$NON-NLS-1$
                
        // role singular
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRoleSingular);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, relation, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(relation.getTargetRoleSingular())) {
                    relation.setDefaultTargetRoleSingular();
                }
            }
        });
        
        // role plural
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRolePlural);
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, relation, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText())) {
                    relation.setDefaultTargetRolePlural();
                }
            }
        });
        
        // min cardinality
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, relation, IRelation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, relation, IRelation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        extFactory.createControls(workArea, uiToolkit, (IIpsObjectPartContainer)relation);
        extFactory.bind(bindingContext);
    }
    
    /*
     * Creates the product side controls:<ul>
     * <li>product relevant
     * <li>role singular
     * <li>role plural
     * <li>min cardinality
     * <li>max cardinality
     * </ul>
     */    
    private void createProductSide(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelProdRelevant);
        final Checkbox productRelevantCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(productRelevantCheckbox, relation, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRoleSingular);
        Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, relation, IRelation.PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE);
        bindingContext.bindEnabled(targetRoleSingularText, relation, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRolePlural);
        Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, relation, IRelation.PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE);
        bindingContext.bindEnabled(targetRolePluralText, relation, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        bindingContext.bindContent(new CardinalityField(minCardinalityText), relation, IRelation.PROPERTY_MIN_CARDINALITY_PRODUCTSIDE);
        bindingContext.bindEnabled(minCardinalityText, relation, IRelation.PROPERTY_PRODUCT_RELEVANT);
                
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        bindingContext.bindContent(new CardinalityField(maxCardinalityText), relation, IRelation.PROPERTY_MAX_CARDINALITY_PRODUCTSIDE);
        bindingContext.bindEnabled(maxCardinalityText, relation, IRelation.PROPERTY_PRODUCT_RELEVANT);
    }
    
}
