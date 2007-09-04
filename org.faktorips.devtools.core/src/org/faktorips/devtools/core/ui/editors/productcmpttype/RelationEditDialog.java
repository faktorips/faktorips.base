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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.productcmpttype2.IRelation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog2 {
    
    private IRelation relation;
    private ExtensionPropertyControlFactory extFactory;
    
    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IRelation relation, Shell parentShell) {
        super(relation, parentShell, "Edit Relation", true );
        this.relation = relation;
        extFactory = new ExtensionPropertyControlFactory(relation.getClass());
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText("Properties");
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
    	Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        extFactory.createControls(workArea, uiToolkit, relation, IExtensionPropertyDefinition.POSITION_TOP); //$NON-NLS-1$
                
        // target
        uiToolkit.createFormLabel(workArea, "Target");
        ProductCmptType2RefControl targetControl = new ProductCmptType2RefControl(relation.getIpsProject(), workArea, uiToolkit, true);
        bindingContext.bindContent(targetControl, relation, IRelation.PROPERTY_TARGET);
        
        // type
//        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelType);
//        final Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
//        bindingContext.bindContent(typeCombo, relation, IRelation.PROPERTY_RELATIONTYPE, RelationType.getEnumType());
//        typeCombo.setFocus();
        
        // read only container
        uiToolkit.createFormLabel(workArea, "Is read-only container:");
        Checkbox containerCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(containerCheckbox, relation, IRelation.PROPERTY_READ_ONLY_CONTAINER);
        
        // container relation
        uiToolkit.createFormLabel(workArea, "Implemented container relation:");
        Text containerRelationText = uiToolkit.createText(workArea);
        bindingContext.bindContent(containerRelationText, relation, IRelation.PROPERTY_IMPLEMENTED_CONTAINER_RELATION);
        ContainerRelationCompletionProcessor completionProcessor = new ContainerRelationCompletionProcessor(relation);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(containerRelationText, CompletionUtil.createContentAssistant(completionProcessor));
        
        // role singular
        uiToolkit.createFormLabel(workArea, "Target role singular:");
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, relation, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(relation.getTargetRoleSingular())) {
                    relation.setTargetRoleSingular(relation.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        uiToolkit.createFormLabel(workArea, "Target role plural:");
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, relation, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && relation.isTargetRolePluralRequired()) {
                    relation.setTargetRolePlural(relation.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        uiToolkit.createFormLabel(workArea, "Min Cardinality");
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, relation, IRelation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        uiToolkit.createFormLabel(workArea, "Max Cardinality");
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, relation, IRelation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        extFactory.createControls(workArea, uiToolkit, relation, IExtensionPropertyDefinition.POSITION_BOTTOM); //$NON-NLS-1$
        extFactory.bind(bindingContext);
        return c;
    }
    
}
