/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;

/**
 * The {@link EditTableControlUIBuilder} is a factory for UI controls that holds references to the
 * created controls. It creates modifiable tables with "Add", "Remove", "Move Up" and "Move Down"
 * buttons at the side.
 * <p>
 * The table have two columns but only one column (the second) is used to show the object. The first
 * column is used to show any validation status icons like warnings and errors.
 * 
 * This class replaces the {@link EditTableControl}.
 * 
 * @see EditTableControl
 * 
 * @since 3.7
 * 
 * @author Stefan Widmaier
 */
public class EditTableControlUIBuilder {

    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    private Table table;
    private Label tableLabel;

    public void createTableEditControl(Composite parent) {
        Composite rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        rootComposite.setLayout(layout);

        tableLabel = new Label(rootComposite, SWT.NONE);
        GridData labelGd = new GridData();
        labelGd.horizontalSpan = 2;
        tableLabel.setLayoutData(labelGd);

        createTable(rootComposite);
        createButtonComposite(rootComposite);
    }

    private void createTable(Composite parent) {
        TableLayoutComposite layouter = new TableLayoutComposite(parent, SWT.NONE);
        addColumnLayoutData(layouter);
        table = new Table(layouter, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 40;
        layouter.setLayoutData(gd);
    }

    /**
     * Subclasses may override. To add custom layout hints. e.g.
     * 
     * <pre>
     * layouter.addColumnData(new ColumnPixelData(15, false)); // message image
     * layouter.addColumnData(new ColumnWeightData(95, true));
     * </pre>
     * 
     * @param layouter the composite that lays out the contained table
     */
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        // message image
        layouter.addColumnData(new ColumnPixelData(15, false));
        layouter.addColumnData(new ColumnWeightData(95, true));
    }

    private void createButtonComposite(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        buttonComposite.setLayout(gl);

        addButton = createButton(buttonComposite, Messages.EditTableControlUIBuilder_AddButtonLabel);
        removeButton = createButton(buttonComposite, Messages.EditTableControlUIBuilder_RemoveButtonLabel);
        addSpacer(buttonComposite);
        upButton = createButton(buttonComposite, Messages.EditTableControlUIBuilder_UpButtonLabel);
        downButton = createButton(buttonComposite, Messages.EditTableControlUIBuilder_DownButtonLabel);
    }

    private Button createButton(Composite buttonComposite, String buttonLabel) {
        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText(buttonLabel);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

    private void addSpacer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5;
        label.setLayoutData(gd);
    }

    public void setTableDescription(String tableDescription) {
        tableLabel.setText(tableDescription);
    }

    public Table getTable() {
        return table;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Button getDownButton() {
        return downButton;
    }

    public Button getUpButton() {
        return upButton;
    }

}
