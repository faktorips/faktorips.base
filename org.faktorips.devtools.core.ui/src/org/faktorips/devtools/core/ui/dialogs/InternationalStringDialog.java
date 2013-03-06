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

import java.util.ArrayList;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.table.InternationalStringTraversalStrategy;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.LocalizedStringCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * A dialog to edit different locales of an attribute.
 * 
 * @author Bouillon
 */
public class InternationalStringDialog extends IpsPartEditDialog2 {
    private TableViewer tableViewer;

    private final IIpsObjectPart ipsObjectPart;

    private final IInternationalString internationalString;

    public InternationalStringDialog(Shell parentShell, IIpsObjectPart ipsObjectPart,
            IInternationalString internationalString) {
        super(ipsObjectPart, parentShell, Messages.InternationalValueDialog_titleText);
        this.ipsObjectPart = ipsObjectPart;
        this.internationalString = internationalString;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    private IInternationalString getInternationalString() {
        return internationalString;
    }

    private IIpsProject getIpsProject() {
        return ipsObjectPart.getIpsProject();
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

        // Table table = createTable(mainComposite);
        initTableViewer(mainComposite);

        ((GridData)parent.getLayoutData()).heightHint = 300;
        return parent;
    }

    /**
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column
     * headers and widths, column properties, cell editors, sorter. Inits popupmenu and
     * hoverservice.
     */
    private void initTableViewer(Composite parent) {
        TableColumnLayout layout = new TableColumnLayout();
        parent.setLayout(layout);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        final TableViewerColumn languageColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
        languageColumn.getColumn().setText(Messages.InternationalValueDialog_languageColumnTitle);
        layout.setColumnData(languageColumn.getColumn(), new ColumnPixelData(100, true, true));

        final TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
        valueColumn.getColumn().setText(Messages.InternationalValueDialog_valueColumnTitle);
        layout.setColumnData(valueColumn.getColumn(), new ColumnPixelData(350, true, true));

        tableViewer.setUseHashlookup(true);
        tableViewer.setContentProvider(new InternationalValueContentProvider(getIpsProject()));
        tableViewer.setLabelProvider(new InternationalValueLabelProvider());

        LocalizedStringEditingSupport localizedStringEditingSupport = new LocalizedStringEditingSupport(tableViewer,
                new IElementModifier<ILocalizedString, ILocalizedString>() {

                    @Override
                    public ILocalizedString getValue(ILocalizedString element) {
                        return element;
                    }

                    @Override
                    public void setValue(ILocalizedString element, ILocalizedString value) {
                        getInternationalString().add(value);
                    }
                });

        localizedStringEditingSupport.setTraversalStrategy(new InternationalStringTraversalStrategy(
                localizedStringEditingSupport, getIpsProject(), 1, getInternationalString()));
        valueColumn.setEditingSupport(localizedStringEditingSupport);
        tableViewer.setInput(getInternationalString());
    }

    /**
     * Content provider for the internationalization dialog. Lists all values of an international
     * string.
     */
    static class InternationalValueContentProvider implements IStructuredContentProvider {
        private final IIpsProject ipsProject;

        InternationalValueContentProvider(IIpsProject ipsProject) {
            this.ipsProject = ipsProject;
        }

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
            if (input instanceof IInternationalString) {
                ArrayList<ILocalizedString> localizedStringInput = new ArrayList<ILocalizedString>();
                IInternationalString inputString = (IInternationalString)input;
                for (ISupportedLanguage language : ipsProject.getProperties().getSupportedLanguages()) {
                    ILocalizedString localizedString = inputString.get(language.getLocale());
                    localizedStringInput
                            .add(localizedString == null ? new LocalizedString(language.getLocale(), "") : localizedString); //$NON-NLS-1$
                }
                return localizedStringInput.toArray();
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

    private static class LocalizedStringEditingSupport extends
            FormattedCellEditingSupport<ILocalizedString, ILocalizedString> {

        public LocalizedStringEditingSupport(TableViewer tableViewer,
                IElementModifier<ILocalizedString, ILocalizedString> elementModifier) {
            super(tableViewer, elementModifier);
        }

        @Override
        public TableViewer getViewer() {
            return (TableViewer)super.getViewer();
        }

        @Override
        public String getFormattedValue(ILocalizedString element) {
            return element.getValue();
        }

        @Override
        protected IpsCellEditor getCellEditorInternal(ILocalizedString element) {
            Text control = new UIToolkit(null).createText(getViewer().getTable());
            LocalizedStringCellEditor cellEditor = new LocalizedStringCellEditor(control);
            TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, getViewer(), 1);
            strat.setRowCreating(true);
            cellEditor.setTraversalStrategy(strat);
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

    }

}
