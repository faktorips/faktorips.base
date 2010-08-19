/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;

/**
 * A dialog to edit a range.
 */
public class RangeEditDialog extends IpsPartEditDialog {

    private IColumnRange range;
    private TableViewer columnViewer;

    private TextField fromField;
    private TextField toField;
    private EnumValueField rangeTypeField;
    private TextField parameterNameField;
    private Button toLeft;
    private Button toRight;
    private Button toLeft2;
    private Button toRight2;
    private Label fromLabel;
    private Label toLabel;

    public RangeEditDialog(IColumnRange range, Shell parentShell) {
        super(range, parentShell, Messages.RangeEditDialog_title, true);
        this.range = range;
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.RangeEditDialog_generalTitle);
        page.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite rangeTypeArea = uiToolkit.createGridComposite(c, 2, false, true);
        uiToolkit.createFormLabel(rangeTypeArea, Messages.RangeEditDialog_labelType);
        rangeTypeField = new EnumValueField(uiToolkit.createCombo(rangeTypeArea, ColumnRangeType.getEnumType()),
                ColumnRangeType.getEnumType());
        rangeTypeField.addChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                adjustEnableStateToRangeType((ColumnRangeType)e.field.getValue());
            }
        });
        uiToolkit.createFormLabel(rangeTypeArea, Messages.RangeEditDialog_RangeEditDialog_parameterName);
        Text parameterNameText = uiToolkit.createText(rangeTypeArea);
        parameterNameField = new TextField(parameterNameText);

        Composite container = uiToolkit.createGridComposite(c, 3, false, false);

        Composite left = uiToolkit.createGridComposite(container, 1, false, true);
        Composite leftGroup = uiToolkit.createGroup(left, SWT.NONE, Messages.RangeEditDialog_groupTitle);

        Composite editArea = uiToolkit.createLabelEditColumnComposite(leftGroup);
        GridData data = (GridData)editArea.getLayoutData();
        data.widthHint = 180;
        data.heightHint = 200;
        fromLabel = uiToolkit.createFormLabel(editArea, Messages.RangeEditDialog_labelFrom);
        Text fromText = uiToolkit.createText(editArea);
        fromField = new TextField(fromText);

        // add space so that the text control for the to column is aligned
        // with it's buttons
        uiToolkit.createLabel(editArea, ""); //$NON-NLS-1$
        uiToolkit.createLabel(editArea, ""); //$NON-NLS-1$
        uiToolkit.createLabel(editArea, ""); //$NON-NLS-1$
        uiToolkit.createLabel(editArea, ""); //$NON-NLS-1$
        uiToolkit.createVerticalSpacer(editArea, 10);
        uiToolkit.createVerticalSpacer(editArea, 10);

        toLabel = uiToolkit.createFormLabel(editArea, Messages.RangeEditDialog_labelTo);
        Text toText = uiToolkit.createText(editArea);
        toField = new TextField(toText);

        Composite middle = uiToolkit.createGridComposite(container, 1, true, true);
        middle.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_CENTER));
        createButtons(middle);

        Composite right = uiToolkit.createGridComposite(container, 1, false, true);
        Composite rightGroup = uiToolkit.createGroup(right, SWT.NONE, Messages.RangeEditDialog_groupAvailableColsTitle);
        createColumnSelectionComposite(rightGroup);

        return c;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        // set the inital state depending on the range type
        adjustEnableStateToRangeType(range.getColumnRangeType());
        return control;
    }

    private void adjustEnableStateToRangeType(ColumnRangeType type) {
        if (type.equals(ColumnRangeType.TWO_COLUMN_RANGE)) {
            setEnabledForFromFieldControls(true);
            setEnabledForToFieldControls(true);
            return;
        }

        if (type.equals(ColumnRangeType.ONE_COLUMN_RANGE_FROM)) {
            setEnabledForFromFieldControls(true);
            setEnabledForToFieldControls(false);
            return;
        }

        if (type.equals(ColumnRangeType.ONE_COLUMN_RANGE_TO)) {
            setEnabledForFromFieldControls(false);
            setEnabledForToFieldControls(true);
        }
    }

    private void setEnabledForToFieldControls(boolean enabled) {
        toField.getControl().setEnabled(enabled);
        toField.getControl().setEnabled(enabled);
        toLeft2.setEnabled(enabled);
        toRight2.setEnabled(enabled);
        toLabel.setEnabled(enabled);
    }

    private void setEnabledForFromFieldControls(boolean enabled) {
        fromField.getControl().setEnabled(enabled);
        toField.getControl().setEnabled(enabled);
        toLeft.setEnabled(enabled);
        toRight.setEnabled(enabled);
        fromLabel.setEnabled(enabled);
    }

    private void createButtons(Composite middle) {
        toLeft = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        toLeft.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        toLeft.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        toLeft.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectColumn(fromField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

        toRight = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        toRight.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        toRight.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        toRight.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearColumn(fromField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

        uiToolkit.createVerticalSpacer(middle, 10);

        toLeft2 = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        toLeft2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        toLeft2.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        toLeft2.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectColumn(toField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

        toRight2 = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        toRight2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        toRight2.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        toRight2.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                clearColumn(toField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

    }

    private void createColumnSelectionComposite(Composite parent) {
        Composite c = uiToolkit.createGridComposite(parent, 1, false, false);
        Table table = new Table(c, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 180;
        data.heightHint = 200;
        table.setLayoutData(data);
        columnViewer = new TableViewer(table);
        columnViewer.setLabelProvider(new DefaultLabelProvider());
        columnViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                IColumn[] columns = range.getTableStructure().getColumns();
                ArrayList<IColumn> result = new ArrayList<IColumn>();
                for (int i = 0; i < columns.length; i++) {
                    if (!columns[i].getDatatype().equals(Datatype.BOOLEAN.getName())) {
                        result.add(columns[i]);
                    }
                }
                return result.toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }
        });
        columnViewer.setInput(this);
    }

    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(rangeTypeField, IColumnRange.PROPERTY_RANGE_TYPE);
        uiController.add(fromField, IColumnRange.PROPERTY_FROM_COLUMN);
        uiController.add(toField, IColumnRange.PROPERTY_TO_COLUMN);
        uiController.add(parameterNameField, IColumnRange.PROPERTY_PARAMETER_NAME);
    }

    private void selectColumn(TextField field) {
        ISelection selection = columnViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        IColumn column = (IColumn)((IStructuredSelection)selection).getFirstElement();
        field.setValue(column.getName());
        uiController.updateModel();
    }

    private void clearColumn(TextField field) {
        field.setValue(""); //$NON-NLS-1$
        uiController.updateModel();
    }
}
