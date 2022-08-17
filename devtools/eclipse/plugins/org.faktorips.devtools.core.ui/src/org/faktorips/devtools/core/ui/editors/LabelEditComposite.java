/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.Locale;

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
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.runtime.MessageList;

/**
 * A composite that allows to edit the {@link ILabel}s attached to an {@link ILabeledElement}. It
 * consists of a table with two columns listing all the labels with their associated language.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public final class LabelEditComposite extends Composite {

    private final ILabeledElement labeledElement;

    private final TableViewer tableViewer;

    private final UIToolkit toolkit;

    public LabelEditComposite(Composite parent, ILabeledElement labeledElement, UIToolkit toolkit) {
        super(parent, SWT.NONE);

        this.labeledElement = labeledElement;
        this.toolkit = toolkit;

        createLayout();
        tableViewer = createTableViewer();
        configureTable();
    }

    public void refresh() {
        tableViewer.refresh();
    }

    private void createLayout() {
        Layout layout = new GridLayout();
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private TableViewer createTableViewer() {
        TableViewer viewer = new TableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        viewer.setColumnProperties(new String[] { ILabel.PROPERTY_LOCALE, ILabel.PROPERTY_VALUE,
                ILabel.PROPERTY_PLURAL_VALUE });

        createTableColumns(viewer);

        viewer.setContentProvider(new TableContentProvider());
        viewer.setLabelProvider(new TableLabelProvider());

        viewer.setUseHashlookup(true);
        viewer.setInput(labeledElement);

        viewer.setCellModifier(new TableCellModifier());

        createTableCellEditors(viewer);

        createTableHoverService(viewer);

        return viewer;
    }

    private void createTableHoverService(TableViewer tableViewer) {
        new TableMessageHoverService(tableViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) {
                if (element != null) {
                    ILabel label = (ILabel)element;
                    return label.validate(label.getIpsProject());
                }
                return null;
            }
        };
    }

    private void createTableCellEditors(TableViewer tableViewer) {
        ValueDatatype datatype = ValueDatatype.STRING;
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);

        int numberCellEditors = labeledElement.isPluralLabelSupported() ? 3 : 2;
        final IpsCellEditor[] cellEditors = new IpsCellEditor[numberCellEditors];
        for (int i = 0; i < cellEditors.length; i++) {
            cellEditors[i] = controlFactory.createTableCellEditor(toolkit, datatype, null, tableViewer, i, null);
            TableViewerTraversalStrategy traversalStrategy = (TableViewerTraversalStrategy)cellEditors[i]
                    .getTraversalStrategy();
            traversalStrategy.setRowCreating(false);
            traversalStrategy.addSkippedColumnIndex(0);
        }

        tableViewer.setCellEditors(cellEditors);
    }

    private void createTableColumns(TableViewer tableViewer) {
        TableViewerColumn languageColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        languageColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderLanguage);
        languageColumn.getColumn().setWidth(120);
        TableViewerColumn labelColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        labelColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderLabel);
        labelColumn.getColumn().setWidth(180);
        if (labeledElement.isPluralLabelSupported()) {
            TableViewerColumn pluralLabelColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            pluralLabelColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderPluralLabel);
            pluralLabelColumn.getColumn().setWidth(180);
        }
    }

    private void configureTable() {
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Make the table use all available space.
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        table.setLayoutData(gridData);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tableViewer.getTable().setEnabled(enabled);
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
            return labeledElement.getLabels().toArray();
        }

    }

    private static class TableLabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // Error markers appear only in the locale column.
            if (columnIndex == 0) {
                ILabel label = (ILabel)element;
                MessageList messageList = label.validate(label.getIpsProject());
                if (!(messageList.isEmpty())) {
                    return IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC);
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ILabel label = (ILabel)element;
            switch (columnIndex) {
                case 0:
                    Locale locale = label.getLocale();
                    return (locale == null) ? "" : locale.getDisplayLanguage(); //$NON-NLS-1$
                case 1:
                    return label.getValue();
                case 2:
                    return label.getPluralValue();
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
                if (property.equals(ILabel.PROPERTY_LOCALE)) {
                    return label.getLocale().getLanguage();
                } else if (property.equals(ILabel.PROPERTY_VALUE)) {
                    return label.getValue();
                } else if (property.equals(ILabel.PROPERTY_PLURAL_VALUE)) {
                    return label.getPluralValue();
                }
            }
            return null;
        }

        @Override
        public void modify(final Object element, final String property, final Object value) {
            ILabel modifiedLabel = getModifiedLabel(element);
            if (canModify(modifiedLabel, value)) {
                setPropertyValue(property, (String)value, modifiedLabel);
                updateViewer(element, property);
            }
        }

        private boolean canModify(ILabel modifiedLabel, Object value) {
            return modifiedLabel != null && value instanceof String;
        }

        private ILabel getModifiedLabel(Object element) {
            if (element instanceof Item) {
                Object widgetData = ((Item)element).getData();
                if (widgetData instanceof ILabel) {
                    return (ILabel)widgetData;
                }
            }
            return null;
        }

        private void updateViewer(Object element, String property) {
            tableViewer.update(element, new String[] { property });
            tableViewer.refresh(true);
        }

        private void setPropertyValue(String property, String value, ILabel label) {
            if (property.equals(ILabel.PROPERTY_VALUE)) {
                label.setValue(value);
            } else if (property.equals(ILabel.PROPERTY_PLURAL_VALUE)) {
                label.setPluralValue(value);
            }
            // The property locale (PROPERTY_LOCALE) cannot be modified
        }
    }

}
