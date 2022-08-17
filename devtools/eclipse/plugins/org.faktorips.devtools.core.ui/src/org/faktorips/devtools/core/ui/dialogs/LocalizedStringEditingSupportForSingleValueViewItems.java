/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.core.ui.table.InternationalStringCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.values.LocalizedString;

/**
 * {@link FormattedCellEditingSupport} that creates its cell editors configured for multiple
 * languages. This class is used in the event that a multi-value attribute is also available in
 * multiple languages. In this case, this table provides the means to generate the cell editor to
 * enter the available languages for one value. So, by binding this editing support to the table
 * used in the {@link MultiValueDialog}, internationalization can be achieved for every value.
 */
public class LocalizedStringEditingSupportForSingleValueViewItems extends
        FormattedCellEditingSupport<SingleValueViewItem, LocalizedString> {
    /**
     * The {@link UIToolkit} for the dialog using this support.
     */
    private final UIToolkit toolkit;

    /**
     * {@link TableViewer} for the multi value attribute.
     */
    private final TableViewer multiValueTableViewer;

    public LocalizedStringEditingSupportForSingleValueViewItems(UIToolkit toolkit, TableViewer tableViewer,
            IElementModifier<SingleValueViewItem, LocalizedString> elementModifier) {
        super(tableViewer, elementModifier);
        this.toolkit = toolkit;
        multiValueTableViewer = tableViewer;
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(SingleValueViewItem element) {
        final ISingleValueHolder singleValueHolder = element.getSingleValueHolder();

        Table table = multiValueTableViewer.getTable();
        InternationalStringDialogHandler handler = new MultilingualValueHandler(table.getShell(),
                singleValueHolder.getParent(), singleValueHolder);
        InternationalStringControl control = new InternationalStringControl(table, toolkit, handler);
        control.setHeightHint(table.getItemHeight());
        InternationalStringCellEditor cellEditor = new InternationalStringCellEditor(control);

        bindTableRefresh(singleValueHolder);

        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, multiValueTableViewer, 1);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);

        return cellEditor;
    }

    /**
     * The Refresh of the table is needed because it may change without using the cell editor when
     * calling the multilingual dialog and changing the current language.
     */
    private void bindTableRefresh(final ISingleValueHolder singleValueHolder) {
        final IIpsModel ipsModel = IIpsModel.get();
        final ContentsChangeListener contentChangeListener = event -> {
            if (event.isAffected(singleValueHolder.getParent())) {
                multiValueTableViewer.refresh();
            }
        };
        ipsModel.addChangeListener(contentChangeListener);
        multiValueTableViewer.getTable().addDisposeListener($ -> ipsModel.removeChangeListener(contentChangeListener));
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    public String getFormattedValue(SingleValueViewItem element) {
        LocalizedString string = getValue(element);
        return string == null ? null : string.getValue();
    }

    private static class MultilingualValueHandler extends InternationalStringDialogHandler {

        private final ISingleValueHolder singleValueHolder;

        private MultilingualValueHandler(Shell shell, IIpsObjectPart part, ISingleValueHolder singleValueHolder) {
            super(shell, part);
            this.singleValueHolder = singleValueHolder;
        }

        @Override
        protected IInternationalString getInternationalString() {
            IValue<?> value = singleValueHolder.getValue();
            if (value.getContent() instanceof IInternationalString) {
                return (IInternationalString)value.getContent();
            } else {
                throw new IllegalArgumentException(
                        "The value provided to the InternationalStringDialog is not supported: The type was " //$NON-NLS-1$
                                + (value.getContent() == null ? "<null>" : value.getContent().getClass())); //$NON-NLS-1$
            }
        }
    }
}
