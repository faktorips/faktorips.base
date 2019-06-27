/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.internal.model.tablecontents.TableRows;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action to manually trigger the unique key validation.
 * 
 * @see TableRows#isUniqueKeyValidatedAutomatically()
 */
public class UniqueKeyValidatonAction extends Action {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;

    public UniqueKeyValidatonAction(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
        setImageDescriptor(IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Ok.png", true)); //$NON-NLS-1$
        setToolTipText(Messages.UniqueKeyValidatonAction_Tooltip);
    }

    @Override
    public void run() {
        IpsUIPlugin.getDefault().runWorkspaceModification(new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                ITableContents tableContents = (ITableContents)tableViewer.getInput();
                TableRows tableRows = (TableRows)tableContents.getTableRows();
                tableRows.validateUniqueKeysManually();
            }
        });
        tableViewer.refresh(true);
    }

}
