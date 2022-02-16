/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.tablecontents.IRow;

/**
 * Action for deleting a single row in a tableviewer.
 * 
 * @author Stefan Widmaier
 */
public class DeleteRowAction extends IpsAction {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;

    private ContentPage contentPage;

    /**
     * Creates an action that, when run, deletes the (first) selected row in the given
     * <code>TableViewer</code>.
     */
    public DeleteRowAction(TableViewer tableViewer, ContentPage page) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        contentPage = page;
        setControlWithDataChangeableSupport(page);
        setText(Messages.DeleteRowAction_Label);
        setToolTipText(Messages.DeleteRowAction_Tooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("DeleteRow.gif")); //$NON-NLS-1$
    }

    /**
     * Deletes the selected rows in the tableviewer and refreshes thereafter. {@inheritDoc}
     */
    @Override
    public void run(final IStructuredSelection selection) {
        IpsModel model = (IpsModel)IIpsModel.get();
        model.executeModificationsWithSingleEvent(new DeleteSelectedRowsModification(
                getIpsSrcFileForSelection(selection), selection, tableViewer, contentPage));
        tableViewer.refresh(false);
    }

    private static class DeleteSelectedRowsModification extends SingleEventModification<Object> {

        private IStructuredSelection selection;
        private TableViewer tableViewer;
        private ContentPage contentPage;

        protected DeleteSelectedRowsModification(IIpsSrcFile srcFile, IStructuredSelection selection,
                TableViewer tableViewer, ContentPage page) {
            super(srcFile);
            this.selection = selection;
            this.tableViewer = tableViewer;
            this.contentPage = page;
        }

        @Override
        protected boolean execute() {
            if (selection.isEmpty()) {
                return false;
            }
            List<IRow> rows = TypedSelection.createAnyCount(IRow.class, selection).getElements();
            for (IRow row : rows) {
                row.delete();
            }
            selectPreviousRow(rows.get(0).getRowNumber());
            return true;
        }

        /**
         * Selects the previous row of the first element or if the given row is the first row the
         * new first row.
         */
        private void selectPreviousRow(int rowNumber) {
            int rowIndexToSelect = rowNumber - 1;
            if (rowIndexToSelect < 0) {
                rowIndexToSelect = 0;
            }
            IRow row = contentPage.getRow(rowIndexToSelect);
            if (row != null) {
                tableViewer.setSelection(new StructuredSelection(row));
            }
        }
    }
}
