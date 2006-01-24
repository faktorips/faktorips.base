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
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog {
    
    private IProductCmptRelation relation;
    
    // edit fields
    private TextButtonField targetField;
    private IntegerField minCardinalityField;
    private TextField maxCardinalityField;

    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IProductCmptRelation relation, Shell parentShell) {
        super(relation, parentShell, "Edit Relation", true);
        this.relation = relation;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
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
        
        uiToolkit.createFormLabel(workArea, "Target:");
        ProductCmptRefControl targetControl = new ProductCmptRefControl(relation.getIpsProject(), workArea, uiToolkit);
        try {
            IRelation pcTypeRelation = relation.findPcTypeRelation();
            targetControl.setPolicyCmptType(pcTypeRelation.getTarget(), true);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        uiToolkit.createFormLabel(workArea, "Minimum Cardinality:");
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, "Maximum Cardinality:");
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        // create fields
        targetField = new TextButtonField(targetControl);
        minCardinalityField = new IntegerField(minCardinalityText);
        maxCardinalityField = new TextField(maxCardinalityText);

        return c;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(targetField, IRelation.PROPERTY_TARGET);
        uiController.add(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);
    }
}
