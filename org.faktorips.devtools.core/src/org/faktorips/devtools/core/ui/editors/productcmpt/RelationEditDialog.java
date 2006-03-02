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
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog {
    
    private IProductCmptRelation relation;
    
    // edit fields
    private TextButtonField targetField;
    private CardinalityField minCardinalityField;
    private CardinalityField maxCardinalityField;

    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IProductCmptRelation relation, Shell parentShell) {
        super(relation, parentShell, Messages.RelationEditDialog_editRelation, true);
        this.relation = relation;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
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
        ProductCmptRefControl targetControl = new ProductCmptRefControl(relation.getIpsProject(), workArea, uiToolkit);
        try {
            IProductCmptTypeRelation typeRelation = relation.findProductCmptTypeRelation();
            if (typeRelation != null) {
            	String targetPolicyCmptType = typeRelation.findTarget().findPolicyCmptyType().getQualifiedName();
            	targetControl.setPolicyCmptType(targetPolicyCmptType, true);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
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
     * Overridden.
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(targetField, IRelation.PROPERTY_TARGET);
        uiController.add(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);
    }
}
