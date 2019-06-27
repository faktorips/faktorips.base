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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.internal.model.tablecontents.TableRows;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.util.UiMessage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.TableDescriptionPage;

/**
 * Editor for a table content.
 */
public class TableContentsEditor extends TimedIpsObjectEditor implements IModelDescriptionSupport {

    private ContentPage contentsPage;

    protected ITableContents getTableContents() {
        return (ITableContents)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException {
        contentsPage = new ContentPage(this);
        addPage(contentsPage);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);

        // always refresh the table after saving
        // thus all problem markers of index error are updated
        // necessary because maybe there are old index validation problem marker,
        // this could happen if the index error state didn't changed (e.g. at least there are
        // errors)
        contentsPage.refreshTable();
    }

    @Override
    protected void refresh() {
        super.refresh();
        contentsPage.refreshTable();
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.TableContentsEditor_TableContentsEditor_title2 + " " + getTableContents().getName() + " (" //$NON-NLS-1$ //$NON-NLS-2$
                + getTableContents().getTableStructure() + ")"; //$NON-NLS-1$
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        ITableStructure tableStructure = getTableContents().findTableStructure(getIpsProject());
        if (tableStructure != null) {
            return new TableDescriptionPage(tableStructure);
        } else {
            return null;
        }
    }

    @Override
    protected List<IMessage> getMessages() {
        List<IMessage> messages = super.getMessages();
        TableRows tableRows = (TableRows)getTableContents().getTableRows();
        if (tableRows.isUniqueKeyValidationEnabled() && !tableRows.isUniqueKeyValidatedAutomatically()) {
            messages.add(0, new UiMessage(Messages.TableContentsEditor_UniqueKeysValidatedManually));
        }
        return messages;
    }

    /*
     * @Override protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
     * contentsPage.updateToolbarActionsEnabledStates();
     * 
     * }
     */
}
