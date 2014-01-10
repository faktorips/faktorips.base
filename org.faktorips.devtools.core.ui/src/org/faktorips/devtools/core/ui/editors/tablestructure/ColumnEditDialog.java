/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;

public class ColumnEditDialog extends IpsPartEditDialog {

    private IColumn column;

    private TextField nameField;
    private TextButtonField datatypeField;

    public ColumnEditDialog(IColumn column, Shell parentShell) {
        super(column, parentShell, Messages.ColumnEditDialog_title, true);
        this.column = column;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.ColumnEditDialog_pageTitle);
        page.setControl(createGeneralPage(folder));

        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, Messages.ColumnEditDialog_labelName);
        Text nameText = getToolkit().createText(workArea);
        nameText.setFocus();

        getToolkit().createFormLabel(workArea, Messages.ColumnEditDialog_labelDatatype);
        DatatypeRefControl datatypeControl = getToolkit().createDatatypeRefEdit(column.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setPrimitivesAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);

        // create fields
        nameField = new TextField(nameText);
        datatypeField = new TextButtonField(datatypeControl);

        return c;
    }

    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, IPolicyCmptTypeAttribute.PROPERTY_NAME);
        uiController.add(datatypeField, IPolicyCmptTypeAttribute.PROPERTY_DATATYPE);
    }

}
