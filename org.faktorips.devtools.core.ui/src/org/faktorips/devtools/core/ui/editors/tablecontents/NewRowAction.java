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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;

/**
 * Action for add a new row in a tableviewer.
 * 
 * @author Joerg Ortmann
 */
public class NewRowAction extends IpsAction {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;
    private ContentPage contentPage;

    /**
     * Creates an action that, when run, addes a new row in the given <code>TableViewer</code>.
     */
    public NewRowAction(TableViewer tableViewer, ContentPage page) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        contentPage = page;
        setControlWithDataChangeableSupport(page);
        setText(Messages.NewRowAction_Label);
        setToolTipText(Messages.NewRowAction_Tooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("InsertRowAfter.gif")); //$NON-NLS-1$
    }

    /**
     * Creates a new row in the tableviewer and refreshes thereafter. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        ITableContents tableContents = (ITableContents)tableViewer.getInput();

        ITableRows tableContentsGeneration = tableContents.getTableRows();

        IRow newRow = null;
        int position = Integer.MAX_VALUE;
        Object selected = selection.getFirstElement();
        if (selected instanceof IRow) {
            position = ((IRow)selected).getRowNumber();
            newRow = tableContentsGeneration.insertRowAfter(position);
            tableViewer.insert(newRow, position);
        } else {
            tableContentsGeneration.newRow();
        }

        tableViewer.refresh(true);
        contentPage.redrawTable();
    }
}
