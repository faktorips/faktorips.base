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

import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.util.message.MessageList;

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

    public LabelEditComposite(Composite parent, ILabeledElement labeledElement) {
        super(parent, SWT.NONE);

        this.labeledElement = labeledElement;

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
        TableViewer tableViewer = new TableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        tableViewer.setColumnProperties(new String[] { ILabel.PROPERTY_LOCALE, ILabel.PROPERTY_VALUE,
                ILabel.PROPERTY_PLURAL_VALUE });

        createTableColumns(tableViewer);

        tableViewer.setContentProvider(new TableContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());

        tableViewer.setUseHashlookup(true);
        tableViewer.setInput(labeledElement);

        tableViewer.setCellModifier(new TableCellModifier());

        createTableCellEditors(tableViewer);

        createTableHoverService(tableViewer);

        return tableViewer;
    }

    private void createTableHoverService(TableViewer tableViewer) {
        new TableMessageHoverService(tableViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
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
        UIToolkit uiToolkit = new UIToolkit(null);

        int numberCellEditors = labeledElement.isPluralLabelSupported() ? 3 : 2;
        final IpsCellEditor[] cellEditors = new IpsCellEditor[numberCellEditors];
        for (int i = 0; i < cellEditors.length; i++) {
            cellEditors[i] = controlFactory.createTableCellEditor(uiToolkit, datatype, null, tableViewer, i, null);
            TableViewerTraversalStrategy traversalStrategy = (TableViewerTraversalStrategy)cellEditors[i]
                    .getTraversalStrategy();
            traversalStrategy.addSkippedColumnIndex(0);
        }

        tableViewer.setCellEditors(cellEditors);
    }

    private void createTableColumns(TableViewer tableViewer) {
        TableViewerColumn languageColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        languageColumn.getColumn().setText(Messages.LabelEditComposite_tableColumnHeaderLanguage);
        languageColumn.getColumn().setWidth(90);
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
            return labeledElement.getLabels().toArray();
        }

    }

    private class TableLabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // Error markers appear only in the locale column.
            if (columnIndex == 0) {
                ILabel label = (ILabel)element;
                try {
                    MessageList messageList = label.validate(label.getIpsProject());
                    if (!(messageList.isEmpty())) {
                        return IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC);
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
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
                    return (locale == null) ? "" : locale.getLanguage(); //$NON-NLS-1$
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
        public void modify(Object element, String property, Object value) {
            if (element instanceof Item) {
                element = ((Item)element).getData();
            }
            if (!(element instanceof ILabel)) {
                return;
            }
            if (!(value instanceof String)) {
                return;
            }

            ILabel label = (ILabel)element;
            String valueString = (String)value;

            if (property.equals(ILabel.PROPERTY_LOCALE)) {
                // The locale cannot be modified
            } else if (property.equals(ILabel.PROPERTY_VALUE)) {
                label.setValue(valueString);
            } else if (property.equals(ILabel.PROPERTY_PLURAL_VALUE)) {
                label.setPluralValue(valueString);
            }

            tableViewer.update(element, new String[] { property });
            tableViewer.refresh(true);
        }
    }

}
