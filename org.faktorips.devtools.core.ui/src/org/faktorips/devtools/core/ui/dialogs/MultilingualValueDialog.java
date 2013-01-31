/*******************************************************************************
 * Copyright (c) Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.Locale;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.table.LocalizedStringCellEditor;
import org.faktorips.devtools.core.ui.table.TableUtil;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * A dialog to edit different locales of an attribute.
 * 
 * @author Bouillon
 */
public class MultilingualValueDialog extends IpsPartEditDialog2 {
    /**
     * Two columns: First column is current locale, second column is the value of the attribute in
     * that locale.
     */
    private static final int NUMBER_OF_COLUMNS = 2;

    /**
     * The table viewer used to present the internationalization data.
     */
    private TableViewer tableViewer;

    /**
     * The corresponding project to the modified value.
     */
    private final IIpsProject ipsProject;

    /**
     * Value holder of the attribute.
     */
    private final IValueHolder<IValue<?>> valueHolder;
    private final IAttributeValue attributeValue;

    public MultilingualValueDialog(Shell parentShell, IAttributeValue attributeValue, IIpsProject ipsProject,
            IValueHolder<IValue<?>> valueHolder) {
        super(attributeValue, parentShell, Messages.InternationalValueDialog_titleText);
        this.valueHolder = valueHolder;
        this.ipsProject = ipsProject;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        Assert.isNotNull(attributeValue);
        this.attributeValue = attributeValue;
    }

    @Override
    protected String buildTitle() {
        return NLS.bind(Messages.InternationalValueDialog_descriptionText, super.buildTitle());
    }

    @Override
    protected void setDataChangeableThis(boolean changeable) {
        /*
         * Do not set data changeable (or unchangeable respectively). This dialog can never be
         * opened in browse mode, the Multi-Value button next to the attribute value's field is
         * disabled in that case.
         */
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        Composite mainComposite = getToolkit().createGridComposite(parent, 1, false, true);

        Table table = createTable(mainComposite);
        initTableViewer(table);

        ((GridData)parent.getLayoutData()).heightHint = 300;
        return parent;
    }

    /**
     * Creates a Table with the given composite as a parent and returns it. Initializess the look,
     * layout of the table and adds a KeyListener that enables the editing of the second cell in the
     * currently selected row by pressing "F2".
     * 
     * @return The newly created and initialized Table.
     */
    private Table createTable(Composite parent) {
        // Table: scroll both vertically and horizontally
        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // occupy all available space
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        table.setLayoutData(tableGridData);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.F2) {
                    IRow selectedRow = (IRow)((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
                    if (selectedRow != null) {
                        tableViewer.editElement(selectedRow, 1);
                    }
                }
            }
        });

        return table;
    }

    private IValueHolder<IValue<?>> getValueHolder() {
        if (valueHolder != null) {
            return valueHolder;
        }
        return (IValueHolder<IValue<?>>)attributeValue.getValueHolder();
    }

    void updateTable(final LocalizedString locString) {
        ((IInternationalString)getValueHolder().getValue().getContent()).add(locString);
        tableViewer.refresh();
    }

    /**
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column
     * headers and widths, column properties, cell editors, sorter. Inits popupmenu and
     * hoverservice.
     */
    private void initTableViewer(Table table) {
        table.removeAll();
        TableUtil.increaseHeightOfTableRows(table, NUMBER_OF_COLUMNS, 5);

        String[] columnProperties = new String[NUMBER_OF_COLUMNS];

        final TableColumn languageColumn = new TableColumn(table, SWT.LEFT, 0);
        languageColumn.setText(Messages.InternationalValueDialog_languageColumnTitle);
        columnProperties[0] = Messages.InternationalValueDialog_languageColumnTitle;
        languageColumn.setWidth(200);

        final TableColumn valueColumn = new TableColumn(table, SWT.LEFT, 1);
        valueColumn.setText(Messages.InternationalValueDialog_valueColumnTitle);
        columnProperties[1] = Messages.InternationalValueDialog_valueColumnTitle;
        valueColumn.setWidth(200);

        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);
        tableViewer.setContentProvider(new InternationalValueContentProvider());
        tableViewer.setLabelProvider(new InternationalValueLabelProvider());
        tableViewer.setColumnProperties(columnProperties);
        Text textControl = getToolkit().createText(tableViewer.getTable(), SWT.SINGLE | SWT.LEFT);

        LocalizedStringCellEditor cellEditor = new LocalizedStringCellEditor(textControl);
        cellEditor.setTraversalStrategy(new TableViewerTraversalStrategy(cellEditor, tableViewer, 1) {
            @Override
            protected void editNextColumn() {
                int nextRow = getNextRow();
                if (nextRow >= ipsProject.getProperties().getSupportedLanguages().size()) {
                    tableViewer.getTable().forceFocus();
                } else {
                    if (isAtNewColumn()) {
                        editCell(nextRow, 1);
                    }
                }
            }
        });
        tableViewer.setCellEditors(new CellEditor[] { null, cellEditor });
        tableViewer.setCellModifier(new MultilingualValueCellModifier(this));
        if (getValueHolder().getValue().getContent() instanceof IInternationalString) {
            IInternationalString internationalString = (IInternationalString)getValueHolder().getValue().getContent();
            for (ISupportedLanguage l : ipsProject.getProperties().getSupportedLanguages()) {
                Locale locale = l.getLocale();
                if (internationalString.get(locale) == null) {
                    internationalString.add(new LocalizedString(locale, "")); //$NON-NLS-1$
                }
            }
        }
        tableViewer.setInput(getValueHolder().getValue().getContent());
    }

    /**
     * Content provider for the internationalization dialog. Lists all values of an international
     * string.
     */
    static class InternationalValueContentProvider implements IStructuredContentProvider {
        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
            // Nothing to do
        }

        @Override
        public Object[] getElements(Object input) {
            if (input instanceof InternationalString) {
                return ((InternationalString)input).values().toArray();
            }
            return new LocalizedString[0];
        }
    }

    /**
     * Label provider for the internationalization dialog.
     */
    static class InternationalValueLabelProvider implements ITableLabelProvider {
        @Override
        public void addListener(ILabelProviderListener arg0) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public boolean isLabelProperty(Object arg0, String arg1) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener arg0) {
            // Nothing to do
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof LocalizedString) {
                LocalizedString locString = (LocalizedString)element;
                switch (columnIndex) {
                    case 0:
                        return locString.getLocale() == null ? "" : locString.getLocale().getDisplayLanguage(); //$NON-NLS-1$
                    case 1:
                        return locString.getValue();
                    default:
                        return ""; //$NON-NLS-1$
                }
            }
            return null;
        }
    }
}
