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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog {
    
    private IProductCmptLink link;
    
    // edit fields
    private TextButtonField targetField;
    private CardinalityField minCardinalityField;
    private CardinalityField maxCardinalityField;

    private ProductCmptRefControl targetControl;
    private IProductCmpt[] toExclude = new IProductCmpt[0];
    
    public RelationEditDialog(IProductCmptLink link, Shell parentShell) {
        super(link, parentShell, Messages.RelationEditDialog_editRelation, true);
        this.link = link;
    }

    /** 
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.RelationEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_target);
        targetControl = new ProductCmptRefControl(link.getIpsProject(), workArea, uiToolkit);
        try {
            IProductCmptTypeAssociation typeRelation = link.findAssociation(link.getIpsProject());
            if (typeRelation != null) {
                IProductCmptType productCmptType = typeRelation.findTargetProductCmptType(link.getIpsProject());
                if (productCmptType != null) {
                    targetControl.setProductCmptType(productCmptType, true);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        targetControl.setProductCmptsToExclude(this.toExclude);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMin);
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMax);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        // create fields
        targetField = new TextButtonField(targetControl);
        minCardinalityField = new CardinalityField(minCardinalityText);
        maxCardinalityField = new CardinalityField(maxCardinalityText);

        return c;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(targetField, IRelation.PROPERTY_TARGET);
        uiController.add(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);
    }
    
    public void setProductCmptsToExclude(IProductCmpt[] toExclude) {
    	if (targetControl != null) {
    		targetControl.setProductCmptsToExclude(toExclude);
    	}
    	else {
    		this.toExclude = toExclude;
    	}
    }
}
