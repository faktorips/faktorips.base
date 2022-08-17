/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.tablestructure.IColumn;

public class ColumnEditDialog extends IpsPartEditDialog2 {

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
        bind();

        return c;
    }

    private void bind() {
        getBindingContext().bindContent(nameField, column, IPolicyCmptTypeAttribute.PROPERTY_NAME);
        getBindingContext().bindContent(datatypeField, column, IPolicyCmptTypeAttribute.PROPERTY_DATATYPE);
    }

}
