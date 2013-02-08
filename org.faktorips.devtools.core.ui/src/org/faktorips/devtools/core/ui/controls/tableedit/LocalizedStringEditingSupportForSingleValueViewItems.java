/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.ISingleValueHolderProvider;
import org.faktorips.devtools.core.ui.controls.MultilingualValueAttributeControl;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.MultilingualTextCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * {@link FormattedCellEditingSupport} that creates its cell editors configured for multiple
 * languages. This class is used in the event that a multi-value attribute is also available in
 * multiple languages. In this case, this table provides the means to generate the cell editor to
 * enter the available languages for one value. So, by binding this editing support to the table
 * used in the {@link MultiValueDialog}, internationalization can be achieved for every value.
 */
public class LocalizedStringEditingSupportForSingleValueViewItems extends
        FormattedCellEditingSupport<SingleValueViewItem, ILocalizedString> {
    /**
     * The {@link UIToolkit} for the dialog using this support.
     */
    private final UIToolkit toolkit;

    /**
     * {@link TableViewer} for the multi value attribute.
     */
    private final TableViewer multiValueTableViewer;

    /**
     * Local {@link BindingContext binding context}. It is used to bind the cell editor to the
     * model.
     */
    private final BindingContext bindingContext;

    public LocalizedStringEditingSupportForSingleValueViewItems(UIToolkit toolkit, TableViewer tableViewer,
            IElementModifier<SingleValueViewItem, ILocalizedString> elementModifier) {
        super(tableViewer, elementModifier);
        this.toolkit = toolkit;
        this.multiValueTableViewer = tableViewer;
        this.bindingContext = new BindingContext();
        tableViewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                dispose();
            }
        });
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(SingleValueViewItem element) {
        final SingleValueHolder singleValueHolder = element.getSingleValueHolder();

        MultilingualValueAttributeControl control = new MultilingualValueAttributeControl(
                multiValueTableViewer.getTable(), toolkit, new ISingleValueHolderProvider() {
                    @Override
                    public SingleValueHolder getSingleValueHolder() {
                        return singleValueHolder;
                    }
                });
        MultilingualTextCellEditor cellEditor = new MultilingualTextCellEditor(IpsPlugin.getMultiLanguageSupport()
                .getLocalizationLocale(), control);

        bindingContext.add(new RefreshTableViewerControlPropertyBinding(multiValueTableViewer, singleValueHolder,
                IValueHolder.PROPERTY_VALUE, Object.class));

        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, multiValueTableViewer, 1);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);

        return cellEditor;
    }

    public void dispose() {
        bindingContext.dispose();
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    public String getFormattedValue(SingleValueViewItem element) {
        ILocalizedString string = getValue(element);
        return string == null ? null : string.getValue();
    }
}
