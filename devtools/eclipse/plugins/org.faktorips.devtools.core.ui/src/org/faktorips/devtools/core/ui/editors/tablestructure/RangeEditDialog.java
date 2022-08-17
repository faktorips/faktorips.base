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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;

/**
 * A dialog to edit a range.
 */
public class RangeEditDialog extends IpsPartEditDialog2 {

    private IColumnRange range;
    private TableViewer columnViewer;

    private TextField fromField;
    private TextField toField;
    private EnumField<ColumnRangeType> rangeTypeField;
    private TextField parameterNameField;
    private Button topRight;
    private Button topLeft;
    private Button bottomRight;
    private Button bottomLeft;
    private Label fromLabel;
    private Label toLabel;

    public RangeEditDialog(IColumnRange range, Shell parentShell) {
        super(range, parentShell, Messages.RangeEditDialog_title, true);
        this.range = range;
    }

    @Override
    public void create() {
        super.create();
        setMessage(null);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.RangeEditDialog_generalTitle);
        page.setControl(createGeneralPage(folder));

        bindContent();

        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite rangeTypeArea = getToolkit().createGridComposite(c, 2, false, true);
        getToolkit().createFormLabel(rangeTypeArea, Messages.RangeEditDialog_labelType);
        rangeTypeField = new EnumField<>(getToolkit().createCombo(rangeTypeArea),
                ColumnRangeType.class);
        rangeTypeField.addChangeListener(e -> adjustEnableStateToRangeType((ColumnRangeType)e.field.getValue()));
        getToolkit().createFormLabel(rangeTypeArea, Messages.RangeEditDialog_RangeEditDialog_parameterName);
        Text parameterNameText = getToolkit().createText(rangeTypeArea);
        parameterNameField = new TextField(parameterNameText);

        Composite container = getToolkit().createGridComposite(c, 3, false, false);

        Composite left = getToolkit().createGridComposite(container, 1, false, true);
        Composite leftGroup = getToolkit()
                .createGroup(left, SWT.NONE, Messages.RangeEditDialog_groupAvailableColsTitle);
        createColumnSelectionComposite(leftGroup);

        Composite middle = getToolkit().createGridComposite(container, 1, true, true);
        middle.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_CENTER));
        createButtons(middle);

        Composite right = getToolkit().createGridComposite(container, 1, false, true);
        Composite rightGroup = getToolkit().createGroup(right, SWT.NONE, Messages.RangeEditDialog_groupTitle);

        Composite editArea = getToolkit().createLabelEditColumnComposite(rightGroup);
        GridData data = (GridData)editArea.getLayoutData();
        data.widthHint = 180;
        data.heightHint = 200;
        fromLabel = getToolkit().createFormLabel(editArea, Messages.RangeEditDialog_labelFrom);
        Text fromText = getToolkit().createText(editArea);
        fromField = new TextField(fromText);

        // add space so that the text control for the to column is aligned
        // with it's buttons
        getToolkit().createLabel(editArea, ""); //$NON-NLS-1$
        getToolkit().createLabel(editArea, ""); //$NON-NLS-1$
        getToolkit().createLabel(editArea, ""); //$NON-NLS-1$
        getToolkit().createLabel(editArea, ""); //$NON-NLS-1$
        getToolkit().createVerticalSpacer(editArea, 10);
        getToolkit().createVerticalSpacer(editArea, 10);

        toLabel = getToolkit().createFormLabel(editArea, Messages.RangeEditDialog_labelTo);
        Text toText = getToolkit().createText(editArea);
        toField = new TextField(toText);

        return c;
    }

    private void createColumnSelectionComposite(Composite parent) {
        Composite c = getToolkit().createGridComposite(parent, 1, false, false);
        Table table = new Table(c, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 180;
        data.heightHint = 200;
        table.setLayoutData(data);
        columnViewer = new TableViewer(table);
        columnViewer.setLabelProvider(new LocalizedLabelProvider());
        columnViewer.setContentProvider(new ComparableDatatypeColumnsProvider());
        columnViewer.setInput(this);
    }

    private void createButtons(Composite middle) {
        createTopRightArrow(middle);
        createTopLeftArrow(middle);
        getToolkit().createVerticalSpacer(middle, 10);
        createBottomRightArrow(middle);
        createBottomLeftArrow(middle);
    }

    private void createTopRightArrow(Composite middle) {
        topRight = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        topRight.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        topRight.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        topRight.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectColumn(fromField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createTopLeftArrow(Composite middle) {
        topLeft = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        topLeft.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        topLeft.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        topLeft.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearColumn(fromField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createBottomRightArrow(Composite middle) {
        bottomRight = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        bottomRight.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        bottomRight.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        bottomRight.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectColumn(toField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createBottomLeftArrow(Composite middle) {
        bottomLeft = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        bottomLeft.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        bottomLeft.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        bottomLeft.addSelectionListener(new SelectionListener() {

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
        bottomRight.setEnabled(enabled);
        bottomLeft.setEnabled(enabled);
        toLabel.setEnabled(enabled);
    }

    private void setEnabledForFromFieldControls(boolean enabled) {
        fromField.getControl().setEnabled(enabled);
        toField.getControl().setEnabled(enabled);
        topRight.setEnabled(enabled);
        topLeft.setEnabled(enabled);
        fromLabel.setEnabled(enabled);
    }

    private void bindContent() {
        getBindingContext().bindContent(rangeTypeField, getIpsPart(), IColumnRange.PROPERTY_RANGE_TYPE);
        getBindingContext().bindContent(fromField, getIpsPart(), IColumnRange.PROPERTY_FROM_COLUMN);
        getBindingContext().bindContent(toField, getIpsPart(), IColumnRange.PROPERTY_TO_COLUMN);
        getBindingContext().bindContent(parameterNameField, getIpsPart(), IColumnRange.PROPERTY_PARAMETER_NAME);
        getBindingContext().updateUI();
    }

    private void selectColumn(TextField field) {
        ISelection selection = columnViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        IColumn column = (IColumn)((IStructuredSelection)selection).getFirstElement();
        field.setValue(column.getName());
    }

    private void clearColumn(TextField field) {
        field.setValue(""); //$NON-NLS-1$
    }

    private final class ComparableDatatypeColumnsProvider implements IStructuredContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            IColumn[] columns = range.getTableStructure().getColumns();
            ArrayList<IColumn> result = new ArrayList<>();
            for (IColumn column : columns) {
                if (column.findValueDatatype(getIpsPart().getIpsProject()).supportsCompare()) {
                    result.add(column);
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
    }
}
