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

package org.faktorips.devtools.core.ui.editors;

import java.util.Locale;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeled;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;

/**
 * A composite that allows to edit the {@link ILabel}s attached to an {@link ILabeled} object. It
 * consists of a table with two columns listing all the labels with their associated language.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public class LabelEditComposite extends Composite {

    private final ILabeled labeledObject;

    private final TableViewer tableViewer;

    public LabelEditComposite(Composite parent, ILabeled labeledObject) {
        super(parent, SWT.NONE);

        this.labeledObject = labeledObject;

        createLayout();
        tableViewer = createTableViewer();
        configureTable();
    }

    private void createLayout() {
        Layout layout = new GridLayout();
        setLayout(layout);
    }

    private TableViewer createTableViewer() {
        TableViewer tableViewer = new TableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        tableViewer.setColumnProperties(new String[] { Messages.LabelEditComposite_tableColumnHeaderLanguage,
                Messages.LabelEditComposite_tableColumnHeaderLabel });

        TableViewerColumn languageColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        languageColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderLanguage);
        languageColumn.getColumn().setWidth(100);
        TableViewerColumn labelColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        labelColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderLabel);
        labelColumn.getColumn().setWidth(350);

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());

        tableViewer.setUseHashlookup(true);
        tableViewer.setInput(labeledObject);

        tableViewer.setCellModifier(new TableCellModifier());

        CellEditor[] cellEditors = new CellEditor[2];
        ValueDatatype datatype = ValueDatatype.STRING;
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        UIToolkit uiToolkit = new UIToolkit(null);
        cellEditors[0] = controlFactory.createTableCellEditor(uiToolkit, datatype, null, tableViewer, 0, null);
        cellEditors[1] = controlFactory.createTableCellEditor(uiToolkit, datatype, null, tableViewer, 1, null);
        tableViewer.setCellEditors(cellEditors);

        return tableViewer;
    }

    private void configureTable() {
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Make the table use all available space.
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = getParent().getClientArea().width;
        gridData.heightHint = getParent().getClientArea().height;
        table.setLayoutData(gridData);
    }

    private class TableContentProvider implements IStructuredContentProvider {

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do.
        }

        @Override
        public void dispose() {
            // Nothing to dispose.
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return labeledObject.getLabels().toArray();
        }

    }

    private class TableLabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // TODO AW: Return error image when there are errors
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ILabel label = (ILabel)element;
            switch (columnIndex) {
                case 0:
                    Locale locale = label.getLocale();
                    return (locale == null) ? "" : locale.getLanguage(); //$NON-NLS-1$
                case 1:
                    String value = label.getValue();
                    return (value == null) ? "" : value; //$NON-NLS-1$
                case 2:
                    String pluralValue = label.getPluralValue();
                    return (pluralValue == null) ? "" : pluralValue; //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // Nothing to do.
        }

        @Override
        public void dispose() {
            // Nothing to dispose.
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // Nothing to do.
        }

    }

    private class TableCellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        @Override
        public Object getValue(Object element, String property) {
            if (element instanceof ILabel) {
                ILabel label = (ILabel)element;
                if (property.equals(Messages.LabelEditComposite_tableColumnHeaderLanguage)) {
                    return label.getLocale().getLanguage();
                } else if (property.equals(Messages.LabelEditComposite_tableColumnHeaderLabel)) {
                    return label.getValue();
                }
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            if (!(element instanceof ILabel)) {
                return;
            }
            if (!(value instanceof String)) {
                return;
            }

            ILabel label = (ILabel)element;
            String valueString = (String)value;

            if (property.equals(Messages.LabelEditComposite_tableColumnHeaderLanguage)) {
                label.setLocale(new Locale(valueString));
            } else if (property.equals(Messages.LabelEditComposite_tableColumnHeaderLabel)) {
                label.setValue(valueString);
            }
        }
    }

}
