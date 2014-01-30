/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action that navigates to the table structure which underlies the table contents.
 */
class NavigateToTableStructureAction extends Action {

    private ITableContents tableContents;

    public NavigateToTableStructureAction(ITableContents tableContents) {
        this.tableContents = tableContents;
        setText(Messages.NavigateToTableStructureAction_Label);
        setToolTipText(Messages.NavigateToTableStructureAction_ToolTip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TableStructure.gif")); //$NON-NLS-1$
    }

    /**
     * Opens the editor for the table structure which these table contents are based on.
     */
    @Override
    public void run() {
        try {
            if (!IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                // if the property changed while the editor is open
                return;
            }
            ITableStructure tableStructure = tableContents.findTableStructure(tableContents.getIpsProject());
            if (tableStructure != null) {
                IpsUIPlugin.getDefault().openEditor(tableStructure);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
