/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.core.runtime.CoreException;
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
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.ColumnEditDialog_pageTitle);
        page.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);

        uiToolkit.createFormLabel(workArea, Messages.ColumnEditDialog_labelName);
        Text nameText = uiToolkit.createText(workArea);
        nameText.setFocus();

        uiToolkit.createFormLabel(workArea, Messages.ColumnEditDialog_labelDatatype);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(column.getIpsProject(), workArea);
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
